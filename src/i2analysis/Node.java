package i2analysis;

import java.io.*;
import java.util.*;

import jdd.bdd.BDD;
import RuleSetPreproccessing.*;


public class Node {
	int nodeDepth;
	int nodeBDD;
	int nodeRealBDD;
	boolean is_leaf;
	ArrayList<Integer> nodeRealBDDAPExp;
	
	
	public Node leftChild ;
	public Node rightChild ;
	
	
	public Node (int BDD, int realBDD, int depth) {
		
		nodeBDD = BDD;
		nodeRealBDD = realBDD;
		nodeDepth = depth;
		is_leaf = true;
		
		
	}
	
	public Node (int BDD, int depth) {
		
		nodeBDD = BDD;
		nodeDepth = depth;
		is_leaf = true;
		
	}
	
	public Node (int BDD, ArrayList<Integer> APset, int depth) {
		
		nodeBDD = BDD;
		nodeDepth = depth;
		is_leaf = true;
		nodeRealBDDAPExp = APset;
	}
	
	/**
	 * Insert a new P into this node, if new leaf nodes are constructed , return true;
	 */
	public boolean addNewNode(int toAdd,int toAddNeg, BDDACLWrapper bddengineTree , int depth) { 
	
		BDD treebdd = bddengineTree.getBDD();
		
	
		int treetmpCon = treebdd.and(toAdd, nodeRealBDD);  // A and B
		treebdd.ref(treetmpCon);
		
		int treetmpConNeg = treebdd.and(toAddNeg, nodeRealBDD); //(neg A) and B
		treebdd.ref(treetmpConNeg);
		
		if (treetmpCon == BDDACLWrapper.BDDFalse || treetmpConNeg == BDDACLWrapper.BDDFalse) {

			treebdd.deref(treetmpCon);
			treebdd.deref(treetmpConNeg);
			return false;
		}
		else {
			is_leaf = false;
			leftChild = new Node ( toAdd, treetmpCon , depth);
			rightChild = new Node ( toAddNeg, treetmpConNeg , depth);
			treebdd.deref(nodeRealBDD);
			//treebdd.deref(toAdd);
			//treebdd.deref(toAddneg);
		
			return true;
		}
	}

	public boolean addNewNode_APset(int toAdd,int toAddNeg, ArrayList<Integer> APset , int depth) { 
		
		
		ArrayList<Integer> temp = ArrayOP.conjunctionSort(this.getNodeRealBDDAPExp(), APset);
		ArrayList<Integer> tempNeg = ArrayOP.subtraction(this.getNodeRealBDDAPExp(), temp);
		
		if (temp.isEmpty() || tempNeg.isEmpty()) {

			return false;
		}
		else {
			is_leaf = false;
			leftChild = new Node ( toAdd , temp, depth);
			rightChild = new Node ( toAddNeg, tempNeg, depth);
			
			//treebdd.deref(toAdd);
			//treebdd.deref(toAddneg);
		
			return true;
		}
	}
	
	
	public void leafNode(int realBDD){
		nodeRealBDD = realBDD;
		}

		
	
	public boolean isLeaf() {
		return is_leaf;
	}
	
	public int getDepth() {
		return nodeDepth;
	}
	
	public int getBDD() {
		return nodeBDD;
	}
	
	public int getRealBDD() {
		return nodeRealBDD;
	}

	void setNodeRealBDDAPExp(ArrayList<Integer> APset){
		nodeRealBDDAPExp = APset;
	}
	
	ArrayList<Integer> getNodeRealBDDAPExp(){
		return nodeRealBDDAPExp;
	}
	

}
