package app.skeleton.block_matrix;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import interfaces.ITupleSpace;


public class DistributedBlockMatrixMasterThread<T extends ITupleSpace> extends Thread
{
	private Object masterGSName;
	int matrixSize;
	int numberOfWorkers;
	int blockSize;
	Class tupleSpaceClass;


	public DistributedBlockMatrixMasterThread(Object masterGSName, int matrixSize, int numberOfWorkers, int blockSize, Class tupleSpaceClass)
	{
		this.masterGSName = masterGSName;
		this.matrixSize = matrixSize;
		this.numberOfWorkers = numberOfWorkers;
		this.tupleSpaceClass = tupleSpaceClass;
		this.blockSize = blockSize;
	}
	   
   @Override
    public void run()  {
		try {
			new DistributedBlockMatrixMaster<T>(masterGSName, matrixSize, numberOfWorkers, blockSize, tupleSpaceClass).start();
		} catch (NoSuchAlgorithmException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}