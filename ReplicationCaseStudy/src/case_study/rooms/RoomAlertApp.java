/*
 * Created on 8May,2017
 */
package case_study.rooms;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import case_study.rooms.Sensor.eSensorType;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.PhysicalLocality;

public class RoomAlertApp {

    public static Hashtable<String, List<String>> groups = null;
    public static ArrayList<Room> roomList;

    public static int numThermometersPerRoom = 4;
    
    public static void main(String[] args) throws KlavaException, InterruptedException {
        
        int numRoom = 3;
        

        beginScenario(numRoom);
    }
      
    static void beginScenario(int numRoom) throws KlavaMalformedPhyLocalityException
    {
        roomList = new ArrayList<Room>();
        groups = buildGroups(numRoom);
        
        // start all sensors in the room
        for(int i=0; i<roomList.size(); i++)
            roomList.get(i).startSensors();
 
        
        Thread drawThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                
                RoomsGUI mp = new RoomsGUI();
                
                while(true)
                { 
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    mp.drawRooms(roomList);

                }
            }
        });
        drawThread.start();
        
        System.out.println("finish creating an app");
    }
     
    
    public static Hashtable<String, List<String>> buildGroups(int numRoom) throws KlavaMalformedPhyLocalityException {
        
        Integer idSequence = 1;
        
        Hashtable<String, List<String>> groups = new Hashtable<>();
        groups.put(eSensorType.CONTROLLER.toString(), new ArrayList<String>());
        groups.put(eSensorType.ALERT.toString(), new ArrayList<String>());
        groups.put("room1", new ArrayList<String>());
        groups.put("room2", new ArrayList<String>());
        groups.put("room3", new ArrayList<String>());
        
        for(int r=0; r<numRoom; r++) {
            String roomName = "room" + (r+1);
            Room room = new Room(roomName);
            for(int i=0; i<RoomAlertApp.numThermometersPerRoom; i++) {
                TemperatureSensor thempSensor = new TemperatureSensor(roomName + "_temp", convertIdentifierToAddress(idSequence++));
                room.addSensor(thempSensor);
                
                // add to corresponding group
                groups.get(roomName).add(thempSensor.getAddress());
            }
            String alertAddress = convertIdentifierToAddress(idSequence++);
            room.addSensor(new AlertSensor(roomName + "_alert", alertAddress));
            groups.get(eSensorType.ALERT.toString()).add(alertAddress);
            
            String controllerAddress = convertIdentifierToAddress(idSequence++);
            room.addSensor(new ControllerSensor(roomName + "_controller", controllerAddress));
            groups.get(eSensorType.CONTROLLER.toString()).add(alertAddress);
            
            roomList.add(room);
        }

        return groups;
    }

    
    public static List<Locality> getLocalitisByGroupName(String groupName)
    {
        List<String> addressList = groups.get(groupName);
        List<Locality> localityList = new ArrayList<Locality>();
        for(int i=0; i<addressList.size(); i++)
        {
            try {
                localityList.add(new PhysicalLocality(addressList.get(i)));
            } catch (KlavaMalformedPhyLocalityException e) {
                e.printStackTrace();
            }
        }
        
        return localityList;
    }    
    
    public static String convertIdentifierToAddress(Integer identifier)
    {
        String ipAddress = "127.0.0.1:" + String.valueOf(6000 + identifier + 1);   
        return ipAddress;
    }
}
