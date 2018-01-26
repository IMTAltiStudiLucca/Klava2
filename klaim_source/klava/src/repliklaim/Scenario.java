package repliklaim;
import java.util.ArrayList;

import klava.KlavaMalformedPhyLocalityException;

public class Scenario {

	public static void main(String[] args) throws KlavaMalformedPhyLocalityException {

		String controllerAdress = "127.0.0.1:6003";
		Controller controller = new Controller("controller", controllerAdress);
		controller.start();
		
		String tempSensor1Address = "127.0.0.1:6001";
		TemperatureSensor sensor1 = new TemperatureSensor("sensor_1", tempSensor1Address, controllerAdress);
		sensor1.start();

		String tempSensor2Address = "127.0.0.1:6002";
		TemperatureSensor sensor2 = new TemperatureSensor("sensor_2", tempSensor2Address, controllerAdress);
		sensor2.start();
	

		
		//ArrayList<String> localityAddresses = new ArrayList<>();
		//localityAddresses.add(tempSensor1Address);
		//localityAddresses.addAll(tempSensor2Address);
		

	}

	
	

}
