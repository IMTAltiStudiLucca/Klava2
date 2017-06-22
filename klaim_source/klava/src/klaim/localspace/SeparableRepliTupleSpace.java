/*
 * Created on 30 Oct 2016
 */
package klaim.localspace;

import klava.Tuple;
import klava.TupleSpace;
import klava.replication.RepliTuple;

public class SeparableRepliTupleSpace<T extends TupleSpace> extends SeparableTupleSpace<T>
{
    
    public SeparableRepliTupleSpace(Class<T> tupleSpaceClass)
    {
        super(tupleSpaceClass);
    }

    @Override
    protected String getSeparator(Tuple tuple)
    {   
        if (tuple instanceof RepliTuple) {
            String key = ((RepliTuple)tuple).getTiedLocationGroup();
            return key;
        } else
            return null;
    }

 
    
}
