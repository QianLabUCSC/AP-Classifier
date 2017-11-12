package i2analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jdd.bdd.BDD;
import stanalysis.APComputer;
import RuleSetPreproccessing.BDDACLWrapper;

/**
 * reconstruct a tree, call back to update the tree. 
 * Weight of APs are considered in construction
 * 
 */


public class ReconstructionTreewithAPWeight implements Runnable{
	 ArrayList<Integer> inputBDDList;
	 ArrayList<Integer> intputBDDListNeg;
	 HashSet<Integer> APs;
	 Callback c1;
	 Node constructedTreeRoot;
	 

	 Vertex vertexPrior;
	 Tree constructedTree;
	 ArrayList<Predicate> predicateAPRE;
	 ArrayOP arrayOP;
	 HashMap<Integer, Integer> APWeight;
	

	 /**
	  * reconstruct a tree using all predicates
	  */
	 
	 public ReconstructionTreewithAPWeight(ArrayList<Integer> PredicateBDDREP, ArrayList<Integer> PredicateBDDREPNeg, 
			 Tree generatingTree, ArrayList<Predicate> PredicateAPREP,BDDACLWrapper BBDengine, 
			 HashMap<Integer, Integer> APWeightList){
		 
		 inputBDDList = PredicateBDDREP;
		 intputBDDListNeg = PredicateBDDREPNeg;
		 predicateAPRE = PredicateAPREP;
		 APWeight = APWeightList;
		 
		 APs = generatingTree.getAPs();

		 //c1 = th1;
		 constructedTree = new Tree(BBDengine);
		// constructedTreeRoot = null;
		 constructedTree.leafNodes = new ArrayList<Node>();
	 }
	 
	 
	
	 

	 

	 public void run(){
	
		 reconstructionTree();
		// notify(....)
		 c1.callback(constructedTree);
		 
	}
	 
	 
		/**
		 * a heuristic to construct a tree with minimized sum of leave depths
		 * @return a reconstructed tree
		 */
		public Tree reconstructionTree(){
			
			ArrayList<Predicate> PredicateBDDREPList= new ArrayList<Predicate>();
				
		
			
			for(Predicate predicate:predicateAPRE ){
				PredicateBDDREPList.add(predicate);
			}
			//System.out.println("size of PredicateBDDREPList " + PredicateBDDREPList.size());
			
			ArrayList<Integer> APunion = new ArrayList<Integer>();
			for(int BDD:APs){
				APunion.add(BDD);
			}
			Collections.sort(APunion);
			
			
			int minSumLevel = minAPsSumDepth(PredicateBDDREPList, APunion, constructedTree.getRoot(), 0);
			
			
			//System.out.println("end of reconstruction tree, calculation of minsumlevel "+minSumLevel);
						
			return constructedTree;
			
			
		}
		
		
		
		
		/**
		 * construct an optimized tree with minimum leave depth
		 * @param predicates
		 * @return sum depth
		 * an optimized tree is constructed
		 * 
		 * At each internal node, a predicate which will achieve minimum leave depth is selected to place in the tree
		 * F(S)=|S|+F(A)+F(\neg A)
		 * 
		 */
		public int minAPsSumDepth(ArrayList<Predicate> PredicateBDDREPList, ArrayList<Integer>  APunion, Node node, int depth){  
			
			//APunion only has one AP
			if(APunion.size()==1){
				node.leafNode(APunion.get(0));
				constructedTree.leafNodes.add(node);
			//	System.out.println("only one predicate left, the depth of the node is "+ node.getDepth() +" "+node.getBDD());	
				return 0;
			}
			
			//System.out.println("size of "+ PredicateBDDREPList.size());	
			
			
			int vertexPriorInitialed = 0;
			for (int index=0; index< PredicateBDDREPList.size(); index++){
				
				ArrayList<Integer> temp = ArrayOP.conjunctionSort(APunion,PredicateBDDREPList.get(index).getAPset());
				ArrayList<Integer> tempNeg = ArrayOP.subtraction(APunion , temp);

	
				if(!(temp.isEmpty()) & !(tempNeg.isEmpty())){
					
									
					int NumPostive=0;
					int NumNegative=0;
					
					for(int BDD: temp){
						NumPostive = NumPostive + APWeight.get(BDD);
					}
					
					for(int BDD: tempNeg){
						NumNegative = NumNegative + APWeight.get(BDD); 
					}
					
					
					
																
					if(vertexPriorInitialed==0){	
						vertexPrior = new Vertex(PredicateBDDREPList.get(index), temp,tempNeg, NumPostive, NumNegative,index );
						vertexPriorInitialed = 1;
					//only the new added vertex
					}
					else{
						
						ArrayList<Integer> Oldtemp = vertexPrior.getTemp();
						ArrayList<Integer> OldtempNeg = vertexPrior.getTempNeg();
						int OldNumPositive = vertexPrior.getNumPositive();



							//for oldPredcate A, and new Predicate B
							
							//case 1, A belongs to B, it will not change due S
							if(ArrayOP.conjunctionSort(Oldtemp, tempNeg).isEmpty()){
								if(OldNumPositive < NumNegative){
									
									vertexPrior.vertexExchange(PredicateBDDREPList.get(index), temp,tempNeg, NumPostive, NumNegative,index);
						
								}
								else{
								
									
								}
								
							}
							//case 2, B belongs to A
							else if(ArrayOP.conjunctionSort(temp, OldtempNeg).isEmpty()){
								if(OldNumPositive > NumNegative){
									vertexPrior.vertexExchange(PredicateBDDREPList.get(index), temp, tempNeg, NumPostive, NumNegative ,index);
										
								}
								else{
								
									
								}
								
							}
							//case 3, A and B are disjointed
							else if(ArrayOP.conjunctionSort(temp, Oldtemp).isEmpty()){
								if(OldNumPositive < NumPostive){
									vertexPrior.vertexExchange(PredicateBDDREPList.get(index), temp,tempNeg, NumPostive, NumNegative ,index);
								
								}
								else{
						
								}
								
							}
							//case 4, A and B overlaps,but not belongs
							else{
					
							}
							
						}	
					  //end of add this predicate into the graph
					
				}
				else{

					}
			} //end of constructing graph 
			
			Predicate predicateTobeAdded = vertexPrior.getPredicate();
			int index = vertexPrior.getIndex();
						
			ArrayList<Integer> conjucntion = vertexPrior.getTemp();
			ArrayList<Integer> conjucntionNeg = vertexPrior.getTempNeg();
			int BDD = inputBDDList.get(inputBDDList.indexOf(predicateTobeAdded.getBDD()));
			int BDDNeg = intputBDDListNeg.get(inputBDDList.indexOf(predicateTobeAdded.getBDD()));
						
			constructedTree.addNewNodeDirectly(BDD, BDDNeg, depth+1, node);


			
			//delete this node from the input predicate list
			PredicateBDDREPList.remove(index);
			
			//F(S)=|S|+F(A)+F(not A);
			int SumLevel =  APunion.size()+minAPsSumDepth(PredicateBDDREPList, conjucntion, node.leftChild, depth+1)+minAPsSumDepth(PredicateBDDREPList, conjucntionNeg, node.rightChild, depth+1);


			PredicateBDDREPList.add(predicateTobeAdded);
						
			return SumLevel;
				
		}
		


		

}
