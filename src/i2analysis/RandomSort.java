package i2analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;



/* sort a set of integers randomly */
public class RandomSort {
	

	ArrayList<RandomGenerator> RandomG;
	
	
	public RandomSort(){
	 RandomG = new ArrayList<RandomGenerator>();
	}
    

	
	public ArrayList<RandomGenerator> RandomListGen(int length){
		Random randomGenerator = new Random();
		
		
		  for (int idx = 0; idx < length; idx++){
		      int randomInt = randomGenerator.nextInt(length*1000);
		      
		      //System.out.println("the "+idx +" random is "+ randomInt);
		      
		      RandomGenerator RG = new RandomGenerator(idx, randomInt);
		      RandomG.add(RG);
		      
		    }
		  
		Collections.sort(RandomG);  
		
	/*	for(RandomGenerator t : RandomG){
			System.out.println(t.getOrder() +", " + t.getValue());
			
		}*/
		
		  //sort
		//System.out.println("size of RandomG is  "+ RandomG.size());
		return RandomG;
	}
    
    
    
  
}
    