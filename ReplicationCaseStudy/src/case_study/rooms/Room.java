/*
 * Created on 16May,2017
 */
package case_study.rooms;

import java.util.ArrayList;
import java.util.List;

public class Room {
    List<Sensor> sensors = new ArrayList<Sensor>();
    String roomID;
    
    public Room(String roomID) {
        this.roomID = roomID;
        
    }
    
    public void addSensor(Sensor sensor) {
        sensor.setRoomID(this.roomID);
        sensors.add(sensor);
    }
    
    public void startSensors() {
        for(int i=0; i<sensors.size(); i++) {
            sensors.get(i).start();
        }
    }

}
