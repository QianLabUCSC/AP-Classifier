package stanalysis;

import i2analysis.Device;

import java.util.*;

import jdd.bdd.BDD;
import stanalysis.*;
import RuleSetPreproccessing.*;

public class DeviceAPT {
	static BDDACLWrapper baw;
	String name;
	// acl name to its id in acllib
	HashMap<String, LinkedList<ACLRule>> aclmap;
	HashMap<String, Integer> rawacl;
	HashSet<Integer> rawaclinuse;
	String[] usedacls;
	ArrayList<ForwardingRule> fws;
	// subnet name -> subnet info
	HashMap <String, ArrayList<Subnet>> subnets;
	HashMap<String, HashSet<String>> vlan_ports;
	ArrayList<ACLUse> acluses;
	HashMap <String, Integer> fwbdds;
//	HashMap <String, FWDAPSet> fwaps;
	// portname -> acl bdd, should be physical port
	// combined with vlan info
	HashMap<String, Integer> inaclbdds;
	HashMap<String, Integer> outaclbdds;

	
	HashMap <Integer, String> bddPorts;
	
	
	public HashMap <Integer, String> get_bddPorts_map()
	{
		return bddPorts;
	}
	
	public HashMap<String, HashSet<String>> get_vlan_mapping()
	{
		return vlan_ports;
	}
	
	public String get_name()
	{
		return this.name;
	}
	
	public HashMap<String, Integer> get_fwbdds_map()
	{
		return fwbdds;
	}
	

	public void show_acls()
	{
		for (String name : aclmap.keySet())
		{
			if(aclmap.get(name).size() >= 40)
			{
				System.out.println("name:" + name);
				System.out.println(aclmap.get(name).size());
			}
		}
	}
	
	public int num_acl_rules_used()
	{
		int rule_count = 0;
		for (ACLUse one_use : acluses)
		{
			if(aclmap.containsKey(one_use.getnumber()))
			{
				rule_count = rule_count + aclmap.get(one_use.getnumber()).size();
			}
		}
		return rule_count;
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
	
	public void show_acl_bddsize()
	{
		for (String name : rawacl.keySet())
		{
			System.out.println(aclmap.get(name).size() + " " + baw.getNodeSize(rawacl.get(name)));
		}
	}

	public DeviceAPT(String dname)
	{
		this.name = dname;
		//ports = new HashMap<String, Port>();
		aclmap = new HashMap<String, LinkedList<ACLRule>>();
		fws = new ArrayList<ForwardingRule>();
		vlan_ports = new HashMap<String, HashSet<String>>();
		acluses = new ArrayList<ACLUse>();
		subnets = new HashMap<String, ArrayList<Subnet>> ();
	}

	public Collection<Integer> getinaclbdds()
	{
		return inaclbdds.values();
	}

	public Collection<Integer> getoutaclbdds()
	{
		return outaclbdds.values();
	}

	public Collection<Integer> getRawACL()
	{
		return rawacl.values();
	}

	public Collection<Integer> getRawACLinUse()
	{
		return rawaclinuse;
	}

	public void computeRawACL()
	{
		rawacl = new HashMap<String, Integer> ();
		for(String aclname : aclmap.keySet())
		{
			rawacl.put(aclname, baw.ConvertACLs(aclmap.get(aclname)));
		}
		
		//System.out.println(rawacl);
	}

	public void computeACLBDDs()
	{
		computeRawACL();
		rawaclinuse = new HashSet<Integer>();
		HashMap<String, ArrayList<Integer>> inaclbddset = new HashMap<String, ArrayList<Integer>> ();
		HashMap<String, ArrayList<Integer>> outaclbddset = new HashMap<String, ArrayList<Integer>> ();
		for(int i = 0; i < acluses.size(); i ++)
		{
			ACLUse oneacluse = acluses.get(i);
			ArrayList<Subnet> subs = subnets.get(oneacluse.getinterface());
			//System.out.println(oneacluse);
			int rawaclbdd;
			if(rawacl.containsKey(oneacluse.getnumber()))
			{

				rawaclbdd = rawacl.get(oneacluse.getnumber());
				rawaclinuse.add(rawaclbdd);
			}else
			{
				// cannot find the acl
				continue;
			}
			
			HashSet<String> ports;
			if(vlan_ports.containsKey(oneacluse.getinterface()))
			{
				ports = vlan_ports.get(oneacluse.getinterface());
			}else
			{
				ports = new HashSet<String> ();
				ports.add(oneacluse.getinterface());
			}
			
			/*ACL, predicate, interface, direction, <ports>, <subnets> */
			if(oneacluse.isin())
			{
				int aclbdd = baw.encodeACLin(subs, rawaclbdd, ports.size());
				//System.out.println(oneacluse);

				for(String pport : ports)
				{
					if(inaclbddset.containsKey(pport))
					{
						inaclbddset.get(pport).add(aclbdd);
					}else
					{
						ArrayList<Integer> newset = new ArrayList<Integer>();
						newset.add(aclbdd);
						inaclbddset.put(pport, newset);
					}
				}
			}else
			{
				int aclbdd = baw.encodeACLout(subs, rawaclbdd, ports.size());
				for(String pport : ports)
				{
					if(outaclbddset.containsKey(pport))
					{
						outaclbddset.get(pport).add(aclbdd);
					}else
					{
						ArrayList<Integer> newset = new ArrayList<Integer>();
						newset.add(aclbdd);
						outaclbddset.put(pport, newset);
					}
				}
			}
		}

		inaclbdds = new HashMap<String, Integer>();
		outaclbdds = new HashMap<String, Integer>();
		for(String pport : inaclbddset.keySet())
		{
			ArrayList<Integer> bddset = inaclbddset.get(pport);
			int [] bdds = Utility.ArrayListToArray(bddset);
			//System.out.println(bdds.length);
			int andbdd = baw.AndInBatch(bdds);
			baw.DerefInBatch(bdds);
			inaclbdds.put(pport, andbdd);
		}
		for(String pport : outaclbddset.keySet())
		{
			ArrayList<Integer> bddset = outaclbddset.get(pport);
			int [] bdds = Utility.ArrayListToArray(bddset);
			int andbdd = baw.AndInBatch(bdds);
			baw.DerefInBatch(bdds);
			outaclbdds.put(pport, andbdd);
		}

		//System.out.println("in: " + inaclbdds);
		//System.out.println("out: " + outaclbdds);
	}

	public static void setBDDWrapper(BDDACLWrapper baw)
	{
		DeviceAPT.baw = baw;
	}

	public HashSet<String> vlanToPhy(String vlanport)
	{
		return vlan_ports.get(vlanport);
	}

	/**
	 * 
	 * @param port
	 * @param fwdaps
	 * @return <portname, ap set>
	 * mapped VLan port may overlap with physical port...
	 */
	public HashMap <String, Integer> FowrdAction(String port, int apt)
	{
		HashMap <String, Integer> fwded = new HashMap<String, Integer>();
		
		BDD thebdd = baw.getBDD();

		// in acl
		int aptmp = apt;
		thebdd.ref(aptmp);
		
		if(inaclbdds.containsKey(port))
		{
			aptmp = thebdd.andTo(aptmp, inaclbdds.get(port));
		}

		//System.out.println(fwaps.keySet());

		Iterator iter = fwbdds.entrySet().iterator();
		while(iter.hasNext())
		//for(String otherport : fwaps.keySet())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String otherport = (String) entry.getKey();
			//
			if(!otherport.equals(port))
			{
				int aptmp1 = thebdd.ref(thebdd.and(aptmp, (Integer)entry.getValue()));
			
				
				if(aptmp1 != BDDACLWrapper.BDDFalse)
				{
					/*
					 * map vlan to physical port
					 */
					if(otherport.startsWith("vlan"))
					{
						HashSet<String> phyports = vlanToPhy(otherport);
						if(phyports == null)
						{

						}else
						{
							for(String pport:phyports)
							{
								// cannot go back to the incoming port
								if(!pport.equals(port))
								{
									// out acl
									int aptmp2 = thebdd.ref(aptmp1);
									if(outaclbdds.containsKey(pport))
									{
										aptmp2 = thebdd.andTo(aptmp2, outaclbdds.get(pport));
									}
									if(aptmp2 != BDDACLWrapper.BDDFalse)
									{
										fwded.put(pport, aptmp2);
									}
								}
							}
						}
					}else{
						// out acl
						if(outaclbdds.containsKey(otherport))
						{
							aptmp1 = thebdd.andTo(aptmp1, outaclbdds.get(otherport));
						}
						if(aptmp1 != BDDACLWrapper.BDDFalse)
						{
							fwded.put(otherport, aptmp1);
						}
					}
				}
			}
		}

		return fwded;
	}


	public Collection<Integer> getfwbdds()
	{
		return fwbdds.values();
	}
	

	public void computeFWBDDs()
	{
		Collections.sort(fws);
		this.fwbdds = DeviceAPT.baw.getfwdbdds_sorted_no_store(fws);
		
		//this.fwbdds = DeviceAPT.baw.getfwdbdds(fws);
		//System.out.println(fwbdds.size());
		//for(String iname : fwbdds.keySet())
		//{
		//	System.out.println(iname + ": " + fwbdds.get(iname));
		//}
		
		bddPorts = new HashMap<Integer,String>();
		for(String port : fwbdds.keySet()){
			int BDD = fwbdds.get(port);
			bddPorts.put(BDD, port);
		}
	}

	public void addACL(String name, LinkedList<ACLRule> acl)
	{
		aclmap.put(name, acl);
	}
	public void addFW(ForwardingRule fw)
	{
		fws.add(fw);
	}
	public void addVlanPorts(String vlan, HashSet<String> ports)
	{
		vlan_ports.put(vlan, ports);
	}
	public void addACLUse(ACLUse oneuse)
	{
		acluses.add(oneuse);
	}
	public void addSubnet(Subnet sub)
	{
		if(subnets.keySet().contains(sub.getname()))
		{
			subnets.get(sub.getname()).add(sub);
		}else
		{
			ArrayList<Subnet> subs = new ArrayList<Subnet>();
			subs.add(sub);
			subnets.put(sub.getname(), subs);
		}
	}

}

