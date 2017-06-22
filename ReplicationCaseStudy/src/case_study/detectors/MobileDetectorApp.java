/*
 * Created on 8May,2017
 */
package case_study.detectors;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import klava.KlavaException;
import klava.KlavaMalformedPhyLocalityException;

public class MobileDetectorApp {

    public static Hashtable<Integer, List<Integer>> neighbourGroups = null;
    static WirelessEnvironment wEnvironment = null;
    static List<MobileDetector> mdList = new ArrayList<MobileDetector>();
    public static double distanceForDetection = 50.0;
    
    public static void main(String[] args) throws KlavaException, InterruptedException {
        
        int nCol = 7;
        int nRow = 3;
        int nMobDev = 1;
        
        neighbourGroups = creatGroups(nCol, nRow);
        beginScenario(nCol, nRow, nMobDev);
        
        
        ArrayList<PointStruct> centerList = new ArrayList<>();
        for(int i=0; i<mdList.size(); i++)
            centerList.add(mdList.get(i).centerCoordinates);
        
//        DetectorsGUI mp = new DetectorsGUI();
//        mp.paintCircles(mdList, wEnvironment);
    }
      
    static void beginScenario(int nCol, int nRow, int nMobDev ) throws KlavaMalformedPhyLocalityException
    {

        wEnvironment = new WirelessEnvironment(distanceForDetection);
        
        // init mobile detectors
        for(int i=0; i<nCol; i++)
            for(int j=0; j<nRow; j++)
            {
                String name = formName(i, j);
                double xCenter = distanceForDetection + 2*i*distanceForDetection - i*0.25*distanceForDetection;
                double yCenter = distanceForDetection + 2*j*distanceForDetection - j*0.25*distanceForDetection;
                MobileDetector md = new MobileDetector(formIdentifier(nCol, i, j),new PointStruct(xCenter, yCenter), 
                        distanceForDetection, 
                        name,
                        wEnvironment
                        );
                
                md.start();   
                mdList.add(md);
            }

        // init mobile devices
        for(int i=0; i<nMobDev; i++) {
            MobileDevice mobDev = new MobileDevice(i, new PointStruct(0, 0));
            mobDev.start();
            
            // attach to the environment
            wEnvironment.addDevice(mobDev);
        }
        
        
        
        
        Thread drawThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                
                DetectorsGUI mp = new DetectorsGUI();

                while(true)
                { 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                  mp.paintCircles(mdList, wEnvironment);

                }
            }
        });
        drawThread.start();
        
        
        System.out.println("finish creating an app");
    }
    
    public static String formName(int first, int second)
    {
        return String.valueOf(first) + "_" + String.valueOf(second);
    }
    
    public static Integer formIdentifier(int nCol, int colID, int rowID)
    {
        return colID*nCol + rowID;
    }   
    
    
    // create groups of neighbors;
    public static Hashtable<Integer, List<Integer>> creatGroups(int nCol, int nRow)
    {
        Hashtable<Integer, List<Integer>> groups = new Hashtable<Integer, List<Integer>>();
        
        for(int i=0; i<nCol; i++)
            for(int j=0; j<nRow; j++)
            {
                Integer identifier = formIdentifier(nCol, i, j);
                ArrayList<Integer> neighbours = new ArrayList<>();
                neighbours.add(identifier);

                  if(i>0)
                      neighbours.add(formIdentifier(nCol, i-1, j));
                  if(i<nCol-1)
                      neighbours.add(formIdentifier(nCol, i+1, j));
                  if(j>0)
                      neighbours.add(formIdentifier(nCol, i, j-1));
                  if(j<nRow-1)
                      neighbours.add(formIdentifier(nCol, i, j+1));   
                
                
                groups.put(identifier, neighbours);
            }
        
        return groups;
    }
    
    public static List<Integer> getNeighbourGroup(Integer identifier)
    {
        return neighbourGroups.get(identifier);
    }
    
    public static String convertIdentifierToAddress(Integer identifier)
    {
        String ipAddress = "127.0.0.1:" + String.valueOf(6000 + identifier + 1);   
        return ipAddress;
    }
}
