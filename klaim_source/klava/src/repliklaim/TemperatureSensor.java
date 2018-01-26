package repliklaim;
import java.util.Arrays;
import java.util.Random;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.replication.RepliTuple;
import klava.replication.eConsistencyLevel;

public class TemperatureSensor extends Sensor {

	
	Random r = new Random();
	String controllerAddress = null;
	
	// constructor
	public TemperatureSensor(String name, String address, String controllerAddress) throws KlavaMalformedPhyLocalityException  {
		// call first the constructor of the base class (Sensor)
		super(name, address);
		this.controllerAddress = controllerAddress;
	}
	
	public void run() {
		
		while (true) {
			// measure the temperature
			float currentTemp = 30*r.nextFloat();
						
			try {
				updateTemp(currentTemp);
			} catch (KlavaException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Temperature (" + identifier + ") = " + currentTemp);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void updateTemp(float currentTemp) throws KlavaException, InterruptedException {
		
		RepliTuple removeTemplate = new RepliTuple(new Object[]{"sensor", identifier, "temperature", Float.class});
		node.in_nbRK(removeTemplate);
	//	System.out.println(removeTemplate);
		
		// put a tuple into the local tuple space
		RepliTuple tempTuple = new RepliTuple(new Object[]{"sensor", identifier,"temperature", currentTemp});
		tempTuple.setConsistencyLevel(eConsistencyLevel.STRONG);
		
		// write tuple locally and replicate to the tuple space of the controller
		PhysicalLocality localNodeLoc = new PhysicalLocality(nodeAddress);
		PhysicalLocality controllerLoc = new PhysicalLocality(controllerAddress);
		
		// tuple is replicated to two (specified) localities (i.e., temperature sensor and controller)
		node.outRK(tempTuple, Arrays.asList(localNodeLoc, controllerLoc));		
		
	}
}
