/*
 * Created on 8May,2017
 */
package case_study.detectors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import klaim.localspace.SeparableRepliListTupleSpace;
import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.replication.RepliTuple;
import klava.replication.eConsistencyLevel;
import klava.topology.KlavaNode;
import klava.topology.KlavaNode.eReplicationType;

public class MobileDetector {
    PointStruct centerCoordinates = null;
    double radius;
    String name;
    int identifier;
    WirelessEnvironment wEnvironment;
    Thread lifeThread = null;
    
    int detectedDevicesCount = 0;
    int informedDeviceCount = 0;
    
    KlavaNode localNode;
    Hashtable<Integer, MobileDevice> internalListOfDetectedDevices = null;
    
    public MobileDetector(int identifier, PointStruct centerCoordinates, double radius, String name, WirelessEnvironment wEnvironment) throws KlavaMalformedPhyLocalityException
    {
        this.centerCoordinates = centerCoordinates;
        this.radius = radius;
        this.name = name;
        this.wEnvironment = wEnvironment;
        this.internalListOfDetectedDevices = new Hashtable<>();
        this.identifier = identifier;
        String ipAddressStr = MobileDetectorApp.convertIdentifierToAddress(this.identifier);
        this.localNode = new KlavaNode(new PhysicalLocality(ipAddressStr), SeparableRepliListTupleSpace.class, eReplicationType.TOPOLOGY_REPLICATION);
        
        
        this.lifeThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true) {
                    
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    // detect device
                    try {
                        detect();
                        
                        boolean res = checkNeighbourDetectors();
                        if(res)
                            informedDeviceCount = 1;
                        else
                            informedDeviceCount = 0;
                    } catch (KlavaException | InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
            }
        });
    }

    public void start()
    {
        lifeThread.start();
    }
    
    private boolean detect() throws KlavaException, InterruptedException
    {

        boolean isChanged = false;
        Hashtable<Integer, MobileDevice> detectedDevices =  wEnvironment.detectDevices(centerCoordinates);
               
    	if(identifier == 3 && detectedDevices.size() > 0)
    		System.out.println("lolo");
    	
        // get list of old devices and new detected devices
        List<MobileDevice> toRemoveList =  new ArrayList<>();
        List<MobileDevice> toAddList =  new ArrayList<>();
        
        Iterator<Integer> internalListKeysIter = this.internalListOfDetectedDevices.keySet().iterator();
        while(internalListKeysIter.hasNext()) {
        	Integer deviceIdentifier = internalListKeysIter.next();
            MobileDevice dev = this.internalListOfDetectedDevices.get(deviceIdentifier);
            if (!detectedDevices.containsKey(deviceIdentifier)) {
                toRemoveList.add(dev);      
                
                printCurrentTime();
                System.out.println("remove from detected device #" + String.valueOf(dev.getIdentifier()));   
                System.out.println("Undetect: " + dev.getCurrentPosition().toString());
            }   
        }
       
        // remove not-detected devices from the list of detected locally devices
        for(int i=0; i<toRemoveList.size();i++) {
            this.internalListOfDetectedDevices.remove(toRemoveList.get(i).getIdentifier());
            removeFromTupleSpace(localNode, this.identifier, toRemoveList.get(i));
        }
                
        detectedDevicesCount = detectedDevices.size();
        
        // find devices newly detected
        Iterator<Integer> detectedDeviceKeyIter = detectedDevices.keySet().iterator();
        while(detectedDeviceKeyIter.hasNext()) {
            isChanged = true;
            Integer deviceIdentifier = detectedDeviceKeyIter.next();
            MobileDevice dev = detectedDevices.get(deviceIdentifier);
            if (!this.internalListOfDetectedDevices.containsKey(dev.getIdentifier())) {
                toAddList.add(dev);
                this.internalListOfDetectedDevices.put(dev.getIdentifier(), dev);
                
                // write about detected devices to a group associated with the current detector
                putIntoTupleSpace(localNode, this.identifier, dev);
                
                printCurrentTime();
                System.out.println("Detected by detector: " + identifier + " mobile device #" + String.valueOf(dev.getIdentifier()));
                System.out.println("Dect: " + dev.getCurrentPosition().toString());
            }
        }

        return isChanged;
    }
    private static void putIntoTupleSpace(KlavaNode node, int detectorIdentifier, MobileDevice dev) throws KlavaException {
        
        RepliTuple detectedDeviceTuple = new RepliTuple(new Object[]{"devices_detected", detectorIdentifier, dev.getIdentifier()});
        detectedDeviceTuple.setConsistencyLevel(eConsistencyLevel.WEAK);
        
        // get groups to put a tuple
        List<Integer> neighbours = MobileDetectorApp.getNeighbourGroup(detectorIdentifier);
        
        List<Locality> localities = new ArrayList<Locality>();
        for(int i=0; i<neighbours.size(); i++) {
            Integer detectorID = neighbours.get(i);
            PhysicalLocality locality = new PhysicalLocality(MobileDetectorApp.convertIdentifierToAddress(detectorID));
            localities.add(locality);
        }
        node.outR(detectedDeviceTuple, localities);
    }
    
    private static void removeFromTupleSpace(KlavaNode node, int detectorIdentifier, MobileDevice dev) throws KlavaException, InterruptedException {
        
        RepliTuple detectedDeviceTuple = new RepliTuple(new Object[]{"devices_detected", detectorIdentifier, dev.getIdentifier()});
        detectedDeviceTuple.setConsistencyLevel(eConsistencyLevel.WEAK);
        
        // get groups to put a tuple
        List<Integer> neighbours = MobileDetectorApp.getNeighbourGroup(detectorIdentifier);
        List<Locality> localities = new ArrayList<Locality>();
        for(int i=0; i<neighbours.size(); i++) {
            Integer detectorID = neighbours.get(i);
            PhysicalLocality locality = new PhysicalLocality(MobileDetectorApp.convertIdentifierToAddress(detectorID));
            localities.add(locality);
        }
        node.in_nbR(detectedDeviceTuple, localities);
    }

    private static List<Locality> convertToLocations(int detectorIdentifier)
            throws KlavaMalformedPhyLocalityException {
        List<Integer> neighbours = MobileDetectorApp.getNeighbourGroup(detectorIdentifier);
               
        List<Locality> localities = new ArrayList<Locality>();
        for(int i=0; i<neighbours.size(); i++) {
            Integer detectorID = neighbours.get(i);
            PhysicalLocality locality = new PhysicalLocality(MobileDetectorApp.convertIdentifierToAddress(detectorID));
            localities.add(locality); 
        }
        return localities;
    }
    
    
    boolean checkNeighbourDetectors() throws KlavaException, InterruptedException 
    {
        List<Integer> neighbours = MobileDetectorApp.getNeighbourGroup(this.identifier);

        for(int i=0; i<neighbours.size(); i++) {
            
            RepliTuple detectedDeviceTuple = new RepliTuple(new Object[]{"devices_detected", Integer.class, Integer.class});
            List<Locality> localities = convertToLocations(neighbours.get(i));
            boolean result = localNode.read_nbR(detectedDeviceTuple, localities);
            if(result) {
                detectedDeviceTuple = new RepliTuple(new Object[]{"devices_detected", Integer.class, Integer.class});
                result = localNode.read_nbR(detectedDeviceTuple, localities);
                return result;
                
            }
        }
        
        return false;
    }
    
    public static void printCurrentTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
    }
    
    
}
