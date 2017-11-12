package i2analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ArrayOP {
	
	static HashSet<Integer> conjunction(HashSet<Integer> operand1,HashSet<Integer> operand2){
		
		if(operand2.size()==1){
			int BDDinOperand2 = 0;
			for(int BDD: operand2){
				BDDinOperand2 = BDD;
			}
			HashSet<Integer> set = new HashSet<Integer>();
			if(operand1.contains(BDDinOperand2)){
				set = operand2;
				return set;
			}
			else return set;
			
	
		}
		else{
		
		HashSet<Integer> set = new HashSet<Integer>();
		for(int i:operand1){
			set.add(i);
		}
		
		HashSet<Integer> conjunctionSet = new HashSet<Integer>();
				for(int i:operand2){
					if(!set.add(i))
						conjunctionSet.add(i);
				}
	        return conjunctionSet;
		}

		    }

	static ArrayList<Integer> subtraction(ArrayList<Integer> operand1, ArrayList<Integer> operand2){
		
		
		ArrayList<Integer> set = new ArrayList<Integer>();
			
			if(operand2.size()==0){
				return set;
			}
			else{
			for(int i:operand1){
				set.add(i);
			}
			for(int i:operand2){
			
				set.remove(set.indexOf(i));
			}
			
	        return set;
		    }
	}
	
	static ArrayList<Integer> conjunctionSort(ArrayList<Integer> operand1, ArrayList<Integer> operand2){
		
		if(operand2.size()==1){
			int BDDinOperand2 = operand2.get(0);
		
			ArrayList<Integer> set = new ArrayList<Integer>();
			if(operand1.contains(BDDinOperand2)){
				set = operand2;
				return set;
			}
			else return set;
		}
		else{
			int[] operand1Array = new int[operand1.size()];
			int[] operand2Array = new int[operand2.size()];
			
			for(int i=0;i<operand1.size();i++ ){
				operand1Array[i] = operand1.get(i); 
			}
			
			for(int j=0;j<operand2.size();j++ ){
				operand2Array[j] = operand2.get(j); 
			}
	
//			Arrays.sort(operand1Array);
//			Arrays.sort(operand2Array);
			int[] conjunctionArray = intersect(operand1Array, operand2Array);
			ArrayList<Integer> set = new ArrayList<Integer>();
			if(conjunctionArray.length==0){
				return set;
			}
			else{
			for(int index=0;index<conjunctionArray.length;index++){
				set.add(conjunctionArray[index]);
			}
			return set;
			}
			
		}
		}
	
	 static int[] intersect(int[] a, int[] b) {
		 	if (a[0] > b[b.length - 1] || b[0] > a[a.length - 1]) {
	             return new int[0];
	        }
	        int[] intersection = new int[Math.max(a.length, b.length)];
	        int offset = 0;
	        int s=0;
	        for (int i = 0; i < a.length & s < b.length; i++) {
	        	while (a[i] > b[s] & s < b.length-1) {
	                 s++;
	                 
	            }
	            if (a[i] == b[s]) {
	                 intersection[offset++] = b[s++];
	            }
//	            while (i < (a.length - 1) && a[i] == a[i + 1]) {
//	                i++;
//	            }
	        }
	        
	        
	        
	        if (intersection.length == offset) {
	            return intersection;
	        }
	        int[] duplicate = new int[offset];
	        System.arraycopy(intersection, 0, duplicate, 0, offset);
	        return duplicate;
	    }
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Integer> a = new ArrayList<Integer>();
		ArrayList<Integer> b = new ArrayList<Integer>();
		int x1=1;int x2=2;int x3=3;int x4=4;int x5=5;int x6=6;int x7=7;
		
		
		a.add(x2);a.add(x3);a.add(x6);a.add(x5);a.add(x4);
		b.add(x7);b.add(x1);b.add(x4);b.add(x1);
		ArrayList<Integer> c= conjunctionSort(a,b);
		for(int i: c){
			System.out.println(i);
		}
		System.out.println(" ");
		ArrayList<Integer> d= subtraction(a,c);
		for(int i: d){
			System.out.println(i);
		}
		
		
		
//		ArrayList<Integer> intlist = new ArrayList<Integer> ();
//		intlist.add(5);
//		intlist.add(4);
//		intlist.add(2);intlist.add(9);intlist.add(8);
//		for(int i=0;i<intlist.size();i++){
//			System.out.println(intlist.get(i));
//		}
//		Collections.sort(intlist);
//		for(int i=0;i<intlist.size();i++){
//			System.out.println(intlist.get(i));
//		}
	
	}

}
