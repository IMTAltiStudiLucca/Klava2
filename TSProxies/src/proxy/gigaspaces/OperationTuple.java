package proxy.gigaspaces;


import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class OperationTuple implements Serializable  {

    private String ssn;
    private String operationName;
    private String status;

    // Default constructor (required by XAP)
    public OperationTuple() {}

    public OperationTuple(String _operationName, String _status) 
    {
    	setOperationName(_operationName);
    	setStatus(_status);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}