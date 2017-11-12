package i2analysis;

import java.util.HashMap;

import stanalysis.APComputer;

public class traceAP {
	
	int atomicPredicate;
	HashMap<String, String> boxPort;
	
	public traceAP(int ap, HashMap<String, String> boxPortMap)
	{
		atomicPredicate = ap;
		boxPort = boxPortMap;
	}
	
	public int getAtomicPredicate()
	{
		return atomicPredicate;
	}
	
	
	public HashMap<String, String> getBoxPort_map()
	{
		return boxPort;
	}
	

}
