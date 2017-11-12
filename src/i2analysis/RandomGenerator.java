package i2analysis;

import java.util.ArrayList;
import java.util.Random;

public class RandomGenerator implements Comparable<RandomGenerator>{
	int order;
	int randomValue;
	
	public RandomGenerator(int key, int Value){
		order = key;
		randomValue = Value;
	}
	
	
	public int compareTo(RandomGenerator another_random) {
        return this.randomValue - another_random.randomValue; //sort in ascending order
        //return another_random.randomValue - this.randomValue;
    }
	
	public int getOrder(){
		return order;
	}
	
	public int getValue(){
		return randomValue;
	}
	

}
