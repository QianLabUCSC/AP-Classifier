package i2analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import RuleSetPreproccessing.BDDACLWrapper;
import stanalysis.APComputer;
import stanalysis.PositionTuple;


public class ComputingBehaviors {
	
	HashMap<PositionTuple, PositionTuple> topology;
	traceAP trace;
	Set<String> boxes;
	Tree APTree;
	
	
	/**
	 * this class computes network wide behaviors of a packet given an AP and its ingress
	 */
	
	public ComputingBehaviors(HashMap<PositionTuple, PositionTuple> inputTopo,  traceAP inputTrace, Set<String> boxNames, Tree tree){
		
		topology = inputTopo;
		trace = inputTrace;
		boxes = boxNames;
		APTree = tree;
	}
		
	public void ComputingBehaviorsofPackets(){

		for(String boxName: boxes){
						
			String currentBoxName = boxName;
			String currentPort = trace.getBoxPort_map().get(currentBoxName);
			
			String boxTemp = currentBoxName;
			
			while(currentPort!=null){
				PositionTuple currentPosTuple  = new PositionTuple(currentBoxName,currentPort);
			
				//re-search the tree for some certain boxes
				if(currentBoxName == "atla"){
					APTree.Search(trace.getAtomicPredicate(), APTree.getRoot());
				}
				
				PositionTuple nextPosTuple  =  topology.get(currentPosTuple);
					
					
					if(nextPosTuple!=null && nextPosTuple.getDeviceName() != boxTemp){
						boxTemp = currentBoxName;
						currentBoxName = nextPosTuple.getDeviceName();
						currentPort = trace.getBoxPort_map().get(currentBoxName);
						
						//System.out.println("boxName: "+currentBoxName+ "  portName  " +  currentPort);
					}
					else
						break;
			}
			
		}
	}
	
	
	public static void main(String[] args) throws IOException
	{
		Network n = new Network("i2");
		
		HashSet<Integer> AP = n.getFWDAPC().getAllAP();
		BDDACLWrapper BDDengine = n.getbddEngine();
		HashMap<String, Device> devices = n.getDevices();
	
		//generating trace, each trace belongs to an AP, AP is presented in <box, port> pair
		ArrayList<traceAP> trace = new ArrayList<traceAP>(); 
		for(int BDD: AP){
			trace.add(new traceAP(BDD, new HashMap<String, String>()));
		}

		//fill information for traceAP <AP, <box, port>>
		for(traceAP oneTrace: trace){
			for(String boxName : devices.keySet()){
					for(int predicate: devices.get(boxName).get_bddPorts_map().keySet()){
						int BDDtemp = BDDengine.getBDD().and(predicate,oneTrace.getAtomicPredicate());
						 BDDengine.getBDD().ref(BDDtemp);
						 if(BDDtemp != BDDACLWrapper.BDDFalse){
							 oneTrace.getBoxPort_map().put(boxName, devices.get(boxName).get_bddPorts_map().get(predicate));
						 }
					}
			}
			
		}
		
		
	/*	for(traceAP oneTrace: trace){
			System.out.println("!#$!@#$!@#$!@#$  ");
			System.out.println("APBDD: "+oneTrace.getAtomicPredicate());
			for(String boxName : oneTrace.getBoxPort_map().keySet()){
				System.out.println("boxName: "+boxName+ "  portName  " +  oneTrace.getBoxPort_map().get(boxName));
			}
		}*/
		
		long t1 = System.nanoTime();
		
		for(traceAP oneTrace:trace){
			ComputingBehaviors ComputingBehaviors = new  ComputingBehaviors(n.get_topology(),  oneTrace, devices.keySet(), n.getReconstrcutingTree());
			for(int cycle=0;cycle<5000;cycle++){
				ComputingBehaviors.ComputingBehaviorsofPackets();
			}
			
		}
		long t2 = System.nanoTime();
		
		System.out.println("throughput of computing behaviors " + trace.size()*5000*devices.keySet().size()*1000.0/(t2-t1) + "M/s");

	}
	
}
