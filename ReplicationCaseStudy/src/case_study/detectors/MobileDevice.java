/*
 * Created on 8May,2017
 */
package case_study.detectors;

import java.util.Random;

public class MobileDevice {

    private final int identifier;
    private double velocity = 50;
    private PointStruct currentPosition;
    private Thread lifeThread = null;
    
    // 
    private Random r = new Random();
    
    public MobileDevice(int identifier, PointStruct initialPosition)
    {
        this.identifier = identifier;
        this.currentPosition = initialPosition;
        this.lifeThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while(true)
                { 
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    PointStruct currentPosition = getCurrentPosition();
                    updatePosition(currentPosition);
                    System.out.println("mob device #" + String.valueOf(getIdentifier()) + ":" + currentPosition.toString());
                }
                
            }
        });
    }
    
//    private synchronized void setCurrentPosition(Point currentPosition)
//    {
//        this.currentPosition = currentPosition;
//    }

    private synchronized void updatePosition(PointStruct currentPosition) {
        double xShift = (r.nextDouble()*2.0 - 1.0)*velocity*1;
        double yShift = (r.nextDouble()*2.0 - 1.0)*velocity;
        
        double newXPos = currentPosition.getX() + xShift;
        double newYPos = currentPosition.getY() + yShift;
        
        newXPos = newXPos > 0? newXPos : 0.0;
        newYPos = newYPos > 0? newYPos : 0.0;
        
        currentPosition.setX(newXPos);
        currentPosition.setY(newYPos);
    }
    
    public int getIdentifier()
    {
        return identifier;
    }
    
    public synchronized PointStruct getCurrentPosition()
    {
        return this.currentPosition;
    }
    
    public void start()
    {
        lifeThread.start();
    }
}
