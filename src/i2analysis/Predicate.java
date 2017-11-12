package i2analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/*
 * for each predicate, its parameters includes {device name, predicate, port name}
 */


public class Predicate implements Comparable<Predicate>{
	int BDD;
	ArrayList<Integer> APset;
	int APsetSize;
	
	public Predicate(int predicate, HashSet<Integer> AP ){
		BDD = predicate;
		APset= new ArrayList<Integer>();
		for(int BDD: AP){
			APset.add(BDD);
		}
		Collections.sort(APset);
		APsetSize = APset.size();

	}
	
	public int compareTo(Predicate another_Predicate) {
        return another_Predicate.APsetSize-this.APsetSize ; //sort in dscending order
        //return another_random.randomValue - this.randomValue;
    }
	
	public int getBDD(){
		return BDD;	
	}
	
	public ArrayList<Integer>  getAPset(){
		return APset;	
	}
	
	public int getAPsetSize(){
		return APsetSize;	
	}
	

}
