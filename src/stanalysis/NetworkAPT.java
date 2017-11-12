package stanalysis;

import i2analysis.Device;
import i2analysis.ReconstructionTree;
import i2analysis.Trace;
import i2analysis.Tree;

import java.io.*;
import java.util.*;

import RuleSetPreproccessing.*;


public class NetworkAPT {
	BDDACLWrapper bddengine;
	String name;
	HashMap<String, DeviceAPT> devices;

	// device|port - device|port
	HashMap<PositionTuple, PositionTuple> topology;
	APComputer fwdapc;
	APComputer aclapc;
	
	Tree generatingTree;
	Tree reconstrcutingTree;
	
	private static final boolean enableTreePrinting = true;
	private static final boolean enableAPWeight = false;
	
	public Tree getReconstrcutingTree()
	{
		return reconstrcutingTree;
	}
	
	public HashMap<String, DeviceAPT> getDevices( )
	{
		return devices;
	}

	public HashMap<PositionTuple, PositionTuple> get_topology()
	{
		return topology;
	}

	public PositionTuple LinkTransfer(PositionTuple pt)
	{
		//System.out.println();
		return topology.get(pt);
	}

	public Collection<DeviceAPT> getAllDevices()
	{
		return devices.values();
	}

	public Set<PositionTuple> getallactiveports()
	{
		return topology.keySet();
	}

	public DeviceAPT getDevice(String dname)
	{
		return devices.get(dname);
	}

	public BDDACLWrapper getBDDEngine()
	{
		return bddengine;
	}

	public APComputer getFWDAPC()
	{
		return fwdapc;
	}

	public APComputer getACLAPC()
	{
		return aclapc;
	}

	public NetworkAPT(String name) throws IOException
	{
		this.name = name;
		System.out.println("< "+this.name+" >");
		bddengine = new BDDACLWrapper();

		devices = new HashMap<String, DeviceAPT> ();
		DeviceAPT.setBDDWrapper(bddengine);
		String foldername2 = "st/";
		String foldername1 = "stconfig/";
		String [] devicenames = {"bbra_rtr", "bbrb_rtr", "boza_rtr", "bozb_rtr", "coza_rtr", "cozb_rtr", "goza_rtr",
				"gozb_rtr", "poza_rtr", "pozb_rtr", "roza_rtr", "rozb_rtr", "soza_rtr", "sozb_rtr", "yoza_rtr", "yozb_rtr"};
		

//		String [] devicenames = {"bbra_rtr", "bbrb_rtr"};
		
		
		for( int i = 0; i < devicenames.length; i ++)
		{
			DeviceAPT d = BuildNetworkAPT.parseDeviceAPT(devicenames[i], foldername1 + devicenames[i] + "_config.txt"
					, foldername2 + devicenames[i] + "ap");


			//System.out.println(d.name);
			devices.put(d.name, d);
			//d.show_fwd_bddsize();
		}	
		
		for(String d_name : devicenames)
		{
			devices.get(d_name).computeFWBDDs();

		}
		
		
		for(DeviceAPT d : devices.values())
		{
			d.computeACLBDDs();
		}
		


		int rule_count = 0;
		for(DeviceAPT d : devices.values())
		{
			//System.out.println(d.num_acl_rules_used());
			rule_count = rule_count + d.num_acl_rules_used();
		}
		System.out.println("Total number of acl rules: " + rule_count);

		ArrayList<Integer> fwdbddary = new ArrayList<Integer> ();
		ArrayList<Integer> fwdbddaryNeg = new ArrayList<Integer> ();
		ArrayList<Integer> aclbddary = new ArrayList<Integer> ();
		ArrayList<Integer> aclbddaryNeg = new ArrayList<Integer> ();
		
		for(DeviceAPT d : devices.values())
		{
			Collection<Integer> bdds = d.getfwbdds();
			for(int bdd : bdds)
			{
				fwdbddary.add(bdd);
			}
		}
		
		for(int BDD: fwdbddary){
			int BDDNeg = bddengine.getBDD().not(BDD);
			bddengine.getBDD().ref(BDDNeg);
			fwdbddaryNeg.add(BDDNeg);
		}
		
		long memoryUsage =0;
		for(int BDD: fwdbddary)
		{
			memoryUsage = memoryUsage + bddengine.getBDD().nodeCount(BDD) * 24;
		}
		System.out.println("Memory cost of Fwd BDDs: "+ memoryUsage/1000000.0+" MB");
		
		

		for(DeviceAPT d : devices.values())
		{
			Collection<Integer> bdds = d.getinaclbdds();
			for(int bdd : bdds)
			{
				aclbddary.add(bdd);
			}
			bdds = d.getoutaclbdds();
			for(int bdd : bdds)
			{
				aclbddary.add(bdd);
			}
		}
		
		for(int BDD: aclbddary){
			int BDDNeg = bddengine.getBDD().not(BDD);
			bddengine.getBDD().ref(BDDNeg);
			aclbddaryNeg.add(BDDNeg);
		}

		long ACLmemoryUsage =0;
		for(int BDD: aclbddary)
		{
			ACLmemoryUsage = ACLmemoryUsage + bddengine.getBDD().nodeCount(BDD) * 24;
		}
		System.out.println("Memory cost of ACL BDDs: "+ ACLmemoryUsage/1000000.0+" MB");
	
		/**
		 * compute APs for fwd and acl rules respectively
		 * 
		 */
		System.out.println("Computing APs for Fwd BDDs- ");
		fwdapc = new APComputer(fwdbddary, bddengine);
		System.out.println("Computing APs for ACL BDDs- ");
		aclapc = new APComputer(aclbddary, bddengine);
		
		
		
		Trace trace = new Trace(fwdapc.getAllAP(),bddengine, enableAPWeight, this.name);
		System.out.println(trace.getTrace().size());
		
			
		for(int BDD: fwdbddary){
			int BDDNeg = bddengine.getBDD().not(BDD);
			bddengine.getBDD().ref(BDDNeg);
			fwdbddaryNeg.add(BDDNeg);
		}
		
		for(int BDD: aclbddary){
			int BDDNeg = bddengine.getBDD().not(BDD);
			bddengine.getBDD().ref(BDDNeg);
			aclbddaryNeg.add(BDDNeg);
		}
		
		
		
		long t1 = System.nanoTime();
		
			generatingTree = new Tree(bddengine);
			generatingTree=generatingTree.treeGeneration(fwdbddary, fwdbddaryNeg, generatingTree);
			
		long t2 = System.nanoTime();
		System.out.println("Construction of generatingTree takes " + (t2 - t1)/1000000.0 + "ms.");
		
		if(enableTreePrinting){
			generatingTree.preOrder(generatingTree.getRoot());
			generatingTree.sumDepthPrint("generatingTree");
		}
			
		
		long t3 = System.nanoTime();
		ReconstructionTree reconstructedTree = new ReconstructionTree(fwdbddary,fwdbddaryNeg,generatingTree,fwdapc.PredicateAPREP,bddengine);
		reconstrcutingTree = reconstructedTree.reconstructionTree();
		long t4 = System.nanoTime();
		System.out.println("Reconstruction of generatingTree takes " + (t4 - t3)/1000000.0 + "ms.");
		
		if(enableTreePrinting){
			reconstrcutingTree.preOrder(reconstrcutingTree.getRoot());
			reconstrcutingTree.sumDepthPrint("reconstrcutingTree");
		}
		
		
		/**
		 * throughput
		 */
		System.out.print("Query the generatingTree - ");
		generatingTree.Search(trace.getTrace(), generatingTree.getRoot());
		
		System.out.print("Query the reconstrcutingTree - ");
		reconstrcutingTree.Search(trace.getTrace(), reconstrcutingTree.getRoot());

	
		
		/**
		 * topology information
		 */
		topology = new HashMap<PositionTuple, PositionTuple>();
		
		addTopology("bbra_rtr","te7/3","goza_rtr","te2/1");
		addTopology("bbra_rtr","te7/3","pozb_rtr","te3/1");
		addTopology("bbra_rtr","te1/3","bozb_rtr","te3/1");
		addTopology("bbra_rtr","te1/3","yozb_rtr","te2/1");
		addTopology("bbra_rtr","te1/3","roza_rtr","te2/1");
		addTopology("bbra_rtr","te1/4","boza_rtr","te2/1");
		addTopology("bbra_rtr","te1/4","rozb_rtr","te3/1");
		addTopology("bbra_rtr","te6/1","gozb_rtr","te3/1");
		addTopology("bbra_rtr","te6/1","cozb_rtr","te3/1");
		addTopology("bbra_rtr","te6/1","poza_rtr","te2/1");
		addTopology("bbra_rtr","te6/1","soza_rtr","te2/1");
		addTopology("bbra_rtr","te7/2","coza_rtr","te2/1");
		addTopology("bbra_rtr","te7/2","sozb_rtr","te3/1");
		addTopology("bbra_rtr","te6/3","yoza_rtr","te1/3");
		addTopology("bbra_rtr","te7/1","bbrb_rtr","te7/1");
		addTopology("bbrb_rtr","te7/4","yoza_rtr","te7/1");
		addTopology("bbrb_rtr","te1/1","goza_rtr","te3/1");
		addTopology("bbrb_rtr","te1/1","pozb_rtr","te2/1");
		addTopology("bbrb_rtr","te6/3","bozb_rtr","te2/1");
		addTopology("bbrb_rtr","te6/3","roza_rtr","te3/1");
		addTopology("bbrb_rtr","te6/3","yozb_rtr","te1/1");
		addTopology("bbrb_rtr","te1/3","boza_rtr","te3/1");
		addTopology("bbrb_rtr","te1/3","rozb_rtr","te2/1");
		addTopology("bbrb_rtr","te7/2","gozb_rtr","te2/1");
		addTopology("bbrb_rtr","te7/2","cozb_rtr","te2/1");
		addTopology("bbrb_rtr","te7/2","poza_rtr","te3/1");
		addTopology("bbrb_rtr","te7/2","soza_rtr","te3/1");
		addTopology("bbrb_rtr","te6/1","coza_rtr","te3/1");
		addTopology("bbrb_rtr","te6/1","sozb_rtr","te2/1");
		addTopology("boza_rtr","te2/3","bozb_rtr","te2/3");
		addTopology("coza_rtr","te2/3","cozb_rtr","te2/3");
		addTopology("goza_rtr","te2/3","gozb_rtr","te2/3");
		addTopology("poza_rtr","te2/3","pozb_rtr","te2/3");
		addTopology("roza_rtr","te2/3","rozb_rtr","te2/3");
		addTopology("soza_rtr","te2/3","sozb_rtr","te2/3");	
		addTopology("yoza_rtr","te1/1","yozb_rtr","te1/3");
		addTopology("yoza_rtr","te1/2","yozb_rtr","te1/2");
		
	}


	public void addTopology(String d1, String p1, String d2, String p2)
	{
		PositionTuple pt1 = new PositionTuple(d1, p1);
		PositionTuple pt2 = new PositionTuple(d2, p2);
		// links are two way
		topology.put(pt1, pt2);
		topology.put(pt2, pt1);
	}

	public static void main (String[] args) throws IOException
	{
		NetworkAPT n = new NetworkAPT("st");
	}

}

