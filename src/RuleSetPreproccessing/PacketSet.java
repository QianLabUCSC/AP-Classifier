package RuleSetPreproccessing;
/*
 * PacketSet.java
 *
 */
import java.util.*;
import java.io.*;

public class PacketSet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3240983307241272863L;
	ArrayList<Tuple> tupleArray ;
	static int octetLength = 8 ;
	static final long octet1Multiplier = 16777216L ;
	static final int octet2Multiplier = 65536 ;
	static final int octet3Multiplier = 256 ;
	static final int octet4Multiplier = 1 ;
	final static long minIntegerIP = 0L ;
	/* 4294967295 is max value of an IP (255.255.255.255) in decimal value */
	final static long maxIntegerIP = 4294967295L ;
	final static long minPort = 0L;
	final static long maxPort = 65535L;
	final static long minProtocol = 0;
	final static long maxProtocol = 255;
	
	/**
	 * 
	 * @return the size of the packet set (in bytes)
	 */
	public long SizeOf()
	{
		return tupleArray.size() * Tuple.size;
	}
	
	/*** Creates a new instance of PacketSet ***/
	PacketSet() {
		tupleArray = new ArrayList<Tuple>();
	}
	void AddOneTuple(Tuple e)
	{
		tupleArray.add(e);
	}
	
	void Clean()
	{
		tupleArray.clear();
	}
	
	/**
	 * 
	 * @return the number of tuples in the packet set
	 * do not mix this function with the SizeOf function
	 */
	long Size()
	{
		return tupleArray.size();
	}
	
	/*** Set what to return when a function wants to display a PacketSet ***/
	public String toString (){
		String output= new String();
		for (int counter=0; counter<tupleArray.size();counter++)
			output = output + tupleArray.get(counter) + " \r\n ";
		return output;
	}

	
	/**
	 * check whether the acl rule is a permit rule or a deny rule
	 */
	static boolean CheckPermit(ACLRule aclr)
	{
		if(aclr.permitDeny.equalsIgnoreCase("permit"))
		{
			return true;
		}else
		{
			return false;
		}
	}
	/***********************************************************************
	 *
	 * Optimize a PacketSet
	 * - remove empty tuples
	 * - remove completely overlapping tuples
	 * - merge adjacent tuples
	 * - sort the tuples
	 *
	 *************************************************************************/
	boolean IsFalse()
	{
		//should be optimized first...
		return tupleArray.isEmpty();
	}
	static boolean OptimizePacketSet (PacketSet inputPS) {
		boolean success = false;
		// no need to optimize false packets
		// to be determined whether it is working
		if(inputPS.Size() == 0)
		{
			return false;
		}
		/***************************
		 * Remove any empty tuples in the PacketSet
		 ***************************/
		for (int i=0; i<inputPS.tupleArray.size(); i++) {
			Tuple currentTuple = (Tuple) inputPS.tupleArray.get(i);
			if (currentTuple.sourceIP.lower == -1) {
				inputPS.tupleArray.remove(i);
				i--;
				success = true;
			}
		}
		/****************************
		 * Loop through looking for overlapping tuples
		 ****************************/
		boolean toContinue;
		do {
			toContinue = false;
			/*** Use a double loop to process the elements in the PacketSet ***/
			for (int i=0; i<inputPS.tupleArray.size()-1; i++) {
				for (int j=i+1; j<inputPS.tupleArray.size(); j++) {
					Tuple tuple1 = (Tuple) inputPS.tupleArray.get(i) ;
					Tuple tuple2 = (Tuple) inputPS.tupleArray.get(j) ;
					int decJ = ProcessOverlapTuple(tuple1,tuple2,inputPS,i,j);
					if (decJ>=1) { // if there was a removal of any tuple
						toContinue = true;
						if (decJ==2) j--;
					}
				} // end of inner for loop
				if (toContinue) success = true;
			} // end of outer for loop
		} while (toContinue);
		/******************
		 * Sort the tuples
		 ******************/
		Object interimArray[] = inputPS.tupleArray.toArray();
		Arrays.sort(interimArray, new AComp());
		for (int i=0; i<interimArray.length; i++)
			inputPS.tupleArray.set(i,(Tuple)interimArray[i]);
		return success;
	}
	/***********************************************************************
	 *
	 * Check 2 tuples for overlaps
	 * Remove a tuple that is completely overlapped by another
	 * Merge adjacent tuples
	 * Do nothing about partially overlapping tuples
	 * (takes more storage otherwise)
	 *
	 ***********************************************************************/
	static int ProcessOverlapTuple (Tuple inputTuple1, Tuple inputTuple2,
			PacketSet outputPS, int i, int j) {
		Tuple outputTuple = new Tuple();
		/*** decJ is used to signal whether there was any successful removal
		 * or merging of tuples ***/
		int decJ = 0;
		/**************************************************************
		 * Check whether inputTuple1 and inputTuple2 have overlaps
		 * store new tuple in outputTuple
		 * return value is result
		 * Process tuples according to the return value (result)
		 **************************************************************/
		int result = CheckOverlapTuple (inputTuple1, inputTuple2, outputTuple);
		switch (result) {
		case 0 : break; // do nothing;
		/**********************************************************
		 * tuple1 completely covers tuple2. outputTuple can be used
		 *********************************************************/
		case 243 :
		case 405 :
		case 675 :
		case 1125 :
		case 1875 :
			outputPS.tupleArray.remove(j); // remove tuple2
			decJ=2;
			break;
			/**********************************************************
			 * tuple2 completely covers tuple1. outputTuple can be used
			 **********************************************************/
		case 1024 :
		case 1280 :
		case 1600 :
		case 2000 :
		case 2500 :
		case 3125:
			// remove tuple1
			outputPS.tupleArray.remove(i);
			// put tuple2 in tuple1's position
			outputPS.tupleArray.add(i,inputTuple2);
			// remove tuple2 from it's orginal position
			outputPS.tupleArray.remove(j);
			decJ=2;
			break;
			/**********************************************************
			 * 1 of the ranges in both tuples are adjacent and can be combined
			 * and the other ranges are exactly the same. outputTuple can be used
			 **********************************************************/
		case 1250 :
			outputPS.tupleArray.add(i,outputTuple);
			outputPS.tupleArray.remove(i+1);
			outputPS.tupleArray.remove(j);
			decJ=1;
			break;
			/**********************************************************
			 * at least 1 range in both tuples overlap partially
			 **********************************************************/
		default : decJ=0; // do nothing
		}
		return decJ ;
	}
	/***********************************************************************
	 *
	 * Function that checks all 5 ranges in each of the 2 tuples
	 * Return the result of the check
	 * As well as return the new tuple to outputTuple
	 *
	 ***********************************************************************/
	static int CheckOverlapTuple (Tuple inputTuple1,
			Tuple inputTuple2, Tuple outputTuple) {
		int result1 = CheckOverlap1Range(inputTuple1.sourceIP,
				inputTuple2.sourceIP, outputTuple.sourceIP) ;
		int result2 = CheckOverlap1Range(inputTuple1.sourcePort,
				inputTuple2.sourcePort, outputTuple.sourcePort) ;
		int result3 = CheckOverlap1Range(inputTuple1.destinationIP,
				inputTuple2.destinationIP, outputTuple.destinationIP) ;
		int result4 = CheckOverlap1Range(inputTuple1.destinationPort,
				inputTuple2.destinationPort, outputTuple.destinationPort) ;
		int result5 = CheckOverlap1Range(inputTuple1.protocol,
				inputTuple2.protocol, outputTuple.protocol);
		int output = 0;
		/***********************************************
		 * if output=1024, 1280, 1600, 2000, or 2500, and 3125,
		 * tuple2 completely covers tuple1.
		 * outputTuple can be used
		 * if output=243, 405, 675, 1125, or 1875,
		 * tuple1 completely covers tuple2.
		 * outputTuple can be used
		 * if output=1250, 1 of the ranges in both tuples are
		 * adjacent and can be combined
		 * and the other ranges are exactly the same.
		 * outputTuple can be used
		 * if output=1, all ranges in both tuples overlap partially.
		 * Need to use another function
		 * if output=0, at least 1 range does not overlap.
		 *************************************************/
		/*** Compact the return value ***/
		output = result1 * result2 * result3 * result4 * result5;
		return output;
	}
	/***********************************************************************
	 *
	 * Function that compares 2 ranges for an overlap
	 * Return the result of the check
	 * As well as return the new range to outputRange
	 *
	 ***********************************************************************/
	static int CheckOverlap1Range (Range inputRange1, Range inputRange2,
			Range outputRange) {
		long lower=0L, upper=0L;
		int output=0;
		/******************************************************
		 * if output=5, range1 and range 2 are exactly the same
		 * if output=4, range2 completely covers range1
		 * if output=3, range1 completely covers range2
		 * if output=2, ranges don't overlap, but they are
		 * adjacent and can be combined
		 * if output=1, ranges overlap partially
		 * if output=0, ranges do not overlap
		 ******************************************************/
		/******************************************************
		 * Ranges do not overlap i.e., result in output=0
		 ******************************************************/
		if (inputRange1.upper + 1 < inputRange2.lower ||
				inputRange2.upper + 1 < inputRange1.lower ) {
			output=0;
		}
		/******************************************************
		 * Both ranges are exactly the same
		 ******************************************************/
		else if (inputRange1.lower == inputRange2.lower &&
				inputRange1.upper == inputRange2.upper) {
			outputRange.lower = inputRange1.lower;
			outputRange.upper = inputRange1.upper;
			output=5 ;
		}
		/******************************************************
		 * For all other combinations
		 ******************************************************/
		else {
			/***********************************************
			 * Determine which is the lower value of both ranges
			 ***********************************************/
			if (inputRange1.lower < inputRange2.lower) {
				outputRange.lower = inputRange1.lower;
				lower = inputRange2.lower;
			}
			else {
				outputRange.lower = inputRange2.lower;
				lower = inputRange1.lower;
			}
			/***********************************************
			 * Determine which is the upper value of both ranges
			 ***********************************************/
			if (inputRange1.upper < inputRange2.upper) {
				outputRange.upper = inputRange2.upper;
				upper = inputRange1.upper;
			}
			else {
				outputRange.upper = inputRange1.upper;
				upper = inputRange2.upper;
				/* // For debugging
		System.out.println("output upper : " + outputRange.upper
		+ " ; upper : " + upper);
				 */
			}
			/*************************************************
			 * Check if upper value is >= lower value
			 * If so, it is a valid range
			 * Otherwise, signal the calling function to discard
			 * the output range
			 **************************************************/
			if (upper >= lower) {
				if (inputRange2.lower == lower &&
						inputRange2.upper==upper) output=3 ;
				else if (inputRange1.lower == lower &&
						inputRange1.upper==upper) output=4 ;
				else output=1;
			}
			/*************************************************
			 * Check if both ranges are adjacent
			 * Signal accordingly
			 *************************************************/
			else if (upper + 1 == lower) output=2;
		} // end else
		return output;
	}
	/***********************************************************************
	 *
	 * Function that compares a permit tuple against a deny tuple
	 * Saves the resultant permitted PacketSet to outputPS
	 *
	 * If at least 1 dimension/range doesn't overlap then nothing will be
	 * added to outputPS
	 * outputPS doesn't need a value, because calling function uses a value
	 * in outputPS as permitTuple
	 * count is the position of the permitTuple in outputPS
	 *
	 ***********************************************************************/
	static boolean GetPermitTuple (Tuple permitTuple, Tuple denyTuple,
			PacketSet outputPS, int count) {
		boolean decrementCounter = false;
		Range outRange1 = new Range();
		Range outRange2 = new Range();
		Range outRange3 = new Range();
		Range outRange4 = new Range();
		Range outRange5 = new Range();
		Range outRange6 = new Range();
		Range outRange7 = new Range();
		Range outRange8 = new Range();
		Range outRange9 = new Range();
		Range outRange10 = new Range();
		int result1=0, result2=0, result3=0, result4=0, result5=0;
		Tuple tuple1 = new Tuple();
		Tuple tuple2 = new Tuple();
		Tuple tuple3 = new Tuple();
		Tuple tuple4 = new Tuple();
		Tuple tuple5 = new Tuple();
		Tuple tuple11 = new Tuple();
		Tuple tuple22 = new Tuple();
		Tuple tuple33 = new Tuple();
		Tuple tuple44 = new Tuple();
		Tuple tuple55 = new Tuple();
		result1 = GetPermitRange (permitTuple.sourceIP,
				denyTuple.sourceIP, outRange1, outRange2);
		result2 = GetPermitRange (permitTuple.destinationIP,
				denyTuple.destinationIP, outRange3, outRange4);
		result3 = GetPermitRange (permitTuple.sourcePort,
				denyTuple.sourcePort, outRange5, outRange6);
		result4 = GetPermitRange (permitTuple.destinationPort,
				denyTuple.destinationPort, outRange7, outRange8);
		result5 = GetPermitRange (permitTuple.protocol,
				denyTuple.protocol, outRange9, outRange10);
		if (result1>=0 && result2>=0 && result3>=0 &&
				result4>=0 && result5>=0) {
			Range temp = new Range();
			if (result1>=1) {
				tuple1.sourceIP = outRange1 ;
				tuple1.destinationIP = permitTuple.destinationIP;
				tuple1.sourcePort = permitTuple.sourcePort ;
				tuple1.destinationPort = permitTuple.destinationPort ;
				tuple1.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple1);
			}
			if (result1==2) {
				tuple11.sourceIP = outRange2 ;
				tuple11.destinationIP = permitTuple.destinationIP;
				tuple11.sourcePort = permitTuple.sourcePort ;
				tuple11.destinationPort = permitTuple.destinationPort ;
				tuple11.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple11);
			}
			if (result2>=1 ) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)) {
					tuple2.sourceIP.lower = temp.lower ;
					tuple2.sourceIP.upper = temp.upper ;
				}
				tuple2.destinationIP = outRange3;
				tuple2.sourcePort = permitTuple.sourcePort ;
				tuple2.destinationPort = permitTuple.destinationPort ;
				tuple2.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple2);
			}
			if (result2==2) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple22.sourceIP.lower = temp.lower ;
					tuple22.sourceIP.upper = temp.upper ;
				}
				tuple22.destinationIP = outRange4;
				tuple22.sourcePort = permitTuple.sourcePort ;
				tuple22.destinationPort = permitTuple.destinationPort ;
				tuple22.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple22);
			}
			if (result3>=1) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple3.sourceIP.lower = temp.lower ;
					tuple3.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)){
					tuple3.destinationIP.lower = temp.lower ;
					tuple3.destinationIP.upper = temp.upper ;
				}
				tuple3.sourcePort = outRange5 ;
				tuple3.destinationPort = permitTuple.destinationPort ;
				tuple3.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple3);
			}
			if (result3==2) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple33.sourceIP.lower = temp.lower ;
					tuple33.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)){
					tuple33.destinationIP.lower = temp.lower ;
					tuple33.destinationIP.upper = temp.upper ;
				}
				tuple33.sourcePort = outRange6 ;
				tuple33.destinationPort = permitTuple.destinationPort ;
				tuple33.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple33);
			}
			if (result4>=1) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple4.sourceIP.lower = temp.lower ;
					tuple4.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)){
					tuple4.destinationIP.lower = temp.lower ;
					tuple4.destinationIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.sourcePort,
						denyTuple.sourcePort, temp)){
					tuple4.sourcePort.lower = temp.lower ;
					tuple4.sourcePort.upper = temp.upper ;
				}
				tuple4.destinationPort = outRange7 ;
				tuple4.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple4);
			}
			if (result4==2) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple44.sourceIP.lower = temp.lower ;
					tuple44.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)){
					tuple44.destinationIP.lower = temp.lower ;
					tuple44.destinationIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.sourcePort,
						denyTuple.sourcePort, temp)){
					tuple44.sourcePort.lower = temp.lower ;
					tuple44.sourcePort.upper = temp.upper ;
				}
				tuple44.destinationPort = outRange8 ;
				tuple44.protocol = permitTuple.protocol ;
				outputPS.tupleArray.add(tuple44);
			}
			if (result5>=1) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple5.sourceIP.lower = temp.lower ;
					tuple5.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)) {
					tuple5.destinationIP.lower = temp.lower ;
					tuple5.destinationIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.sourcePort,
						denyTuple.sourcePort, temp)){
					tuple5.sourcePort.lower = temp.lower ;
					tuple5.sourcePort.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationPort,
						denyTuple.destinationPort, temp)){
					tuple5.destinationPort.lower = temp.lower ;
					tuple5.destinationPort.upper = temp.upper ;
				}
				tuple5.protocol = outRange9 ;
				outputPS.tupleArray.add(tuple5);
			}
			if (result5==2) {
				if (GetOverlapRange (permitTuple.sourceIP,
						denyTuple.sourceIP, temp)){
					tuple55.sourceIP.lower = temp.lower ;
					tuple55.sourceIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationIP,
						denyTuple.destinationIP, temp)){
					tuple55.destinationIP.lower = temp.lower ;
					tuple55.destinationIP.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.sourcePort,
						denyTuple.sourcePort, temp)){
					tuple55.sourcePort.lower = temp.lower ;
					tuple55.sourcePort.upper = temp.upper ;
				}
				if (GetOverlapRange (permitTuple.destinationPort,
						denyTuple.destinationPort, temp)){
					tuple55.destinationPort.lower = temp.lower ;
					tuple55.destinationPort.upper = temp.upper ;
				}
				tuple55.protocol = outRange10 ;
				outputPS.tupleArray.add(tuple55);
			}
			outputPS.tupleArray.remove(count);
			decrementCounter = true ;
		} // end if
		return decrementCounter;
	} // end of GetPermitTuple
	/***********************************************************************
	 *
	 * Function that finds the overlapping range in 2 ranges
	 * Return the new range to outputRange
	 *
	 ***********************************************************************/
	static boolean GetOverlapRange (Range inputRange1,
			Range inputRange2, Range outputRange) {
		boolean success = false;
		outputRange.lower = Math.max(inputRange1.lower, inputRange2.lower) ;
		outputRange.upper = Math.min(inputRange1.upper, inputRange2.upper) ;
		if (outputRange.upper >= outputRange.lower) success = true;
		return success;
	}
	/***********************************************************************
	 *
	 * Function that compares a permit range against a deny range
	 * Saves the 2 possible output ranges to outputRange1 and outputRange2
	 *
	 * Return value signifies the extent of the overlap
	 *
	 ***********************************************************************/
	static int GetPermitRange (Range permitRange, Range denyRange,
			Range outputRange1, Range outputRange2) {
		int numberOfOutputRanges = 0;
		/*************************************
		 * 0 means no outputRange used, full overlap
		 * 1 means only 1 outputRange used, there is overlap
		 * 2 means 2 outputRanges used, there is overlap
		 * -1 means no intersection or overlap
		 *************************************/
		/***********************************************
		 * Determine 1st output range
		 ***********************************************/
		outputRange1.lower = permitRange.lower ;
		outputRange1.upper = Math.min(denyRange.lower-1 , permitRange.upper);
		if (outputRange1.upper >= outputRange1.lower)
			numberOfOutputRanges++;
		/***********************************************
		 * Determine 2nd output range
		 ***********************************************/
		outputRange2.lower = Math.max(permitRange.lower, denyRange.upper + 1);
		outputRange2.upper = permitRange.upper ;
		if (outputRange2.upper >= outputRange2.lower)
			numberOfOutputRanges++;
		if (numberOfOutputRanges==1) outputRange1 = outputRange2 ;
		/***********************************************
		 * If permit and deny ranges do not overlap
		 ***********************************************/
		if ( (denyRange.upper < permitRange.lower) ||
				(denyRange.lower > permitRange.upper) )
			numberOfOutputRanges = -1;
		return numberOfOutputRanges;
	}
	/***********************************************************************
	 *
	 * Function that converts an IP address and a mask
	 * into an integer(long) range
	 *
	 * Takes an IP in dotted decimal format
	 * Simplified IP address and mask handling,
	 * where lower value is set to inputIP
	 * and upper value is inputIP with full inputMask
	 *
	 * Currently assumes that router config uses Cisco recommendation of
	 * a contiguous range of IP addresses
	 *
	 * To add functionality later : to handle mask fully,
	 * for cases like "10.10.9.0", "0.0.4.255"
	 * where 9 = 1001, 4 = 100; 1001 and 1101 ; 13 = 1101
	 * and 10.10.9.0 with mask of 0.0.1.255,
	 * which actually refers to a range of 10.10.8.0 to 10.10.9.255
	 * 
	 * Be attention that the mask in Cisco recommendation is different than the 
	 * conventional mask
	 *
	 ***********************************************************************/
	static Range convertIPtoIntegerRange (String inputIP, String inputMask) {
		Range output = new Range();
		int ipOctet1, ipOctet2, ipOctet3, ipOctet4;
		int maskOctet1, maskOctet2, maskOctet3, maskOctet4;
		int upperOctet1, upperOctet2, upperOctet3, upperOctet4 ;
		int lowerOctet1, lowerOctet2, lowerOctet3, lowerOctet4 ;
		String upperOctetString1, upperOctetString2,
		upperOctetString3, upperOctetString4 ;
		String lowerOctetString1, lowerOctetString2,
		lowerOctetString3, lowerOctetString4 ;
		/*******************************************************
		 * If there are no IP addresses, the range is set as the max
		 *******************************************************/
		if (inputIP==null || inputIP.equalsIgnoreCase("any")) {
			output.lower = minIntegerIP ;
			output.upper = maxIntegerIP ;
		}
		else {
			/*******************************************************
			 * Process IP address first
			 * Break up IP address into octets
			 *******************************************************/
			Scanner s = new Scanner(inputIP).useDelimiter("\\.");
			//System.out.println(inputIP);
			ipOctet1 = Integer.parseInt(s.next());
			ipOctet2 = Integer.parseInt(s.next());
			ipOctet3 = Integer.parseInt(s.next());
			ipOctet4 = Integer.parseInt(s.next());
			/*******************************************************
			 * Calculate integer value of the IP address
			 *******************************************************/
			output.lower = ( ipOctet1 * octet1Multiplier ) +
			( ipOctet2 * octet2Multiplier ) +
			( ipOctet3 * octet3Multiplier ) +
			( ipOctet4 * octet4Multiplier ) ;
			/*******************************************************
			 * Process wildcard mask next
			 * If there is no mask, single point in range
			 *******************************************************/
			if (inputMask==null) {
				/* Disabled this section's functionality till
				full mask functionality is implemented
				// Calculate lower value of the IP address
				output.lower = ( ipOctet1 * octet1Multiplier ) +
				( ipOctet2 * octet2Multiplier ) +
				( ipOctet3 * octet3Multiplier ) +
				( ipOctet4 * octet4Multiplier ) ;
				 */
				output.upper = output.lower;
			}
			else {
				/*******************************************************
				 * Break up wildcard mask into octets
				 *******************************************************/
				s = new Scanner(inputMask).useDelimiter("\\.");
				maskOctet1 = Integer.parseInt(s.next());
				maskOctet2 = Integer.parseInt(s.next());
				maskOctet3 = Integer.parseInt(s.next());
				maskOctet4 = Integer.parseInt(s.next());
				/* Disabled this section's functionality till
				full mask functionality is implemented
				// Calculate lower value of IP range in string notation
				lowerOctetString1 = Integer.toString(ipOctet1 ^ maskOctet1) ;
				lowerOctetString2 = Integer.toString(ipOctet2 ^ maskOctet2) ;
				lowerOctetString3 = Integer.toString(ipOctet3 ^ maskOctet3) ;
				lowerOctetString4 = Integer.toString(ipOctet4 ^ maskOctet4) ;
				// Convert lower value of IP range from string to integer
				lowerOctet1 = Integer.parseInt(lowerOctetString1);
				lowerOctet2 = Integer.parseInt(lowerOctetString2);
				lowerOctet3 = Integer.parseInt(lowerOctetString3);
				lowerOctet4 = Integer.parseInt(lowerOctetString4);
				// Calculate integer value of the IP address
				output.lower = ( lowerOctet1 * octet1Multiplier ) +
				( lowerOctet2 * octet2Multiplier ) +
				( lowerOctet3 * octet3Multiplier ) +
				( lowerOctet4 * octet4Multiplier ) ;
				 */
				/*** Calculate upper value of IP range in string ***/
				upperOctetString1 = Integer.toString(ipOctet1 | maskOctet1) ;
				upperOctetString2 = Integer.toString(ipOctet2 | maskOctet2) ;
				upperOctetString3 = Integer.toString(ipOctet3 | maskOctet3) ;
				upperOctetString4 = Integer.toString(ipOctet4 | maskOctet4) ;
				/*** Convert upper value of IP range from string to integer ***/
				upperOctet1 = Integer.parseInt(upperOctetString1);
				upperOctet2 = Integer.parseInt(upperOctetString2);
				upperOctet3 = Integer.parseInt(upperOctetString3);
				upperOctet4 = Integer.parseInt(upperOctetString4);
				/*** Calculate integer value of the IP address ***/
				output.upper = ( upperOctet1 * octet1Multiplier ) +
				( upperOctet2 * octet2Multiplier ) +
				( upperOctet3 * octet3Multiplier ) +
				( upperOctet4 * octet4Multiplier ) ;
			} // end of else statement on inputMask
		} // end of else statement on inputIP
		return output;
	}
	/************************************************************************
	 *
	 * Function to pad a binary string with leading zeros.
	 * Input : binary (string)
	 * Output : 8-bit padded (string)
	 *
	 ************************************************************************/
	static String BinaryPadder (String inputString) {
		for (int counter=inputString.length(); counter<octetLength; counter++)
			inputString = "0" + inputString;
		return inputString;
	}
	/**************************************************************************
	 *
	 * Function to convert an IP from integer into dotted decimal format
	 *
	 **************************************************************************/
	static String convertIntegertoIP (long inputInteger) {
		String outputString = new String();
		long octet1, octet1Mod, octet2, octet2Mod, octet3, octet3Mod, octet4 ;
		String octet1String, octet2String, octet3String, octet4String ;
		octet1 = inputInteger / octet1Multiplier;
		octet1Mod = inputInteger % octet1Multiplier;
		octet2 = octet1Mod / octet2Multiplier;
		octet2Mod = octet1Mod % octet2Multiplier;
		octet3 = octet2Mod / octet3Multiplier;
		octet3Mod = octet2Mod % octet3Multiplier;
		octet4 = octet3Mod / octet4Multiplier;
		octet1String = Long.toString(octet1);
		octet2String = Long.toString(octet2);
		octet3String = Long.toString(octet3);
		octet4String = Long.toString(octet4);
		outputString = octet1String + "." + octet2String + "."
		+ octet3String + "." + octet4String ;
		return outputString;
	}
	/**************************************************************************
	 *
	 * Function to convert a port into a range
	 *
	 **************************************************************************/
	static Range convertPortToRange (String inputLower, String inputUpper) {
		Range outputRange = new Range();
		if (inputLower==null || inputLower.equalsIgnoreCase("any")) {
			outputRange.lower = minPort;
			outputRange.upper = maxPort;
		}
		else {
			outputRange.lower = Long.parseLong(inputLower);
			if (inputUpper==null) outputRange.upper = maxPort;
			else outputRange.upper = Long.parseLong(inputUpper);
		}
		return outputRange;
	}
	/***************************************************************************
	 *
	 * Function to convert a protocol into a range
	 *
	 **************************************************************************/
	static Range convertProtocolToRange (String inputLower, String inputUpper){
		Range outputRange = new Range();
		if (inputLower==null || inputLower.equalsIgnoreCase("any")) {
			outputRange.lower = minProtocol;
			outputRange.upper = maxProtocol;
		}
		else {
			outputRange.lower = Long.parseLong(inputLower);
			if (inputUpper==null) outputRange.upper = maxProtocol;
			else outputRange.upper = Long.parseLong(inputUpper);
		}
		return outputRange;
	}
	/*************************************************************************
	 *
	 * Function to convert an ACL rule into a tuple format
	 *
	 **************************************************************************/
	static Tuple convertACLRuletoTuple (ACLRule inputACLrule) {
		Tuple outputTuple = new Tuple();
		outputTuple.sourceIP = convertIPtoIntegerRange
		(inputACLrule.source, inputACLrule.sourceWildcard);
		outputTuple.destinationIP = convertIPtoIntegerRange
		(inputACLrule.destination, inputACLrule.destinationWildcard);
		outputTuple.sourcePort = convertPortToRange
		(inputACLrule.sourcePortLower, inputACLrule.sourcePortUpper);
		outputTuple.destinationPort = convertPortToRange
		(inputACLrule.destinationPortLower,
				inputACLrule.destinationPortUpper);
		outputTuple.protocol = convertProtocolToRange
		(inputACLrule.protocolLower, inputACLrule.protocolUpper);
		return outputTuple;
	}
	
	/**************************************************************************
	 *
	 * Function to carry out a Union operation on 2 PacketSets
	 *
	 **************************************************************************/
	static PacketSet Union (PacketSet inputPS1, PacketSet inputPS2) {
		PacketSet outputPS = new PacketSet();
		if (inputPS1!=null) outputPS.tupleArray.addAll(inputPS1.tupleArray);
		if (inputPS2!=null) outputPS.tupleArray.addAll(inputPS2.tupleArray);
		return outputPS;
	}
	/**
	 * same as the previous one, plus that we do optimization at last
	 * @param inputPS1
	 * @param inputPS2
	 * @return
	 */
	static PacketSet UnionOpt(PacketSet inputPS1, PacketSet inputPS2) {
		PacketSet outputPS = new PacketSet();
		if (inputPS1!=null) outputPS.tupleArray.addAll(inputPS1.tupleArray);
		if (inputPS2!=null) outputPS.tupleArray.addAll(inputPS2.tupleArray);
		OptimizePacketSet(outputPS);
		return outputPS;
	}
	/**************************************************************************
	 *
	 * Function to carry out an Intersection operation on 2 PacketSets
	 *
	 * Assumption : All tuples in a PacketSet are distinct
	 * (i.e., the 5 ranges in a tuple do not all overlap)
	 * Put another way, tuples cannot be combined further.
	 *
	 **************************************************************************/
	static boolean Intersection (PacketSet inputPS1, PacketSet inputPS2,
			PacketSet outputPS) {
		boolean success=false;
		Tuple tupleOut;
		outputPS.tupleArray.clear(); // Empty the outputPS
		if(inputPS1.IsFalse()|| inputPS2.IsFalse())
		{
			return false;
		}
		for (int i=0; i<inputPS1.tupleArray.size(); i++)
			for (int j=0; j<inputPS2.tupleArray.size(); j++) {
				tupleOut = new Tuple();
				if (IntersectTuple ((Tuple)inputPS1.tupleArray.get(i),
						(Tuple)inputPS2.tupleArray.get(j),tupleOut)) {
					success = true;
					outputPS.tupleArray.add(tupleOut);
				}
			}
		OptimizePacketSet(outputPS);
		return success;
	}
	/****************************
	 * check the intersection of the two is false or not
	 * one way to generate the ap-based expression
	 * @param inputPS
	 * @param outputPS
	 * @return
	 */
	static boolean IntersectionNotFalse (PacketSet inputPS1, PacketSet inputPS2) {
		boolean success=false;
		Tuple tupleOut;
		if(inputPS1.IsFalse()|| inputPS2.IsFalse())
		{
			return false;
		}
		for (int i=0; i<inputPS1.tupleArray.size(); i++)
			for (int j=0; j<inputPS2.tupleArray.size(); j++) {
				tupleOut = new Tuple();
				if (IntersectTuple ((Tuple)inputPS1.tupleArray.get(i),
						(Tuple)inputPS2.tupleArray.get(j),tupleOut)) {
					success = true;
					return success;
				}
			}
		return success;
	}
	/******************
	 * negate a packetset
	 * return false if the negation is empty, 
	 */
	static boolean Negation(PacketSet inputPS, PacketSet outputPS)
	{
		boolean success = false;
		outputPS.Clean();
		if(inputPS.IsFalse())
		{
			// simply return true
			outputPS.tupleArray.add(NoFilters().tupleArray.get(0));
			return true;
		}else
		{
			PacketSet temps = NoFilters();
			for(int i = 0; i < inputPS.tupleArray.size(); i ++)
			{
				PacketSet p2 = new PacketSet();
				PacketSet p3 = new PacketSet();
				//System.out.println(i);
				if(NegateTuple(inputPS.tupleArray.get(i), p2))
				{
					if(Intersection(temps, p2, p3))
					{
						//p3 will be cleared in Intersection automatically
						temps = p3;
					}else
					{
						return success;
					}
					
				}else{
					//then the whole thing is empty....
					return success;
				}
			}
			success = true;
			outputPS.tupleArray.addAll((Collection) temps.tupleArray);
			//OptimizePacketSet(outputPS);
			//System.out.println(outputPS);
			return success;
		}
	}
	/**************************************************************************
	 *
	 * Function to carry out an Intersection operation on 2 tuples
	 *
	 **************************************************************************/
	static boolean IntersectTuple (Tuple tuple1, Tuple tuple2, Tuple tuple3) {
		boolean success = false;
		if (IntersectRange(tuple1.sourceIP, tuple2.sourceIP,
				tuple3.sourceIP, "intersect"))
			if (IntersectRange(tuple1.sourcePort, tuple2.sourcePort,
					tuple3.sourcePort, "intersect"))
				if (IntersectRange(tuple1.destinationIP,
						tuple2.destinationIP, tuple3.destinationIP, "intersect"))
					if (IntersectRange(tuple1.destinationPort,
							tuple2.destinationPort, tuple3.destinationPort,
					"intersect"))
						if (IntersectRange(tuple1.protocol, tuple2.protocol,
								tuple3.protocol, "intersect"))
							success=true;
		return success;
	}
	/*
	 * Negate a tuple will usually get a packetset (a list of tuples)
	 * return false if the negation is empty, ps should be clear before use
	 */
	static boolean NegateTuple (Tuple inputTuple, PacketSet ps){
		boolean success = false;

		ArrayList<Range> or;
		or = new ArrayList<Range> ();
		// source IP field
		NegateRange(new Range(minIntegerIP, maxIntegerIP), inputTuple.sourceIP, or);
		if(or.isEmpty())
		{
			//the whole thing should also be empty
			//return success;
		}else{
			// now at least ps has one tuple
			success = true;
			for (int i = 0; i < or.size(); i ++)
			{
				ps.tupleArray.add(new Tuple(or.get(i), new Range(minPort, maxPort),
						new Range(minIntegerIP, maxIntegerIP), 
						new Range(minPort, maxPort), 
						new Range(minProtocol, maxProtocol)));
			}
			or.clear();
		}

		// source port field
		NegateRange(new Range(minPort, maxPort), inputTuple.sourcePort, or);
		if(or.isEmpty())
		{
			//OptimizePacketSet(ps);
			//return success;
		}else{
			success = true;
			for(int i = 0; i< or.size(); i++)
			{
				ps.tupleArray.add(new Tuple(new Range(inputTuple.sourceIP), 
						or.get(i), new Range(minIntegerIP, maxIntegerIP),
						new Range(minPort, maxPort), new Range(minProtocol, maxProtocol)));
			}
			or.clear();
		}

		// destination IP field
		NegateRange(new Range(minIntegerIP, maxIntegerIP), inputTuple.destinationIP, or);
		if(or.isEmpty())
		{
			//OptimizePacketSet(ps);
			//return success;
		}else{
			success = true;
			for(int i = 0; i< or.size(); i++)
			{
				ps.tupleArray.add(new Tuple(new Range(inputTuple.sourceIP),
						new Range(inputTuple.sourcePort), or.get(i),
						new Range(minPort, maxPort), new Range(minProtocol, maxProtocol)));
			}
			or.clear();
		}

		// destination port field
		NegateRange(new Range(minPort, maxPort), inputTuple.destinationPort, or);
		if(or.isEmpty())
		{
			//OptimizePacketSet(ps);
			//return success;
		}else{
			success = true;
			for (int i = 0; i< or.size(); i ++)
			{
				ps.tupleArray.add(new Tuple(new Range(inputTuple.sourceIP),
						new Range(inputTuple.sourcePort), new Range(inputTuple.destinationIP),
						or.get(i), new Range(minProtocol, maxProtocol)));
			}
			or.clear();
		}
		
		// protocol field
		NegateRange(new Range(minProtocol, maxProtocol), inputTuple.protocol, or);
		if (or.isEmpty())
		{
			
		}else{
			success = true;
			for (int i = 0; i < or.size(); i ++)
			{
				ps.tupleArray.add(new Tuple(new Range(inputTuple.sourceIP), 
						new Range(inputTuple.sourcePort), new Range(inputTuple.destinationIP),
						new Range(inputTuple.destinationPort), or.get(i)));
			}
			or.clear();
		}

		OptimizePacketSet(ps);
		return success;
	}
	/**************************************************************************
	 *
	 * Function to carry out an Intersection operation on 2 ranges
	 *
	 * 2 switches : for union and intersection
	 * For intersection operation, find smaller matching range of
	 * range1 and range2
	 * For union operation, find widest matching range of range1 and range2
	 *
	 * Update : union switch no longer used)
	 *
	 ***********************************************************************/
	static boolean IntersectRange (Range inputRange1, Range inputRange2,
			Range outputRange, String operation) {
		boolean success = false;
		String unionOp = "union";
		String intersectOp = "intersect";
		long lower, upper;
		/*******************************************************************
		 * If the switch is for an intersection operation
		 *******************************************************************/
		if (operation.equals(intersectOp)) {
			if (inputRange1.lower < inputRange2.lower)
				outputRange.lower = inputRange2.lower;
			else outputRange.lower = inputRange1.lower;
			if (inputRange1.upper < inputRange2.upper)
				outputRange.upper = inputRange1.upper;
			else outputRange.upper = inputRange2.upper;
			if (outputRange.upper < outputRange.lower) success=false;
			else success=true;
		}
		/*******************************************************************
		 * If the switch is for a union operation
		 *******************************************************************/
		else if (operation.equals(unionOp)){
			if (inputRange1.lower < inputRange2.lower) {
				outputRange.lower = inputRange1.lower;
				lower = inputRange2.lower;
			}
			else {
				outputRange.lower = inputRange2.lower;
				lower = inputRange1.lower;
			}
			if (inputRange1.upper < inputRange2.upper) {
				outputRange.upper = inputRange2.upper;
				upper = inputRange1.upper;
			}
			else {
				outputRange.upper = inputRange1.upper;
				upper = inputRange2.upper;
			}
			if (upper + 1 >= lower) success=true;//{
			}
		return success;
	}
	/*
	 * negate a range. possibly will result in two ranges, so an arraylist of range 
	 * is returned. The arraylist is empty if the negation is empty.
	 * assume that outputRange is clear before processing
	 */
	static void NegateRange(Range universeRange,Range inputRange, ArrayList<Range> outputRange)
	{
		if(universeRange.lower < inputRange.lower)
		{
			outputRange.add(new Range(universeRange.lower, inputRange.lower - 1));
		}
		if(universeRange.upper > inputRange.upper)
		{
			outputRange.add(new Range(inputRange.upper + 1, universeRange.upper));
		}
	}
	
	/***************************************************************************
	 *
	 * Set default value if no filters are applied to an interface
	 * Default value is to permit any
	 *
	 ***************************************************************************/
	static PacketSet NoFilters() {
		PacketSet output = new PacketSet();
		Tuple out = new Tuple();
		out.sourceIP.lower = minIntegerIP;
		out.sourceIP.upper = maxIntegerIP;
		out.destinationIP.lower = minIntegerIP;
		out.destinationIP.upper = maxIntegerIP ;
		out.sourcePort.lower = minPort;
		out.sourcePort.upper = maxPort;
		out.destinationPort.lower = minPort;
		out.destinationPort.upper = maxPort;
		out.protocol.lower = minProtocol;
		out.protocol.upper = maxProtocol;
		output.tupleArray.add(out);
		return output;
	}
}