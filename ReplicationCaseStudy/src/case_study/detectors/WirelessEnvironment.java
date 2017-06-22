/*
 * Created on 15May,2017
 */
package case_study.detectors;

import java.util.ArrayList;
import java.util.List;

public class WirelessEnvironment {

    List<MobileDevice> devices;
    double distanceForDetection;

    
    public WirelessEnvironment(double distanceForDetection)
    {
        devices = new ArrayList<MobileDevice>();  
        this.distanceForDetection = distanceForDetection;
    }
    
    public void addDevice(MobileDevice device)
    {
        devices.add(device);
    }
    
    public synchronized List<MobileDevice> detectDevices(PointStruct decectorCenter)
    {
        List<MobileDevice> detectedDevices = new ArrayList<MobileDevice>();  
        
        for(int i=0; i<devices.size(); i++)
        {
            MobileDevice dev = devices.get(i);
            PointStruct currentPosition = dev.getCurrentPosition();
            Double distance = PointStruct.getDistanceBetweenPoints(currentPosition, decectorCenter);
            if(distance <= distanceForDetection)
                detectedDevices.add(dev);
        }
        
        return detectedDevices;
    }
    
}
