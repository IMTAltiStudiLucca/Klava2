package proxy.gigaspaces;

import java.io.Serializable;
import java.util.Vector;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class UpdateDataTuple implements Serializable {

    private String ssn;
    private String taskType;
    private String firstField;
    private Integer secondField;
    private Integer thirdField;
    private Vector<?> data;

    // Default constructor (required by XAP)
    public UpdateDataTuple() {}

    public UpdateDataTuple(String _firstField, Integer _secondField, Integer _thirdField, Vector<?> _data) 
    {
    	setTaskType("oceanModel");
    	setFirstField(_firstField);
    	setSecondField(_secondField);
    	setThirdField(_thirdField);
    	setData(_data);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

	public String getFirstField() {
		return firstField;
	}

	public void setFirstField(String firstField) {
		this.firstField = firstField;
	}

	public Integer getSecondField() {
		return secondField;
	}

	public void setSecondField(Integer secondField) {
		this.secondField = secondField;
	}

	public Integer getThirdField() {
		return thirdField;
	}

	public void setThirdField(Integer thirdField) {
		this.thirdField = thirdField;
	}

	public Vector<?> getData() {
		return data;
	}

	public void setData(Vector<?> data) {
		this.data = data;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}





    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}