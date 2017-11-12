package stanalysis;

public class ForwardingRule implements Comparable<ForwardingRule>{
	long destip;
	int prefixlen;
	String outinterface;
	int bddrep;
	boolean visible;
	
	/**
	 * form a forwarding rule
	 * @param destip
	 * @param prefixlen
	 * @param outinterface
	 */
	
	public ForwardingRule(long destip, int prefixlen, String outinterface)
	{
		this.destip = destip;
		this.prefixlen = prefixlen;
		this.outinterface = outinterface;
		this.visible = true;
	}
	
	public int compareTo(ForwardingRule another_rule) {
        return this.prefixlen - another_rule.prefixlen;
    }
	
	public void setVisible()
	{
		visible = true;
	}
	
	public void setInvisible()
	{
		visible = false;
	}
	
	public boolean isvisible()
	{
		return visible;
	}
	
	public long getdestip()
	{
		return destip;
	}
	
	public int getprefixlen()
	{
		return prefixlen;
	}
	
	public String getiname()
	{
		return outinterface;
	}
	
	public void setBDDRep(int bddentry)
	{
		bddrep = bddentry;
	}
	
	public int getBDDRep()
	{
		return bddrep;
	}

	public String toString()
	{
		return destip + " " + prefixlen + " " + outinterface;
	}
}
