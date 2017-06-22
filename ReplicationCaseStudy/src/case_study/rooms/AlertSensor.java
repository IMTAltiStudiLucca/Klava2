/*
 * Created on 17May,2017
 */
package case_study.rooms;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.replication.RepliTuple;

public class AlertSensor extends Sensor {

    AtomicBoolean alertState = new AtomicBoolean(false);
    public AlertSensor(String name, String ip) throws KlavaMalformedPhyLocalityException {
        super(name, ip);

        this.lifeThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true)
                { 
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    alertState.set(checkPresenceOfAlert());
                    if(alertState.get()) {
                        System.err.println(address + ": ALERT");
                    } 

                }
            }
        });
    }
    
    public void start()
    {
        this.lifeThread.start();
    }
    
    public eSensorType getType() {
        return eSensorType.ALERT;
    }
    
    
    private boolean checkPresenceOfAlert()
    {
        List<Locality> localityList = RoomAlertApp.getLocalitisByGroupName(eSensorType.ALERT.toString());
        
        // add alert message
        RepliTuple alertOnTemplate = new RepliTuple(new Object[]{"alert", Boolean.class});
        try {
            boolean result = localNode.read_nbR(alertOnTemplate, localityList);
            return result;
        } catch (KlavaException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
//    private void notifyOtherAlerts()
//    {
//        
//        
//    }

}
