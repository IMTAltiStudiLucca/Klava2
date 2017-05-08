package proxy.gigaspaces;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

import case_studies.Panel;

@SpaceClass
public class PanelTuple implements Serializable {

    private String ssn;
    private String taskName;
    private String name;
    private Panel panel;

    // Default constructor (required by XAP)
    public PanelTuple() {}

    public PanelTuple(String name, Panel panel) 
    {
    	setName(name);
    	setPanel(panel);
    }
    
    public PanelTuple(String taskName, String name, Panel panel) 
    {
    	setName(taskName);
    	setName(name);
    	setPanel(panel);
    }
   
    @SpaceId(autoGenerate = true)
    public String getSsn() {
        return ssn;
    }
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

	public Panel getPanel() {
		return panel;
	}

	public void setPanel(Panel panel) {
		this.panel = panel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}



    
    
    // Getters and Setters of firstName and lastName are omitted in this snippet.    
}