package repliklaim;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.replication.RepliTuple;

public class Controller extends Sensor{

	public Controller(String name, String address) throws KlavaMalformedPhyLocalityException {
		super(name, address);
		// TODO Auto-generated constructor stub
	}
      
	float averageTemperature;  
	

	public void run(){
	
		while (true){
			try {
/*				PhysicalLocality physLoc = new PhysicalLocality( "127.0.0.1:6001");
				RepliTuple template1 = new RepliTuple(new Object[]{"sensor", String.class, "temperature", Float.class});
				node.readRK(template1,physLoc);		
				
				PhysicalLocality physLoc2 = new PhysicalLocality( "127.0.0.1:6002");
				RepliTuple template2 = new RepliTuple(new Object[]{"sensor", String.class, "temperature", Float.class});		
				node.readRK(template2,physLoc2);*/
				
				
				RepliTuple template1 = new RepliTuple(new Object[]{"sensor", "sensor_1", "temperature", Float.class});
				node.readRK(template1);
				System.out.println(template1);
							
				RepliTuple template2 = new RepliTuple(new Object[]{"sensor", "sensor_2", "temperature", Float.class});
				node.readRK(template2);
				
				float temperature1 = (float)template1.getItem(3);
				float temperature2 = (float)template2.getItem(3);
				averageTemperature = (temperature1 + temperature2)/2;
				
				System.out.println("Controller. Average temperature =" + averageTemperature);
				
				
					
			} catch (KlavaException | InterruptedException e) {
				
				
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				
			}
    
			 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
					
			//read temperature from each sensor
		
		
		}
		
		// read the temperature from nodes
		
		
		
	}
	
}
