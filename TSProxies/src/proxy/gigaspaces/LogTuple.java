package proxy.gigaspaces;


import java.io.Serializable;
import java.util.ArrayList;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class LogTuple implements Serializable  {

    private String ssn;
    private String logName;
    private ArrayList data;

    // Default constructor (required by XAP)
    public LogTuple() {}

    public LogTuple(String logName, ArrayList data) 
    {
    	setLogName(logName);
    	setData(data);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}	

	public ArrayList getData() {
		return data;
	}

	public void setData(ArrayList data) {
		this.data = data;
	}


    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}