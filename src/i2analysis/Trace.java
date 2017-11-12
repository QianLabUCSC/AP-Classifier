package i2analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import RuleSetPreproccessing.BDDACLWrapper;


public class Trace {
	ArrayList<Integer> trace;

	public HashMap<Integer, Integer> APWeightList;
	
	private static final boolean enableWrTraceToFile = false;
	private boolean enableParetoI2 = false;
	private boolean enableParetoSt = false;
	
	int[] I2Trace = {108, 36, 18, 11, 8, 6, 4, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1};
	//with number of packets from 1 to 23
	int[] StTrace = {247, 83, 42, 25, 17, 12, 9, 7, 6, 5, 4, 4, 3, 3, 3, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

	/**
	 * when APs are equally treated, traces are generated uniformly with respect to the set of AP. 
	 * when APs are assigned with weights, traces are generated under Pareto distribution
	 */
	
	
	
	public Trace(HashSet<Integer> AP, BDDACLWrapper bddengine, Boolean enableAPWeight, String name){
		
		if(enableAPWeight && name == "i2"){
			enableParetoI2 = enableAPWeight;
			enableParetoSt = !enableAPWeight;
		}
		else if(enableAPWeight && name == "st")
		{
			enableParetoSt = enableAPWeight;
			enableParetoI2 = !enableAPWeight;
		}
		
		ArrayList<Integer> APList = new ArrayList<Integer>();
		for(int BDD:AP){
			APList.add(BDD);
		}
		
		trace = new ArrayList<Integer>();
		APWeightList = new HashMap<Integer, Integer>();
		
//		try {
//		File writename1 = new File("C:\\Users\\huazhe\\Dropbox\\codes\\APClassifier\\StTrace.txt"); 
//		writename1.createNewFile(); 
//		BufferedWriter out1 = new BufferedWriter(new FileWriter(writename1));
		
		if(enableParetoI2){
			int traceArrayIndex=0;
			int traceNumber=1;
			int traceCounter=1;
			for(int i=0;i<APList.size();i++ ){
				int APIndex = i+70;
				if(APIndex>=APList.size()){
					APIndex=APIndex-APList.size();
				}
				int oneTrace = bddengine.getBDD().oneSat(APList.get(APIndex));
				APWeightList.put(APList.get(APIndex), (traceNumber-1));
				
				if(traceCounter <= I2Trace[traceArrayIndex]){
				
					for(int j=0; j<traceNumber;j++){
						trace.add(oneTrace);
					}
					traceCounter++;	
				}
				else{
					traceCounter=1;
					traceArrayIndex++;
					traceNumber++;
					for(int j=0; j<traceNumber;j++){
						trace.add(oneTrace);
					}
					traceCounter++;	
				}
								
//					if (enableWrTraceToFile) {
//						int [] bitFormTrace = null;
//						bitFormTrace = bddengine.getBDD().oneSat(APList.get(i), bitFormTrace);
//						//String bitFormTraceString = Arrays.toString(bitFormTrace);
//						for(int bitFormTraceStringIndex=0; bitFormTraceStringIndex<bitFormTrace.length;bitFormTraceStringIndex++){
//							int oneBit = bitFormTrace[bitFormTraceS//import jdd.bdd.debug.DebugBDD;tringIndex];
//							if(oneBit == -1){
//								out1.write("x");
//							}
//							else if(oneBit == 0){
//								out1.write("0");
//							}
//							else out1.write("1");
//						}
//						out1.newLine();
//						
//						}

			}
		}
		else if(enableParetoSt){
			
			int traceArrayIndex=0;
			int traceNumber=1;
			int traceCounter=1;
			for(int i=0;i<APList.size();i++ ){
				int APIndex = i+480;
				if(APIndex>=APList.size()){
					APIndex=APIndex-APList.size();
				}
				int oneTrace = bddengine.getBDD().oneSat(APList.get(APIndex));
				APWeightList.put(APList.get(APIndex), (traceNumber-1));
				
		
				
				if(traceCounter <= StTrace[traceArrayIndex]){
				
					for(int j=0; j<traceNumber;j++){
						trace.add(oneTrace);
					}
					traceCounter++;	
				}
				else{
					traceCounter=1;
					traceArrayIndex++;
					traceNumber++;
					for(int j=0; j<traceNumber;j++){
						trace.add(oneTrace);
					}
					traceCounter++;	
				}
			}
		}
		else{
			
			for(int i=0;i<APList.size();i++ ){

				int oneTrace = bddengine.getBDD().oneSat(APList.get(i));
				trace.add(oneTrace);
			}
		}
		
	
		
//		if (enableWrTraceToFile) {
//			out1.flush(); 
//		
//			out1.close();
//		}
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
	
	public ArrayList<Integer> getTrace(){
		return trace;
	}
	
	HashMap<Integer, Integer> getAPWeightList(){
		return APWeightList;
	}
}
