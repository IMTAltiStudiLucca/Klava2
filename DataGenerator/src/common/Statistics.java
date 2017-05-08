package common;

import java.util.Arrays;

public class Statistics 
{
/*	Double[] data;
    int size;   

    public Statistics(Double[] data) 
    {
        this.data = data;
        size = data.length;
    }   */

    public static double getMean(Double[] data)
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.length;
    }

    public static double getVariance(Double[] data)
    {
        double mean = getMean(data);
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
        return temp/data.length;
    }

    public static double getStdDev(Double[] data)
    {
    	double variance = getVariance(data);
        return Math.sqrt(variance);
    }

    public static Double getMedian(Double[] data) 
    {
    	if(data.length == 0)
    		return null;
		Arrays.sort(data);

    	if (data.length % 2 == 0) 
    	{
    		return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
    	} 
    	else 
    	{
    		return data[data.length / 2];
    	}
    }
    
    public static Double getMax(Double[] data)
    {
    	if(data.length == 0)
    		return null;
    	
    	Arrays.sort(data);
        return data[data.length - 1];
    }
    
    public static Double getMin(Double[] data)
    {    	
    	if(data.length == 0)
    		return null;
    
    	Arrays.sort(data);
        return data[0];
    }
    
}