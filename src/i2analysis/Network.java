package i2analysis;

import java.io.*;
import java.util.*;

import stanalysis.APComputer;
import stanalysis.PositionTuple;
import RuleSetPreproccessing.*;



public class Network {
	BDDACLWrapper bddengine;
	String name;
	HashMap<String, Device> devices;

	// device|port - device|port
	HashMap<PositionTuple, PositionTuple> topology;
	//ArrayList<ArrayList<ACLRule>> acllib;
	APComputer fwdapc;
	Tree generatingTree;
	Tree reconstrcutingTree;
	

	private static final boolean enableTreePrinting = true;
	private static final boolean enableAPWeight = true; /*weighted AP*/
	
	public Tree getGeneratingTree()
	{
		return generatingTree;
	}
	
	public Tree getReconstrcutingTree()
	{
		return reconstrcutingTree;
	}
	
	
	public BDDACLWrapper getbddEngine()
	{
		return bddengine;
	}

	public PositionTuple LinkTransfer(PositionTuple pt)
	{
		//System.out.println();
		return topology.get(pt);
	}
	
	public HashMap<PositionTuple, PositionTuple> get_topology()
	{
		return topology;
	}
	
	public HashMap<String, Device> getDevices( )
	{
		return devices;
	}

	public Device getDevice(String dname)
	{
		return devices.get(dname);
	}
	
	public Collection<Device> getAllDevices()
	{
		return devices.values();
	}
	
	public Set<PositionTuple> getallactiveports()
	{
		return topology.keySet();
	}
	
	public APComputer getFWDAPC()
	{
		return fwdapc;
	}
	
	public Network(String name) throws IOException
	{
		this.name = name;
		
		System.out.println("< "+this.name+" >");
		
		devices = new HashMap<String, Device>();
		
		String foldername2 = "i2/";
		String [] devicenames = {"atla","chic","hous","kans","losa","newy32aoa","salt","seat","wash"};
		for( int i = 0; i < devicenames.length; i ++)
		{
			Device d = BuildNetwork.parseDevice(devicenames[i], foldername2 + devicenames[i] + "ap");

			//System.out.println(d.name);
			devices.put(d.name, d);
		}
		
		bddengine = new BDDACLWrapper();

		Device.setBDDWrapper(bddengine);
		
		/**
		 * compute predicates
		 */
				
		for(Device d : devices.values())
		{
			long t1 = System.nanoTime();
			//System.out.println(d.name);
			d.computeFWBDDs();
			
			long t2 = System.nanoTime();
			System.out.println("Device: "+ d.name + " - computing bdds takes: " + (t2 - t1)/1000000000.0 + "ns");
		}
			
		ArrayList<Integer> fwdbddary = new ArrayList<Integer> ();
		ArrayList<Integer> fwdbddaryNeg = new ArrayList<Integer> ();
		
		for(Device d : devices.values())
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
		
		/**
		 * memory usage
		 */
		
		long memoryUsage =0;
		for(int BDD: fwdbddary)
		{
			memoryUsage = memoryUsage + bddengine.getBDD().nodeCount(BDD) * 24;
		}
		System.out.println("memory cost: "+ memoryUsage/1000000.0 + " MB");
		
		/**
		 * compute AP
		 */
		fwdapc = new APComputer(fwdbddary, bddengine);
		
		
		/**
		 * generate traces
		 */
		
		Trace trace = new Trace(fwdapc.getAllAP(),bddengine, enableAPWeight, this.name);
		System.out.println("tracesize: "+trace.getTrace().size());


		/**
		 * AP Tree construction
		 */
			
		long t1 = System.nanoTime();

		/**
		 * in order construction, predicates are placed in the tree in original order
		 * 	
		 */
			generatingTree = new Tree(bddengine);
			generatingTree=generatingTree.treeGeneration(fwdbddary, fwdbddaryNeg, generatingTree);

		
		long t2 = System.nanoTime();
		
		System.out.println("Construction of generatingTree takes " + (t2 - t1)/1000000.0 + "ms.");
		
		if(enableTreePrinting){
			generatingTree.preOrder(generatingTree.getRoot());
			generatingTree.sumDepthPrint("generatingTree");
		}	
			
			
		/**
		 * Optimized tree construction to minimize the average depth of the leaves
		 * 	
		 */
		long t3 = System.nanoTime();
		ReconstructionTree reconstructedTree = new ReconstructionTree(fwdbddary,fwdbddaryNeg,generatingTree,fwdapc.getPredicateAPREP(),bddengine);
		reconstrcutingTree = reconstructedTree.reconstructionTree();
		long t4 = System.nanoTime();
		
		System.out.println("Reconstruction of generatingTree takes " + (t4 - t3)/1000000.0 + "ms.");
		
		if(enableTreePrinting){
			reconstrcutingTree.preOrder(reconstrcutingTree.getRoot());
			reconstrcutingTree.sumDepthPrint("reconstrcutingTree");
		}
		
		
		/***
		 * throughput, queries per second
		 * A trace is evaluated starting from the root, until a leave is found.
		 *
		 */
		System.out.print("Query the generatingTree - ");
		generatingTree.Search(trace.getTrace(), generatingTree.getRoot());
		System.out.print("Query the reconstrcutingTree - ");
		reconstrcutingTree.Search(trace.getTrace(), reconstrcutingTree.getRoot());

		if(enableAPWeight){
						
//			for(int BDD: trace.APWeightList.keySet()){
//				System.out.println(trace.APWeightList.get(BDD));
//			}
		
			ReconstructionTreewithAPWeight reconstructedTreewithAPWeight =  new ReconstructionTreewithAPWeight(fwdbddary,fwdbddaryNeg,
			generatingTree,fwdapc.getPredicateAPREP(),bddengine, trace.APWeightList);
	
			Tree reconstrcutingTreewithAPWeight = reconstructedTreewithAPWeight.reconstructionTree();
			reconstrcutingTreewithAPWeight.preOrder(reconstrcutingTreewithAPWeight.getRoot());
			reconstrcutingTreewithAPWeight.sumDepthPrint("reconstrcutingTreewithAPWeight");
			reconstrcutingTreewithAPWeight.Search(trace.getTrace(), reconstrcutingTreewithAPWeight.getRoot());
		}
	
		
		/***
		 * topology information
		 */
		topology = new HashMap<PositionTuple, PositionTuple>();
		
		addTopology("chic","xe-0/1/0","newy32aoa","xe-0/1/3");
		addTopology("chic","xe-0/0/1","kans","xe-0/1/0");
		addTopology("chic","xe-1/1/3","wash","xe-6/3/0");
		addTopology("hous","xe-3/1/0","losa","ge-6/0/0");
		addTopology("kans","ge-6/0/0","salt","ge-6/1/0");
		addTopology("chic","xe-1/1/2","atla","xe-0/1/3");
		addTopology("seat","xe-0/0/0","salt","xe-0/1/1");
		addTopology("chic","xe-2/0/1","kans","xe-0/0/3");
		addTopology("hous","xe-1/1/0","kans","xe-1/0/0");
		addTopology("seat","xe-0/1/0","losa","xe-0/0/0");
		addTopology("salt","xe-0/0/1","losa","xe-0/1/3");
		addTopology("seat","xe-1/0/0","salt","xe-0/1/3");
		addTopology("newy32aoa","et-2/","wash","et-3/0/0-0");
		addTopology("newy32aoa","et-3/0/0-1","wash","et-3/0/0-1");
		addTopology("chic","xe-1/1/1","atla","xe-0/0/0");
		addTopology("losa","xe-0/1/0","seat","xe-2/1/0");
		addTopology("hous","xe-0/1/0","losa","ge-6/1/0");
		addTopology("atla","xe-0/0/3","wash","xe-1/1/3");
		addTopology("hous","xe-3/1/0","kans","ge-6/2/0");
		addTopology("atla","ge-6/0/0","hous","xe-0/0/0");
		addTopology("chic","xe-1/0/3","kans","xe-1/0/3");
		addTopology("losa","xe-0/0/3","salt","xe-0/1/0");
		addTopology("atla","ge-6/1/0","hous","xe-1/0/0");
		addTopology("atla","xe-1/0/3","wash","xe-0/0/0");
		addTopology("chic","xe-2/1/3","wash","xe-0/1/3");
		addTopology("atla","xe-1/0/1","wash","xe-0/0/3");
		addTopology("kans","xe-0/1/1","salt","ge-6/0/0");
		addTopology("chic","xe-1/1/0","newy32aoa","xe-0/0/0");
	
		
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
		Network n = new Network("i2");
	
	}

}
