/*
 * Created on 16May,2017
 */
package case_study.rooms;

import java.util.List;
import java.util.Random;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.replication.RepliTuple;
import klava.replication.eConsistencyLevel;

public class TemperatureSensor extends Sensor {

    private Random r = new Random();

    double currentTemperature = 0.0;
    
    public TemperatureSensor(String name, String address)
            throws KlavaMalformedPhyLocalityException {
        super(name, address);
        
        this.lifeThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true)
                { 
                    try {
                        Thread.sleep( (long) (r.nextDouble()*5000));
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    updateData();
                }
            }
        });
    }
    
    public void start()
    {
        this.lifeThread.start();
    }
    
    public eSensorType getType() {
        return eSensorType.THERMOMETER;
    }
    
    protected void updateData() {
        currentTemperature = r.nextDouble()*70;
        
        System.out.println("Temp-" + this.address + ": " + String.valueOf(currentTemperature));
        
        List<Locality> localityList = RoomAlertApp.getLocalitisByGroupName(this.roomID);
        
        RepliTuple tupleToRemove = new RepliTuple(new Object[]{"temperature", this.roomID, this.address, Double.class});
        try {
            localNode.in_nbR(tupleToRemove, localityList);
        } catch (KlavaException | InterruptedException e) {
            e.printStackTrace();
        }
        
        RepliTuple tuple = new RepliTuple(new Object[]{"temperature", this.roomID, this.address, currentTemperature});
        tuple.setConsistencyLevel(eConsistencyLevel.WEAK);
        
        try {
            localNode.outR(tuple, localityList);
            

        } catch (KlavaException e) {
            e.printStackTrace();
        }
    }

}
