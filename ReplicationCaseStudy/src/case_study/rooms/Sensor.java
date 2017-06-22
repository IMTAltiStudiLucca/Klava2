/*
 * Created on 16May,2017
 */
package case_study.rooms;

import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.topology.KlavaNode;

public abstract class Sensor {
    
    public enum eSensorType {THERMOMETER, ALERT, CONTROLLER}
    String name;
    protected String roomID = null;
    protected String address;
    
    KlavaNode localNode;
    protected Thread lifeThread = null;
    
    public Sensor(String name, String address) throws KlavaMalformedPhyLocalityException {
        this.name = name;
        this.address = address;
        localNode = new KlavaNode(new PhysicalLocality(address), true);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
    
    public String getAddress(){
        return this.address;
    }
    
    public abstract eSensorType getType();
    
    public abstract void start();
    
//    public abstract void stop();
    
}

