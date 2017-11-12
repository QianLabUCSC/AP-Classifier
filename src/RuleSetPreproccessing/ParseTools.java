package RuleSetPreproccessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class ParseTools {

	/************************************************************************************
	 *
	 * Function to calculate the network prefix of an IP address.
	 * Input : IP address (string), IP mask (string)
	 * Output : Network prefix (string)
	 *
	 ************************************************************************************/
	static String GetPrefix (String ip, String mask) {
		String Prefix;
		int ipOctet1=0, ipOctet2=0, ipOctet3=0, ipOctet4=0;
		int maskOctet1=0, maskOctet2=0, maskOctet3=0, maskOctet4=0;
		/*** Break up ip string into octets ***/
		Scanner s = new Scanner(ip).useDelimiter("\\.");
		ipOctet1 = Integer.parseInt(s.next());
		ipOctet2 = Integer.parseInt(s.next());
		ipOctet3 = Integer.parseInt(s.next());
		ipOctet4 = Integer.parseInt(s.next());
		/*** Break up mask string into octets ***/
		s = new Scanner(mask).useDelimiter("\\.");
		maskOctet1 = Integer.parseInt(s.next());
		maskOctet2 = Integer.parseInt(s.next());
		maskOctet3 = Integer.parseInt(s.next());
		maskOctet4 = Integer.parseInt(s.next());
		/*** Determine the network prefix based on the ip and mask ***/
		Prefix = Integer.toString(ipOctet1 & maskOctet1) + "." +
				Integer.toString(ipOctet2 & maskOctet2) + "." +
				Integer.toString(ipOctet3 & maskOctet3) + "." +
				Integer.toString(ipOctet4 & maskOctet4);
		return Prefix;
	}

	/**
	 * 
	 * @param mask
	 * @return the length of the prefix
	 * 255.255.255.0 -> 24
	 */
	static int GetPrefixLength(String mask)
	{
		String [] masks = mask.split("\\.");
		int numofzeros = 0;
		for(int i = masks.length - 1; i >=0; i --)
		{
			int offset = 1;
			int maskInt = Integer.parseInt(masks[i]);

			for(int j = 0; j < 8; j ++)
			{
				offset = offset << j;
				if((maskInt & offset )>0)
				{
					return masks.length * 8 - numofzeros;
				}
				numofzeros ++;
			}
		}
		return masks.length * 8 - numofzeros;
	}

	/************************************************************************************
	 *
	 * Function to convert ports in an ACL rule to its IANA number assignment.
	 * Input : ACL rule (ACLrule), parsed argument (string), position k (int),
	 * port name or number (string)
	 * Output : position k, which is one position after reading the port arguments
	 * The upper and lower port numbers are stored in the ACL rule that was
			input.
	 * Limitation: Does not handle neq operator yet
	 *
	 ************************************************************************************/
	static int ParsePort (ACLRule aclRule, String[] argument, int k, String port) {
		// handles eq, gt, lt, range
		// to include handling for neq
		if (argument[k].equalsIgnoreCase("eq")) { // handles eq
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				aclRule.sourcePortUpper = aclRule.sourcePortLower;
				k++;
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
				aclRule.destinationPortUpper = aclRule.destinationPortLower;
			}
		}
		else if (argument[k].equalsIgnoreCase("gt")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination"))
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
		}
		else if (argument[k].equalsIgnoreCase("lt")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortUpper = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination"))
				aclRule.destinationPortUpper = GetPortNumber (argument[k]);
		}
		else if (argument[k].equalsIgnoreCase("range")) {
			k++;
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument[k]);
				k++;
				aclRule.sourcePortUpper = GetPortNumber (argument[k]);
				k++;
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument[k]);
				k++;
				aclRule.destinationPortUpper = GetPortNumber (argument[k]);
			}
		}
		return k;
	}

	static void ParsePort (ACLRule aclRule, ArrayList<String> argument, String port) {
		// handles eq, gt, lt, range
		// to include handling for neq
		if (argument.get(0).equalsIgnoreCase("eq")) { // handles eq
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				aclRule.sourcePortUpper = aclRule.sourcePortLower;
				argument.remove(0);
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
				aclRule.destinationPortUpper = aclRule.destinationPortLower;
			}
		}
		else if (argument.get(0).equalsIgnoreCase("gt")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination"))
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
		}
		else if (argument.get(0).equalsIgnoreCase("lt")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortUpper = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination"))
				aclRule.destinationPortUpper = GetPortNumber (argument.get(0));
		}
		else if (argument.get(0).equalsIgnoreCase("range")) {
			argument.remove(0);
			if (port.equals("source")) {
				aclRule.sourcePortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
				aclRule.sourcePortUpper = GetPortNumber (argument.get(0));
				argument.remove(0);
			}
			else if (port.equals("destination")) {
				aclRule.destinationPortLower = GetPortNumber (argument.get(0));
				argument.remove(0);
				aclRule.destinationPortUpper = GetPortNumber (argument.get(0));
			}
		}
	}

	/************************************************************************************
	 *
	 * Function to lookup the IANA number assignment of a port.
	 * Input : port name or number (string)
	 * Output : port number (string)
	 *
	 ************************************************************************************/
	static String GetPortNumber (String port) {
		String portNumber;
		// Note switch statement does not work with strings
		if (port.equalsIgnoreCase("tcpmux")) portNumber = "1";
		else if (port.equalsIgnoreCase("ftp-data")) portNumber = "20";
		else if (port.equalsIgnoreCase("ftp")) portNumber = "21";
		else if (port.equalsIgnoreCase("ssh")) portNumber = "22";
		else if (port.equalsIgnoreCase("telnet")) portNumber = "23";
		else if (port.equalsIgnoreCase("smtp")) portNumber= "25";
		else if (port.equalsIgnoreCase("dsp")) portNumber= "33";
		else if (port.equalsIgnoreCase("time")) portNumber= "37";
		else if (port.equalsIgnoreCase("rap")) portNumber= "38";
		else if (port.equalsIgnoreCase("rlp")) portNumber= "39";
		else if (port.equalsIgnoreCase("name")) portNumber= "42";
		else if (port.equalsIgnoreCase("nameserver")) portNumber= "42";
		else if (port.equalsIgnoreCase("nicname")) portNumber= "43";
		else if (port.equalsIgnoreCase("dns")) portNumber = "53";
		else if (port.equalsIgnoreCase("domain")) portNumber = "53";
		else if (port.equalsIgnoreCase("bootps")) portNumber = "67";
		else if (port.equalsIgnoreCase("bootpc")) portNumber = "68";
		else if (port.equalsIgnoreCase("tftp")) portNumber = "69";
		else if (port.equalsIgnoreCase("gopher")) portNumber = "70";
		else if (port.equalsIgnoreCase("finger")) portNumber = "79";
		else if (port.equalsIgnoreCase("http")) portNumber = "80";
		else if (port.equalsIgnoreCase("www")) portNumber = "80";
		else if (port.equalsIgnoreCase("kerberos")) portNumber = "88";
		else if (port.equalsIgnoreCase("pop2")) portNumber = "109";
		else if (port.equalsIgnoreCase("pop3")) portNumber = "110";
		else if (port.equalsIgnoreCase("sunrpc")) portNumber = "111";
		else if (port.equalsIgnoreCase("ident")) portNumber = "113";
		else if (port.equalsIgnoreCase("auth")) portNumber = "113";
		else if (port.equalsIgnoreCase("sftp")) portNumber = "115";
		else if (port.equalsIgnoreCase("nntp")) portNumber = "119";
		else if (port.equalsIgnoreCase("ntp")) portNumber = "123";
		else if (port.equalsIgnoreCase("netbios-ns")) portNumber = "137";
		else if (port.toLowerCase().startsWith("netbios-dg")) portNumber = "138";
		else if (port.toLowerCase().startsWith("netbios-ss")) portNumber = "139";
		else if (port.equalsIgnoreCase("sqlsrv")) portNumber = "156";
		else if (port.equalsIgnoreCase("snmp")) portNumber = "161";
		else if (port.equalsIgnoreCase("snmptrap")) portNumber = "162";
		else if (port.equalsIgnoreCase("bgp")) portNumber = "179";
		else if (port.equalsIgnoreCase("exec")) portNumber = "512";
		else if (port.equalsIgnoreCase("shell")) portNumber = "514";
		else if (port.equalsIgnoreCase("isakmp")) portNumber = "500";
		else if (port.equalsIgnoreCase("biff")) portNumber = "512";
		else if (port.equalsIgnoreCase("lpd")) portNumber = "515";
		else if (port.equalsIgnoreCase("cmd")) portNumber = "514";
		else if (port.equalsIgnoreCase("syslog")) portNumber = "514";
		else if (port.equalsIgnoreCase("whois")) portNumber = "43";
		else if (port.equals("lLth9012b03GJ")) portNumber = "390"; //this is ad hoc
		else portNumber = port;
		return portNumber;
	}


	/************************************************************************************
	 *
	 * Function to lookup the IANA number assignment of a protocol.
	 * Input : protocol name or number (string)
	 * Output : protocol number (string)
	 *
	 ************************************************************************************/
	static String GetProtocolNumber (String protocol) {
		String protocolNumber;
		if(protocol.equalsIgnoreCase("icmp")) protocolNumber = "1" ;
		else if(protocol.equalsIgnoreCase("igmp")) protocolNumber = "2" ;
		//else if(protocol.equalsIgnoreCase("ip")) protocolNumber = "4" ;
		else if(protocol.equalsIgnoreCase("ip")) protocolNumber = "256" ;
		// special case to indicate all protocols. No actual protocol number 256.
		else if(protocol.equalsIgnoreCase("tcp")) protocolNumber = "6" ;
		else if(protocol.equalsIgnoreCase("egp")) protocolNumber = "8" ;
		else if(protocol.equalsIgnoreCase("igp")) protocolNumber = "9" ;
		else if(protocol.equalsIgnoreCase("udp")) protocolNumber = "17" ;
		else if(protocol.equalsIgnoreCase("rdp")) protocolNumber = "27" ;
		else if(protocol.equalsIgnoreCase("ipv6")) protocolNumber = "41" ;
		else if(protocol.equalsIgnoreCase("rsvp")) protocolNumber = "46" ;
		else if(protocol.equalsIgnoreCase("eigrp")) protocolNumber = "88" ;
		else if(protocol.equalsIgnoreCase("l2tp")) protocolNumber = "115" ;
		else if(protocol.equalsIgnoreCase("esp")) protocolNumber = "50";
		else if(protocol.equalsIgnoreCase("ahp")) protocolNumber = "51";
		else if(protocol.equalsIgnoreCase("gre")) protocolNumber = "47";
		else if(protocol.equalsIgnoreCase("ospf")) protocolNumber = "89";
		else {
			System.out.println("Unknown Protocol: " + protocol + "----------------------------");
			protocolNumber = protocol ;
		}
		return protocolNumber;
	}


	
	public static void main(String args[]) {
		// test get prefix
		String ipaddr = "172.140.23.14";
		String prefix = "255.255.255.252";
		System.out.println(GetPrefix(ipaddr, prefix));

		System.out.println(Parser.SeperateMask("/32", 0));
		System.out.println(Parser.SeperateMask("/30", 0));
		System.out.println(Parser.SeperateMask("/16", 0));
		System.out.println(Parser.SeperateMask("/17", 0));

		System.out.println(GetPrefixLength("255.255.255.255"));
		System.out.println(GetPrefixLength("255.255.252.0"));
		System.out.println(GetPrefixLength("255.254.0.0"));
	}

}
