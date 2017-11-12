package i2analysis;

import java.io.*;
import java.util.*;

import stanalysis.ForwardingRule;
import RuleSetPreproccessing.*;

/**
 * only FIB is considered.
 * read forwarding rules from configuration files for each device
 */

public class BuildNetwork {
	
	public static void main (String[] args) throws IOException
	{
		BDDACLWrapper baw = new BDDACLWrapper();
		Device.setBDDWrapper(baw);
		Device d = parseDevice("atla", "i2/atlaap");
		//System.out.println(d.subnets.size());
		d.computeFWBDDs();
	}

	public static Device parseDevice(String dname, String filenameparsed) throws IOException
	{
		Device d = new Device(dname);
		File inputFile = new File(filenameparsed);
		readParsed(d, inputFile);
		return d;
	}
	
	public static ForwardingRule assembleFW(String[] entries)
	{
		//use short name for physical port
		String portname = entries[3];
		
		if(entries[3].startsWith("vlan"))
		{
			portname = entries[3];
			System.out.println(portname);
		}else
		{
			//System.out.println(entries[3]);
			portname = entries[3].split("\\.")[0];
		}
		return new ForwardingRule(Long.parseLong(entries[1]), Integer.parseInt(entries[2]), portname);
	}
	

	
	public static void readParsed(Device d, File inputFile) throws IOException
	{
		Scanner OneLine = null;
		try {
			OneLine = new Scanner (inputFile);
			OneLine.useDelimiter("\n");
			//scanner.useDelimiter(System.getProperty("line.separator"));
	
		} catch (FileNotFoundException e) {
			System.out.println ("File not found!"); // for debugging
			System.exit (0); // Stop program if no file found
		}
		
		while(OneLine.hasNext())
		{
			String linestr = OneLine.next();
			String[] tokens = linestr.split(" ");
			if(tokens[0].equals( "fw"))
			{
				ForwardingRule onerule = assembleFW(tokens);
				d.addFW(onerule);
				//System.out.println("add: " + onerule);
			}
		}
	}
	
}
