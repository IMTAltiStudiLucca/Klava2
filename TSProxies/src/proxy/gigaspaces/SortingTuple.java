package proxy.gigaspaces;


import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

import apps.sorting.QSort;

@SpaceClass
public class SortingTuple implements Serializable  {

    private String ssn;
    private QSort qsort;
    private String operationType;
    private String status;

    // Default constructor (required by XAP)
    public SortingTuple() {}

    public SortingTuple(String _operationType, QSort _qsort, String _status) 
    {
    	setOperationType(_operationType);
    	setQsort(_qsort);
    	setStatus(_status);
    }
    
    public SortingTuple(QSort _qsort, String _status) 
    {
    	setOperationType("qsort");
    	setQsort(_qsort);
    	setStatus(_status);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public QSort getQsort() {
		return qsort;
	}

	private void setQsort(QSort qsort) {
		this.qsort = qsort;
	}

	public String getStatus() {
		return status;
	}

	private void setStatus(String status) {
		this.status = status;
	}

	public String getOperationType() {
		return operationType;
	}

	private void setOperationType(String operationType) {
		this.operationType = operationType;
	}

    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}