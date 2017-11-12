package RuleSetPreproccessing;
/*
 * Parser.java
 *
 * Created : July 25, 2006, 3:20 AM
 * Last Modified : December 12, 2006
 * Author : Eric Gregory Wong
 *
 * ************ MAIN CALLING FUNCTION FOR PARSING AND PACKETSET
CREATION *************
 *
 * Functionality:
 *
 * 1. Parses Router Config files with commands:
 * a. Router hostname
 * b. Interface:
 * i. Name
 * ii. IP address and mask
 * iii.Interface access-group in and out
 * iv. ***Exception - ignores the above if keyword "remark" is used
 * c. Access-list:
 * i. Standard lists
 * ii. Extended lists with:
 * (1) ACL #
 * (2) Dynamic name
 * (3) Timeout minutes (to be added)
 * (4) Permit or deny
 * (5) Protocol
 * (6) Source + wildcard
 * (7) Destination + wildcard
 * (8) Precedence (to be added)
 * (9) TOS (to be added)
 * (10) Log (to be added)
 */

import java.io.*;
import java.util.*;

public class Parser {
	/***************
	 * Debugging file names
	 * These files will be saved in the source code or project directory when run
	 ***************/
	String testFilename1 = "Test File 1 Parsed output check direct from read.txt";
	String testFilename2 = "Test File 2 Intermediate parser check.txt";
	String testFilename3 = "Test File 3 Last ACL Output.txt";
	String testFilename4 = "Test File 4 Hashtable Output (Post).txt";
	String testFilename5 = "Test File 5 Hashtable Output (from read).txt";
	String testFilename6 = "Test File 6 Last Interface check.txt";
	String testFilename7 = "Test File 7 Files in Folder Check.txt";

	public static enum ACLType{
		standard, extend;
	}	

	public static void GetArgument(Scanner tokeninline, ArrayList<String> argument)
	{
		argument.clear();
		while(tokeninline.hasNext())
		{
			argument.add(tokeninline.next());
		}
	}

	/**
	 * aclNumbers[0] -- previous acl number
	 * aclNumbers[1] -- current acl number
	 * return true the acl is valid
	 * return false the acl is invalid
	 * */
	public static boolean CheckValidACL(ArrayList<String> argument, int[] aclNumbers)
	{
		try{
			if(aclNumbers[0] == -1)
			{
				aclNumbers[0] = Integer.valueOf(argument.get(0));
				aclNumbers[1] = aclNumbers[0];
				argument.remove(0);
			}else
			{
				aclNumbers[1] = Integer.valueOf(argument.get(0));
				argument.remove(0);
			}
		} catch(NumberFormatException e)
		{
			//the acl rule is not valid
			return false;
		}

		if(argument.get(0).equals("remark"))
		{
			return false;
		}else{
			return true;
		}
	}

	public static void DebugInput(PrintStream out, ArrayList<String> argument, String keyword) throws IOException
	{
		out.print (keyword + " | ");
		if(argument != null){
			for (int j=0; j < argument.size(); j++) {
				out.print (argument.get(j) + " | ");
			}
		}
		out.println();
	}

	// seems not useful...
	public static void AddDynamic(ACLRule onerule, ArrayList<String> argument)
	{
		if (argument.get(0).equals ("dynamic")) {
			onerule.dynamic="dynamic";
			argument.remove(0);
			onerule.dynamicName=argument.get(0);
			argument.remove(0);
		}
	}

	public static void CheckPermitDeny(ACLRule onerule, ArrayList<String> argument)
	{
		if(argument.get(0).equals("permit"))
		{
			onerule.permitDeny = "permit";
		}else
		{
			onerule.permitDeny = "deny";
		}
		argument.remove(0); 
	}

	public static void HandleACLRuleStandard(ACLRule onerule, ArrayList<String> argument)
	{
		//acl number has been removed.
		// the first argument is the ip address
		if(argument.get(0).equals("host"))
		{
			argument.remove(0);
		}

		onerule.source = argument.get(0);
		argument.remove(0);
		if(!argument.isEmpty())
		{
			if(!argument.get(0).equals("log")){
				onerule.sourceWildcard = argument.get(0);
			}
		}

	}

	public static void HandleACLRuleExtend(ACLRule onerule, ArrayList<String> argument)
	{
		//first is the protocol
		onerule.protocolLower = ParseTools.GetProtocolNumber(argument.get(0));
		if (onerule.protocolLower.equals("256")) {
			onerule.protocolLower = "0";
			onerule.protocolUpper = "255";
		}else {
			onerule.protocolUpper = onerule.protocolLower;
		}
		argument.remove(0);

		// then is the source field
		if (argument.get(0).equals("any")) {
			onerule.source = "any";
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}
		else if (argument.get(0).equals("host")) {
			argument.remove(0);
			onerule.source = argument.get(0);
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}
		else {// the last case uses wildcard
			onerule.source = argument.get(0);
			argument.remove(0);
			onerule.sourceWildcard = argument.get(0);
			argument.remove(0);
			ParseTools.ParsePort (onerule, argument, "source");
		}

		//the last is the destination field
		/*** If the destination keyword is "any" ***/
		if (argument.get(0).equals("any")) {
			onerule.destination = "any";
			argument.remove(0);
			if (!argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
		else if (argument.get(0).equals("host")) {
			argument.remove(0);
			onerule.destination = argument.get(0);
			argument.remove(0);
			if (! argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
		else {
			onerule.destination = argument.get(0);
			argument.remove(0);
			onerule.destinationWildcard = argument.get(0);
			argument.remove(0);
			if (! argument.isEmpty()){
				ParseTools.ParsePort (onerule, argument, "destination");
			}
		}
	}

	public static ACLType CheckACLType(int aclnum)
	{
		if (aclnum >=1 && aclnum <=99 )
			return ACLType.standard ;
		else if (aclnum >=100 && aclnum <=199 )
			return ACLType.extend;
		else if (aclnum >= 1300 && aclnum <=1999 )
			return ACLType.standard;
		else if (aclnum >=2000 && aclnum <=2699 )
			return ACLType.extend;
		//by default
		return ACLType.extend;
	}

	public static String SeperateMask(String instr, int ind)
	{
		int maskNum = Integer.parseInt(instr.substring(ind + 1));
		// maskNum = 0,1,2,...32
		int byteNum = maskNum / 8;
		if(byteNum == 4)
		{
			return "255.255.255.255";
		}
		String mask = "";
		for(int i = 0; i < byteNum; i ++)
		{
			mask = mask + "255.";
		}

		int rest = maskNum%8;
		int nextByteNum = (int) Utility.SumPower2(7 - rest + 1, 7);
		mask = mask + Integer.toString(nextByteNum);

		//fill up the rest bytes with "0"
		for(int i = 0; i < 4 - byteNum - 1; i ++)
		{
			mask = mask + ".0";
		}

		return mask;
	}


}
