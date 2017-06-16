package axillary;

public class SyncObjectClass
{
	int count = 0;
	
	public synchronized int get()
	{
		return count;
	}
	
	public synchronized void inc()
	{
		count++;
	}

}
