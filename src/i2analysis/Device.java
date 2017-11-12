package i2analysis;

import java.util.*;

import stanalysis.APComputer;
import stanalysis.ForwardingRule;
import stanalysis.Subnet;
import RuleSetPreproccessing.*;

public class Device {
	static BDDACLWrapper baw;
	String name;
	// acl name to its id in acllib
	ArrayList<ForwardingRule> fws;
	HashMap <String, Integer> fwbdds;	
	HashMap <Integer, String> bddPorts;
	
	
	public HashMap <Integer, String> get_bddPorts_map()
	{
		return bddPorts;
	}
	

	public String get_name()
	{
		return name;
	}
	
	public HashMap <String, Integer> get_fwbdds_map ()
	{
		return fwbdds;
	}
	

	public Device(String dname)
	{
		this.name = dname;
		fws = new ArrayList<ForwardingRule>();
	}

	public void show_fwd_bddsize()
	{
		System.out.println(name);
		int total_size = 0;
		for(Integer one_p : fwbdds.values())
		{
			total_size = total_size + baw.getNodeSize(one_p);
		}
		System.out.println(total_size);
	}


	public static void setBDDWrapper(BDDACLWrapper baw)
	{
		Device.baw = baw;
	}









	public Collection<Integer> getfwbdds()
	{
		return fwbdds.values();
	}



	public void computeFWBDDs()
	{
		Collections.sort(fws);
		this.fwbdds = Device.baw.getfwdbdds_sorted_no_store(fws);
		
		
		bddPorts = new HashMap<Integer,String>();
		for(String port : fwbdds.keySet()){
			int BDD = fwbdds.get(port);
			bddPorts.put(BDD, port);
			
		}
		
		//this.fwbdds = Device.baw.getfwdbdds(fws);
		
		//System.out.println(fwbdds.size());
		//for(String iname : fwbdds.keySet())
		//{
		//	System.out.println(iname + ": " + fwbdds.get(iname));
		//}
	}

	public void addFW(ForwardingRule fw)
	{
		fws.add(fw);
	}




}
