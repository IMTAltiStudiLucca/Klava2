package common;

public class TupleLogStructure 
{
	public TupleLogStructure(Double time, String labelName)
	{
		//this.testKey = testKey;
		this.time = time;
		this.labelName = labelName;
		computed = false;
	}
	
	//public String testKey;
	boolean computed;
	// in nanoseconds
	public Double time;
	public String labelName;
	

}
