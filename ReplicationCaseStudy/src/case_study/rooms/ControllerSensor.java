/*
 * Created on 17May,2017
 */
package case_study.rooms;

import java.util.List;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.replication.RepliTuple;
import klava.replication.eConsistencyLevel;

public class ControllerSensor extends Sensor {

    boolean isDetected = false;
    double averageTemperatureGlobal = 0.0;
    double temperatureThreshold = 40.0;
    
    public ControllerSensor(String name, String ip)
            throws KlavaMalformedPhyLocalityException {
        super(name, ip);
               
        this.lifeThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true)
                { 
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Double averageTemp = checkAverageTemp();
                    if( averageTemp.compareTo(temperatureThreshold) > 0) {
                        System.out.println(address + ": Achtung " + String.valueOf(averageTemp));
                        
                        // signal about an alert
                        //removeAlertState(false);
                        notifyAlert(true);
                        isDetected = true;
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                        isDetected = false;
                        // remove the alert
                        removeAlertState(true);
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
        return eSensorType.CONTROLLER;
    }

    
    public Double checkAverageTemp() {
        
        List<Locality> localityList = RoomAlertApp.getLocalitisByGroupName(this.roomID);
        
        double averageTemp = 0.0;
        
        for(int i=0; i<RoomAlertApp.numThermometersPerRoom; i++) {
            RepliTuple temperatureTemplate = new RepliTuple(new Object[]{"temperature", this.roomID, String.class, Double.class});
            try {
                localNode.inR(temperatureTemplate, localityList);
            } catch (KlavaException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            averageTemp += (Double)temperatureTemplate.getItem(3);
        }
        averageTemp = averageTemp/RoomAlertApp.numThermometersPerRoom;
        averageTemperatureGlobal = averageTemp;
        return averageTemp;
    }
    
    
    public void notifyAlert(boolean alertOn) {
        List<Locality> localityList = RoomAlertApp.getLocalitisByGroupName(eSensorType.ALERT.toString());
               
        // add alert message
        RepliTuple alertOnTuple = new RepliTuple(new Object[]{"alert", true});
        alertOnTuple.setConsistencyLevel(eConsistencyLevel.WEAK);
        try {
            localNode.outR(alertOnTuple, localityList);      
        } catch (KlavaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }
    
    
    public void removeAlertState(boolean alertStatus) {
        List<Locality> localityList = RoomAlertApp.getLocalitisByGroupName(eSensorType.ALERT.toString());
               
        // add alert message
        RepliTuple tupleToRemoveTemplate = new RepliTuple(new Object[]{"alert", true});
        try {
            localNode.in_nbR(tupleToRemoveTemplate, localityList);
        } catch (KlavaException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }
    
    
    
}
