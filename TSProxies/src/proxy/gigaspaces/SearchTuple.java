package proxy.gigaspaces;


import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class SearchTuple implements Serializable {

    private String ssn;
    private String tupleName;
    private String hashValue;
    private String value;

    // Default constructor (required by XAP)
    public SearchTuple() {}

    public SearchTuple(String _tupleName, String _hashValue, String _value) 
    {
    	tupleName = _tupleName;
    	setHashValue(_hashValue);
    	setValue(_value);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

	public String getTupleName() {
		return tupleName;
	}

	private void setTupleName(String tupleName) {
		this.tupleName = tupleName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getHashValue() {
		return hashValue;
	}

	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}

    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}