package i2analysis;

import java.io.*;
import java.util.*;

import jdd.util.*;
import jdd.bdd.*;
import RuleSetPreproccessing.*;
import i2analysis.Node;


public class Tree {
	Node root ;
	ArrayList<Node> leafNodes; 
	
	public BDDACLWrapper engine;
	
	HashMap<Integer, Integer> levelCount; //count the number of nodes on each tree level, <level,number>
	public int sumDepth; //total depth of all leaf nodes
	int No;  //index of leaf node
	
	HashSet<Integer> APs;
	public ArrayList<Integer> depthList; //list of depth of leaf nodes
	private int cycle=1000;
	int searchSumDepth=0;
	long searchTime;
	public double realTimeThroughput;
	

	
	public Tree (BDDACLWrapper engine_)
	{
		engine = engine_;
		root = new Node(BDDACLWrapper.BDDTrue, BDDACLWrapper.BDDTrue,  0);
		leafNodes = new ArrayList<Node>();
		leafNodes.add(root);
		levelCount = new HashMap<Integer, Integer>();
		depthList = new ArrayList<Integer>();
		sumDepth = 0;
		No = 0;
	
		
	}
	
	
	
	public synchronized void insert_new_P (int toAdd,int toAddNeg) {	
		ArrayList<Node> created_nodes = new ArrayList<Node>();
		for (Node n: leafNodes) {
			
			if (n.isLeaf()) {
				if (n.addNewNode(toAdd,  toAddNeg, this.engine,   n.getDepth()+1)  ) {
					created_nodes.add(n.leftChild);
					created_nodes.add(n.rightChild);
				}
				else
					created_nodes.add(n);
			}
		}
		leafNodes = created_nodes;
		//System.out.println("number of nodes in the tree " + nodes.size() );
		//merge  created_nodes and nodes	 
	}
	
	public HashSet<Integer> getAPs(){
		APs = new HashSet<Integer>();
		for(Node n: leafNodes){
			APs.add(n.getRealBDD());
		}
		
		return APs;
		
	}
	
	/**
	 * normal order generate tree
	 */
	public Tree treeGeneration(ArrayList<Integer> predicates, ArrayList<Integer> predicatesNeg, Tree generatingTree){
		
		for(int i= 0; i<predicates.size();i++){
			int BDD = predicates.get(i);
			int BDDNeg = predicatesNeg.get(i);
			generatingTree.insert_new_P(BDD, BDDNeg);	
			}
	
		return generatingTree;
		
	}
	/**
	 * descend order, 
	 */
	public Tree QuickSortTreeGeneration(ArrayList<Predicate> PredicateList, ArrayList<Integer> predicates, ArrayList<Integer> predicatesNeg, Tree generatingTree, HashSet<Integer> APs){
			
		Collections.sort(PredicateList);
		ArrayList<Integer> completeAPset = new ArrayList<Integer>();
		for(int BDD: APs){
			completeAPset.add(BDD);
		}
		Collections.sort(completeAPset);
	
 		for(Predicate predicate:PredicateList){
 			int BDD = predicate.getBDD();
 			int BDDNeg = predicatesNeg.get(predicates.indexOf(predicate.getBDD()));
 			
 			//generatingTree.insert_new_P(BDD, BDDNeg);
			
			ArrayList<Integer> predicateAPset = predicate.getAPset();
		
			
			this.root.setNodeRealBDDAPExp(completeAPset);
			generatingTree.insert_new_P_APset(predicateAPset,  BDD, BDDNeg);	
			}
	
		return generatingTree;
		
	}
	/**
	 * Using AP subset to decide whether a new node is built 
	 * 
	 */
	public void insert_new_P_APset (ArrayList<Integer> APset, int predicate, int predicateNeg) {	
		ArrayList<Node> created_nodes = new ArrayList<Node>();
		for (Node n: leafNodes) {
					
				if (n.isLeaf()) {
					if (n.addNewNode_APset(predicate,  predicateNeg, APset,   n.getDepth()+1)  ) {
						created_nodes.add(n.leftChild);
						created_nodes.add(n.rightChild);
					}
					else
						created_nodes.add(n);
				}
			}
			leafNodes = created_nodes;	 
	}
	
	
	
	public synchronized void preOrder(Node node){
		if(node == root){
			sumDepth=0;
			No=0;
		}	
		if(node !=null){
			
			//System.out.println("begin ");
			//bddengineTree.getBDD().printDot("c", root.getBDD());
			
			//System.out.println("end ");
			if(node.isLeaf()){
				//System.out.println(" the depth of"+ No +"th leaf node is" + node.getDepth());
				//bddengineTree.getBDD().printDot("c", node.getRealBDD());
				sumDepth = sumDepth + node.getDepth();
				No = No+1;
				depthList.add(node.getDepth());
			}
					
			preOrder(node.leftChild);
			preOrder(node.rightChild);	
		}
	}
	
	public synchronized void sumDepthPrint(String name){
		System.out.println(name+" has " + No+ " leaves.");
		System.out.println("Sum of leaf nodes depth is " + sumDepth);
		System.out.println("Average leaf depth is " + sumDepth/No*1.0);
		
}
	
	
	/**
	 * 	add two child nodes directly.
	 */

		public void addNewNodeDirectly(int toAdd, int toAddNeg,int depth, Node node){
			//engine.getBDD().ref(toAdd);
			//engine.getBDD().ref(toAddNeg);
			
			node.is_leaf = false;
			node.leftChild = new Node ( toAdd ,depth);
			node.rightChild = new Node ( toAddNeg ,depth);
			
		//	bddengineNode.getBDD().printDot("c", toAdd);
		//	bddengineNode.getBDD().printDot("d", toAddNeg);
			
		}
	
	
	
	
	
		/**
		 * searching the tree
		 * @param a trace packet and root
		 * @return a leave that evaluates to true
		 * 
		 * the trace is evaluated at each internal node, if it evaluates to true by the predicate, it goes to the left 
		 * child, or it goes to the right child
		 */
		public synchronized void Search(ArrayList<Integer> traceBDD, Node node){
			searchSumDepth=0;
			long t1 = System.nanoTime();
			for(int i=0;i<cycle;i++){
				for(int trace:traceBDD){
					if(node == null){
						System.out.println("the tree is empty" );	
					}
				
					Node p = node;
					BDD searchBDDEngine = this.engine.getBDD();
					
					while(p.isLeaf() == false){
						
						int leftConjunction = searchBDDEngine.and(p.leftChild.getBDD(),trace);
						searchBDDEngine.ref(leftConjunction);
						
						if( leftConjunction != BDDACLWrapper.BDDFalse){
							p= p.leftChild;
							searchBDDEngine.deref(leftConjunction);
							//System.out.println("choose left" );
						}
						else {
							p= p.rightChild;
							searchBDDEngine.deref(leftConjunction);
							//System.out.println("choose right" );
						}
					}
					//System.out.println("searchDepth "+ p.getDepth() );
					searchSumDepth = searchSumDepth +p.getDepth();
					
				}
			}
			
			long t2 = System.nanoTime();
			
			realTimeThroughput = traceBDD.size()*cycle*1000.0/(t2-t1);
			
			System.out.println("Query throughput: " + traceBDD.size()*cycle*1000.0/(t2-t1)+" Mqps");
//			System.out.println(searchSumDepth*1.0/cycle/traceBDD.size());
		}

		/**
		 * lookup the trace
		 */
		public synchronized void Search(int trace, Node node){
			searchSumDepth=0;
		//long t1 = System.nanoTime();
			for(int i=0;i<1;i++){
		
			if(node == null){
				System.out.println("the tree is empty" );	
			}
		
			Node p = node;
			BDD searchBDDEngine = this.engine.getBDD();
			
			while(p.isLeaf() == false){
				
				int leftConjunction = searchBDDEngine.and(p.leftChild.getBDD(),trace);
				searchBDDEngine.ref(leftConjunction);
				
				if( leftConjunction != BDDACLWrapper.BDDFalse){
					p= p.leftChild;
					searchBDDEngine.deref(leftConjunction);
					//System.out.println("choose left" );
				}
				else {
					p= p.rightChild;
					searchBDDEngine.deref(leftConjunction);
					//System.out.println("choose right" );
				}
			}
			//System.out.println("searchDepth "+ p.getDepth() );
			searchSumDepth = searchSumDepth +p.getDepth();
			
		
	}
	
//	long t2 = System.nanoTime();
	
//	realTimeThroughput = traceBDD.size()*cycle*1000.0/(t2-t1);
	
//	System.out.println("throughput " + traceBDD.size()*cycle*1000.0/(t2-t1)+" M/s");
//	System.out.println(searchSumDepth*1.0/cycle/traceBDD.size());
}


	
	/**
	 * count number of nodes on each level
	 */
	public void nodeCounter(int level)
	{
		if(levelCount.containsKey(level))
		{
			levelCount.put(level, levelCount.get(level) + 1);
		}else
		{
			levelCount.put(level, 1);
		}
	}
	

	
	
	public Node getRoot(){
	return root;
	}
	
	public int getSumDepth(){
		return sumDepth;
	}
	
	
	public double getRealTimeThroughput(){
		return realTimeThroughput;
	}
	
	public synchronized Tree referenceTree(Tree newTree, ArrayList<Integer> newAddingList,ArrayList<Integer> inputPredicateList,ArrayList<Integer> inputPredicateListNeg){
		
		for(int index = 0; index<newAddingList.size();index++){		
			int BDD = newAddingList.get(index);
			
			int BDDNeg = inputPredicateListNeg.get(inputPredicateList.indexOf(BDD));
			
			newTree.insert_new_P(BDD,BDDNeg);
	
		}	
		return newTree;
	}
	
	public synchronized Tree referenceTree(Tree newTree){
		return newTree;
	}


	public Node deepCopy(Node node){
		Node p = node;
		Node q = new Node(p.getBDD(), p.getDepth());
	
		if(p.is_leaf==false){
			q.leftChild = deepCopy(p.leftChild);
			q.rightChild = deepCopy(p.rightChild);
			q.is_leaf = false;
		}	
		return q;
	}	


	
}

