package case_study.detectors;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DetectorsGUI extends JFrame {

    PaintPanel paintPan = null;
    
    public DetectorsGUI() {
        setTitle("Sensor network for smartphone detection");
        setSize(650, 325);
        setLayout(new BorderLayout());

        paintPan = new PaintPanel();
        add(paintPan, BorderLayout.CENTER);
        repaint();
        setVisible(true);
    }
    
    public void paintCircles(List<MobileDetector> mdList, WirelessEnvironment wirelessEnvironment) {
        paintPan.paintCircles(mdList, wirelessEnvironment);
        repaint();
    }

    public static void main(String[] args) {
        new DetectorsGUI();
    }
}

class PaintPanel extends JPanel {

    int taskID = 0;
    List<MobileDetector> mdList = null;
    WirelessEnvironment wirelessEnvironment;
    
    public PaintPanel() {
        Color backgroundColor = Color.decode("#f0f9e8");
        setBackground(backgroundColor);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D drawImage = (Graphics2D) g;
        
        double detectionRadius = MobileDetectorApp.distanceForDetection;
        // draw detectors
        if(mdList != null) { 
            for(int i=0; i<mdList.size(); i++) {
                drawImage.setPaint(Color.BLACK);
                
                MobileDetector detector = mdList.get(i);
                drawImage.drawOval((int)detector.centerCoordinates.getX() - (int)detector.radius, (int)detector.centerCoordinates.getY() - (int)detector.radius, 
                        2*(int)detector.radius, 2*(int)detector.radius);
                   
                if(detector.informedDeviceCount > 0) {
                    Color detectedByNeighborColor = Color.decode("#43a2ca");
                    drawImage.setPaint(detectedByNeighborColor);
                    drawImage.fill(new Ellipse2D.Double(detector.centerCoordinates.getX() - detectionRadius/2, detector.centerCoordinates.getY() - detectionRadius/2, detectionRadius, detectionRadius));
                }
                
                if(detector.detectedDevicesCount > 0) {
                    Color ownDetectedColor = Color.decode("#a8ddb5");
                    drawImage.setPaint(ownDetectedColor);
                    drawImage.fill(new Ellipse2D.Double(detector.centerCoordinates.getX() - detectionRadius/4, detector.centerCoordinates.getY() - detectionRadius/4, detectionRadius/2, detectionRadius/2));
                }
                
                drawImage.setPaint(Color.BLACK);
                drawImage.fill(new Ellipse2D.Double(detector.centerCoordinates.getX() - 3, detector.centerCoordinates.getY() - 3, 6.0, 6.0));
                
            }
        }  
        
        int mobileDeviceSize = 10;
        // draw mobile devices
        if(wirelessEnvironment != null) {
            Color objetColor = Color.decode("#f33000");
            drawImage.setPaint(objetColor);
            
            for(int i=0; i<wirelessEnvironment.devices.size(); i++)
            {
                MobileDevice device = wirelessEnvironment.devices.get(i);
                PointStruct currentPosition = device.getCurrentPosition();
                drawImage.drawOval((int)currentPosition.getX(), (int)currentPosition.getY(), 
                        mobileDeviceSize, mobileDeviceSize);
                drawImage.fill(new Ellipse2D.Double((int)currentPosition.getX(), (int)currentPosition.getY(), 
                        mobileDeviceSize, mobileDeviceSize));
                
            }  
        }
    }
    
    public void paintCircles(List<MobileDetector> mdList, WirelessEnvironment wirelessEnvironment) {

        this.mdList = mdList;
        this.wirelessEnvironment = wirelessEnvironment;
        repaint();
    }
    
    
}