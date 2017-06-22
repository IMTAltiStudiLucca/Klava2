/*
 * Created on 20Apr.,2017
 */
package klava.replication;

import java.util.List;

import klava.Tuple;

public class RepliTuple extends Tuple
{
    /** */
    private static final long serialVersionUID = 3098685903271433335L;
    private String owner;
    private eConsistencyLevel consistencyLevel;
    private String tiedLocalityGroup;
    
    // for the tuple of owner
    private List<String> locationList;
    
    
    public RepliTuple(Object[] data)
    {
        super(data);
    }
      
    
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public eConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }
    public void setConsistencyLevel(eConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }
    public String getTiedLocationGroup() {
        return tiedLocalityGroup;
    }
    public void setTiedLocalityGroup(String tiedLocationGroup) {
        this.tiedLocalityGroup = tiedLocationGroup;
    }
    
    public List<String> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<String> locationList) {
        this.locationList = locationList;
    }
    
    
    @Override
    public void copy(Tuple t) {
        super.copy(t);

        RepliTuple castT = (RepliTuple)t;
        this.setOwner(((RepliTuple)t).getOwner());
        this.setConsistencyLevel(((RepliTuple)t).getConsistencyLevel());
        this.setTiedLocalityGroup(((RepliTuple)t).getTiedLocationGroup());

    }



}
