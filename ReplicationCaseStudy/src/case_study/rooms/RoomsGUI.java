package case_study.rooms;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import case_study.rooms.Sensor.eSensorType;

public class RoomsGUI extends JFrame {

    PaintPanel paintPan = null;
    public RoomsGUI() {
        setTitle("Smart home system");
        setSize(1000, 400);
        setLayout(new BorderLayout());

        paintPan = new PaintPanel();
        add(paintPan, BorderLayout.CENTER);
        
//        JButton testButon = new JButton("Display shape");
//        add(testButon, BorderLayout.PAGE_END);     
        //paintPan.updateGraphics(50, 50);
        
        repaint();

//        testButon.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
//                //paintPan.updateGraphics(50, 50);
//                repaint();
//            }
//        });
        setVisible(true);
    }
    
    public void drawRooms(ArrayList<Room> roomList) {
        paintPan.drawRooms(roomList);
        repaint();
    }
}

class PaintPanel extends JPanel {

    private int x, y;
    private Color color = null;
    
    
    int taskID = 0;
    ArrayList<Room> roomList; 
    int radius;
    
    // format
    private static DecimalFormat df1 = new DecimalFormat("#.#");
    
    public PaintPanel() {
        Color backgroundColor = Color.decode("#f0f9e8");
        setBackground(backgroundColor);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D drawImage = (Graphics2D) g;
        
        int roomSize = 300;
        int sensorSize = 50;

        if(roomList != null) {
            for(int r=0; r<roomList.size(); r++) {
                
                int x = r*(roomSize + 15) + 10;
                y = 10;
                drawImage.setStroke(new BasicStroke(4));
                drawImage.drawRect(x, y, roomSize, roomSize);
                drawImage.setStroke(new BasicStroke(2));
                
                Font currentFont = g.getFont();
                g.setFont(currentFont.deriveFont(17f));
                String rommName = "Room #" + String.valueOf(r+1);
                drawImage.drawString(rommName, x + roomSize/2 - 35, roomSize/3);
                g.setFont(currentFont);
                
                int thermometerCounter = 0;
                for(int s=0; s<roomList.get(r).sensors.size(); s++) {
                    Sensor sensor = roomList.get(r).sensors.get(s);
                     
                    if(sensor.getType() == eSensorType.ALERT) {
                      
                        int xSensor = x + roomSize/2 - (int)(1.5*sensorSize);
                        int ySensor = y + roomSize/2 - sensorSize/2;
                        drawImage.drawRect(xSensor, ySensor, sensorSize, sensorSize);
                        //drawImage.drawChars(new char[]{'A'}, 0, 1, xSensor + 2, (int)(ySensor + 12));
                        if(((AlertSensor)sensor).alertState.get()) {
                              Color alertColor = Color.decode("#f33000");
                              drawImage.setPaint(alertColor);
                              drawImage.fillRect(xSensor, ySensor, sensorSize, sensorSize);
                        }
                        // add text
                        drawImage.setPaint(Color.BLACK);
                        drawImage.drawChars(new char[]{'A'}, 0, 1, xSensor + 2, (int)(ySensor + 12));
    
                    }
                  
                    if(sensor.getType() == eSensorType.CONTROLLER) {       
                      
                        int xSensor = x + roomSize/2 + sensorSize/2;
                        int ySensor = y + roomSize/2 - sensorSize/2;
                        drawImage.drawRect(xSensor, ySensor, sensorSize, sensorSize);

                        if(((ControllerSensor)sensor).isDetected) {
                            Color controllerColor = Color.decode("#4575b4");
                            drawImage.setPaint(controllerColor);
                            drawImage.fillRect(xSensor, ySensor, sensorSize, sensorSize);
                        }
                        // add text
                        drawImage.setPaint(Color.BLACK);
                        drawImage.drawChars(new char[]{'C'}, 0, 1, xSensor + 2, (int)(ySensor + 12));
                      
                        double temperature = ((ControllerSensor)sensor).averageTemperatureGlobal;
                        String strTemp = df1.format(temperature) + "\u00b0" + "C";
                        char[] tempStr = String.valueOf(strTemp).toCharArray();
                        drawImage.drawChars(tempStr, 0, tempStr.length, xSensor + sensorSize/7, (int)(ySensor + sensorSize*0.6));
                    }
                  
                    if(sensor.getType() == eSensorType.THERMOMETER) {
                        int xSensor = (thermometerCounter == 1 || thermometerCounter == 3) ? x + 10 : x + roomSize - sensorSize - 10;
                        int ySensor = (thermometerCounter == 1 || thermometerCounter == 2) ? y + 10 : y + roomSize - sensorSize - 10;
                        drawImage.drawRect(xSensor, ySensor, sensorSize, sensorSize);
                        drawImage.drawChars(new char[]{'T'}, 0, 1, xSensor + 2, (int)(ySensor + 12));
                        double temperature = ((TemperatureSensor)sensor).currentTemperature;
                        String strTemp = df1.format(temperature) + "\u00b0" + "C";
                        char[] tempStr = String.valueOf(strTemp).toCharArray();
                        drawImage.drawChars(tempStr, 0, tempStr.length, xSensor + sensorSize/7, (int)(ySensor + sensorSize*0.6));
                      
                        thermometerCounter++;
                    }
                  
                    drawImage.setPaint(Color.BLACK);
                  
                  //char firstChar = sensor.getType().toString().charAt(0);
            //      drawImage.drawChars(new char[]{firstChar}, 0, 1, xSensor + 2, (int)(ySensor + 12));
                    
                    
                    
//                    drawImage.drawRect(xSensor, ySensor, sensorSize, sensorSize);
//                    
//                    if(sensor.getType() == eSensorType.ALERT && ((AlertSensor)sensor).alertState.get()) {
//                        Color alertColor = Color.decode("#f33000");
//                        drawImage.setPaint(alertColor);
//                        drawImage.fill(new Rectangle2D.Double(xSensor, ySensor, sensorSize, sensorSize));
//                    }
//                    
//                    if(sensor.getType() == eSensorType.CONTROLLER && ((ControllerSensor)sensor).isDetected) {        
//                        Color controllerColor = Color.decode("#4575b4");
//                        drawImage.setPaint(controllerColor);
//                        drawImage.fill(new Rectangle2D.Double(xSensor, ySensor, sensorSize, sensorSize));
//                    }
//                    
//                    if(sensor.getType() == eSensorType.THERMOMETER) {
//                        double temperature = ((TemperatureSensor)sensor).currentTemperature;
//                        char[] tempStr = String.valueOf(temperature).toCharArray();
//                        drawImage.drawChars(tempStr, 0, tempStr.length > 4 ? 4 : tempStr.length, xSensor + sensorSize/4, (int)(ySensor + sensorSize*0.75));
//                    }
//                    
//                    drawImage.setPaint(Color.BLACK);
//                    
//                    char firstChar = sensor.getType().toString().charAt(0);
//                    drawImage.drawChars(new char[]{firstChar}, 0, 1, xSensor + 2, (int)(ySensor + 12));
                         
                }
                
            }
        }

    }
   
    public void drawRooms(ArrayList<Room> roomList) {

        this.roomList = roomList;
        repaint();
    }
    
    
}