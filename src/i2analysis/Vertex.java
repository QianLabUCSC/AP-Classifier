package i2analysis;

import java.util.ArrayList;
import java.util.HashSet;

public class Vertex {

	int APNumBDDConjunction;
	int APNumBDDNegConjunction;
	int vertexCount;
	ArrayList<Integer> temp;
	ArrayList<Integer> tempNeg;
	Predicate predicate;
	int index;

	
	public Vertex(Predicate Predicate, ArrayList<Integer> Temp, ArrayList<Integer> TempNeg, int APNumConjunction, int APNumNegConjunction, int Index){
		predicate = Predicate;
		temp=Temp;
		tempNeg = TempNeg;
		APNumBDDConjunction = APNumConjunction;
		APNumBDDNegConjunction = APNumNegConjunction;
		vertexCount = 0;
		index = Index;
		
		
		//BDDArray = new ArrayList<Vertex>();
		
	}
	

	
	public void vertexExchange(Predicate Predicate,ArrayList<Integer> Temp, ArrayList<Integer> TempNeg, int APNumConjunction, int APNumNegConjunction, int Index){
		predicate = Predicate;
		temp= Temp;
		tempNeg = TempNeg;
		APNumBDDConjunction = APNumConjunction;
		APNumBDDNegConjunction = APNumNegConjunction;
		index = Index;
		
	}
	
	public void countAddOne(){
		
		vertexCount = vertexCount+1;
	}
	
	public int getBDDArraySize(){
		return vertexCount;
	}

	
	
	public int getNumPositive(){
		return APNumBDDConjunction;
	}
	
	public int getNumNegative(){
		return APNumBDDNegConjunction;
	}

	
	
	public ArrayList<Integer> getTemp(){
		return temp;
	}
	public ArrayList<Integer> getTempNeg(){
		return tempNeg;
	}
	
	public Predicate getPredicate(){
		return predicate;
	}
	public int getIndex(){
		return index;
	}
}
