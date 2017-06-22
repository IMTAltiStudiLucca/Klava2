/*
 * Created on 8May,2017
 */
package case_study.detectors;

public class PointStruct {

    private double x;
    private double y;
    
    public PointStruct(double x, double y)
    {
        this.setX(x);
        this.setY(y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
    
    public String toString() {
        
        return "(" + String.valueOf(x) + ", " + String.valueOf(y) + ")";
    }
    
    public static Double getDistanceBetweenPoints(PointStruct first, PointStruct second)
    {
        if(first == null || second == null)
            return null;
        else {
            double distance = Math.sqrt( Math.pow(first.x-second.x, 2) + Math.pow(first.y-second.y, 2));
            return distance;
        }
    }
}
