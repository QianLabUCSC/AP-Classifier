package i2analysis;

import java.util.ArrayList;
import java.util.Collection;

import jdd.bdd.BDD;
import RuleSetPreproccessing.BDDACLWrapper;

public class MultiFieldClassification {
	BDD BDD;
	Collection<Integer> singleBoxBDDSets;
	ArrayList<Integer> multiFieldClassificationTrace;

	public MultiFieldClassification(Collection<Integer> getfwbdds,
			BDDACLWrapper bddengine, Trace trace) {
		
		BDD = bddengine.getBDD();
		singleBoxBDDSets = getfwbdds;
		multiFieldClassificationTrace = trace.getTrace();
		
		
		int round = 1000;
		
		long startTime = System.nanoTime();
		
		for(int i=round; i>0; i--){
		
		for(int packet : multiFieldClassificationTrace){
			
			//int localCount = 0;
			for(int oneBDD : singleBoxBDDSets){
				int classifyResult = BDD.and(packet, oneBDD);
				BDD.ref(classifyResult);
				if(classifyResult != BDDACLWrapper.BDDFalse){
					BDD.deref(classifyResult);
					//System.out.println("hit BDD: " + localCount);
					break;
				}
				BDD.deref(classifyResult);
				//localCount++;
			}
			
		}
		}
		long endTime = System.nanoTime();
		long timeCost = endTime-startTime;
		System.out.println("number of BDDs: " + singleBoxBDDSets.size());
		System.out.println("multifield classification queryThroughput: " + round*multiFieldClassificationTrace.size()*1000.0/timeCost + " Mqps");
		
	}

}
