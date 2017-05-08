package proxy.gigaspaces;

import java.io.Serializable;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;

@SpaceClass
public class MatrixRowTuple implements Serializable  {

	  private String ssn;
	    private String matrixName;
	    private Boolean isComplete;
	    private Integer rowID;
	    private Integer dimension;    
	    private int[] data;
	    
	    private Integer updateCounter;


	    // Default constructor (required by XAP)
	    public MatrixRowTuple() {}

	    public MatrixRowTuple(String matrixName, Boolean isComplete, Integer rowID, Integer dimension, int[] data) 
	    {
	    	setMatrixName(matrixName);
	    	setIsComplete(isComplete);
	    	setRowID(rowID);
	    	setDimension(dimension);
	    	setData(data);
	    	setUpdateCounter(null);
	    }
	    
	    public MatrixRowTuple(String matrixName, Boolean isComplete, Integer rowID, Integer dimension, int[] data, Integer updateCounter) 
	    {
	    	setMatrixName(matrixName);
	    	setIsComplete(isComplete);
	    	setRowID(rowID);
	    	setDimension(dimension);
	    	setData(data);
	    	setUpdateCounter(updateCounter);
	    }
	   
	    @SpaceId(autoGenerate = true)
	    public String getSsn() {
	        return ssn;
	    }
	    public void setSsn(String ssn) {
	        this.ssn = ssn;
	    }

		public String getMatrixName() {
			return matrixName;
		}

		public void setMatrixName(String matrixName) {
			this.matrixName = matrixName;
		}

		public Boolean getIsComplete() {
			return isComplete;
		}

		public void setIsComplete(Boolean isComplete) {
			this.isComplete = isComplete;
		}
		
		public Integer getRowID() {
			return rowID;
		}

		public void setRowID(Integer rowID) {
			this.rowID = rowID;
		}

		public Integer getDimension() {
			return dimension;
		}

		public void setDimension(Integer dimension) {
			this.dimension = dimension;
		}
		
		public int[] getData() {
			return data;
		}

		public void setData(int[] data) {
			this.data = data;
		}

		public Integer getUpdateCounter() {
			return updateCounter;
		}

		public void setUpdateCounter(Integer updateCounter) {
			this.updateCounter = updateCounter;
		}



	

}
