/*
 * Created on 30May,2017
 */
package klaim.tree;

import java.util.Comparator;

import klava.Tuple;

public class TupleComparator implements Comparator<Tuple>{
    private Boolean[] mask;
    
    public TupleComparator(Boolean[] mask){
        this.mask = mask;
    }


    public boolean equals(Tuple tuple, Tuple tupleToCompare) {
        return compare(tuple, tupleToCompare) == 0;    
    }
    
    @Override
    public int compare(Tuple tuple, Tuple tupleToCompare) {
        int lengthDifference = tuple.length() - tupleToCompare.length();
        
        if(lengthDifference == 0)
        {
            for(int i=0; i<tuple.length(); i++)
            {
                if(mask!= null && mask[i] == false)
                    continue;
                Comparable<Object> obj1 = (Comparable<Object>) tuple.getItem(i);
                Comparable<Object> obj2 = (Comparable<Object>) tupleToCompare.getItem(i);
                int comparisionResult = obj1.compareTo(obj2);
                if(comparisionResult == 0)
                    continue;
                else 
                    return comparisionResult;
            }
        }
        else if(lengthDifference < 0)
            return -1;
        else if (lengthDifference > 0)
            return 1;

        return 0;
    }

  }