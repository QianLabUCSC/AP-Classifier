package stanalysis;

import i2analysis.Tree;
import i2analysis.traceAP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import RuleSetPreproccessing.BDDACLWrapper;


	public class ComputingBehaviorsAPT {
		
		HashMap<PositionTuple, PositionTuple> topology;
		traceAP trace;
		Set<String> boxes;
		Tree APTree;
		
		
	/**
	 * 
	 *
	 * @param inputTopo
	 * @param inputTrace
	 * @param boxNames
	 * @param tree
	 */
		
		public ComputingBehaviorsAPT(HashMap<PositionTuple, PositionTuple> inputTopo,  traceAP inputTrace, Set<String> boxNames, Tree tree){
			
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
				
//					if(currentBoxName == "atla"){
//						APTree.Search(trace.getAtomicPredicate(), APTree.getRoot());
//					}
					
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
			NetworkAPT n = new NetworkAPT("st");
			
			HashSet<Integer> AP = n.getFWDAPC().getAllAP();
			BDDACLWrapper BDDengine = n.getBDDEngine();
			HashMap<String, DeviceAPT> devices = n.getDevices();
		
			
			ArrayList<traceAP> trace = new ArrayList<traceAP>(); 
			for(int BDD: AP){
				trace.add(new traceAP(BDD, new HashMap<String, String>()));
			}
			
			

			
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
			
			
			for(traceAP oneTrace: trace){
				System.out.println("!#$!@#$!@#$!@#$  ");
				System.out.println("APBDD: "+oneTrace.getAtomicPredicate());
				for(String boxName : oneTrace.getBoxPort_map().keySet()){
					System.out.println("boxName: "+boxName+ "  portName  " +  oneTrace.getBoxPort_map().get(boxName));
				}
			}
			
//			long t1 = System.nanoTime();
//			
//			for(traceAP oneTrace:trace){
//				ComputingBehaviorsAPT ComputingBehaviors = new  ComputingBehaviorsAPT(n.get_topology(),  oneTrace, devices.keySet(), n.getReconstrcutingTree());
//				for(int cycle=0;cycle<100;cycle++){
//					ComputingBehaviors.ComputingBehaviorsofPackets();
//				}
//				
//			}
//			long t2 = System.nanoTime();
//			
//			System.out.println("throughput of computing behaviors " + trace.size()*100*devices.keySet().size()*1000.0/(t2-t1) + "M/s");
			
			
		
		}
		
	}
	

