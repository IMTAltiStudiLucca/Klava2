package common;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;


public class DataGeneration {
    public static void main(String[] args) {
    /*	        
    	int numberOfElements = 100000;
        int[] array = generateIntArray(numberOfElements, 10000);
        
        QSort.inverseQuickSort(array, 0, array.length - 1);
        long starttime = System.currentTimeMillis();
    //    QSort.inverseQuickSort(array, 0, array.length - 1);
  //      QSort.quickSort(array, 0, array.length - 1);
        long time = System.currentTimeMillis() - starttime;
        
        System.out.println("Time = " + time);
        
        writeStringToFile("data_" + numberOfElements +".dat", Arrays.toString(array));
        
        
        try {
			String data = readStringFromFile("data.dat");
			int[] newArray = fromStringToIntArray(data);
	//        System.out.println(Arrays.toString(newArray));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("Task is completed");*/
    }
    
    
    /**
     * generate array of int
     * @param numberOfElements
     * @param maxValue
     * @return
     */
    public static int[] generateIntArray(int numberOfElements, int maxValue)
    {
        Random r = new Random();
        
        int[] array = new int[numberOfElements];
        for(int i=0; i < numberOfElements; i++)
        {
        	int number = r.nextInt(maxValue);
        	array[i] = number;
        }
        return array;	
    }
    
    /**
     * write string to file
     * @param fileName
     * @param string
     * @return
     */
    public static boolean writeStringToFile(String fileName, String string)
    {
    	 BufferedWriter outputWriter = null;
         try {
    	 	outputWriter = new BufferedWriter(new FileWriter(fileName));
 			outputWriter.write(string);
 			outputWriter.flush();
 			return true;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} finally {
// 			try {
//				outputWriter.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}	
    }
    
    public static String readStringFromFile(String fileName) throws IOException
    {
    	BufferedReader br = null;
    	
    	try {
    		br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            return sb.toString();
        } 
    	finally {
            br.close();
        } 
    }
    
    public static ArrayList<String> readFromFile(String fileName) throws IOException
    {
    	BufferedReader br = null;
    	
    	try {
    		br = new BufferedReader(new FileReader(fileName));
    		ArrayList<String> sb = new ArrayList<String>();
            String line = br.readLine();

            while (line != null) {
                sb.add(line);
                line = br.readLine();
            }
            return sb;
        } 
    	finally {
            br.close();
        } 
    }
    
    public static String readLineStringFromFile(String fileName) throws IOException
    {
    	BufferedReader br = null;
    	
    	try {
    		br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        } 
    	finally {
            br.close();
        } 
    }
    
    
    public static int[] fromStringToIntArray(String string) {
        String[] strings = string.replace("[", "").replace("]", "").replace(" ", "").replace("\n", "").split(",");
        int result[] = new int[strings.length];
        for (int i = 0; i < result.length; i++) {
          result[i] = Integer.parseInt(strings[i]);
        }
        return result;
      }
    
    
    public static String serializeObject(Object obj)
    {
    	 // serialize the object
    	 try {
    	     ByteArrayOutputStream bo = new ByteArrayOutputStream();
    	     ObjectOutputStream so = new ObjectOutputStream(bo);
    	     so.writeObject(obj);
    	     so.flush();
    	     
    	     byte[] objectInBytes = bo.toByteArray();
    	     
    	     StringBuffer buffer = new StringBuffer();
    	     final int partSize = 9000;
    	     int partCounter = objectInBytes.length / partSize;
    	     for(int i = 0; i < partCounter + 1; i++)
    	     {
    	    	 int leftBorder = i*partSize;
    	    	 int rightBorder = i == partCounter ? (objectInBytes.length) : (leftBorder + partSize);
    	    	 byte[] bufferPart = Arrays.copyOfRange(objectInBytes, leftBorder, rightBorder);
    	    	 String serializedObjectPart = Base64.getEncoder().encodeToString(bufferPart);
    	    	 buffer.append(serializedObjectPart);
    	     }
    	    // serializedObject = Base64.getEncoder().encodeToString(bo.toByteArray());
    	     
    	     String serializedObject = buffer.toString();
    	     return serializedObject;
    	 } catch (Exception e) {
    	     System.out.println(e);
    	     return null;
    	 }
    }

    
    public static Object deserializeObject(String serializedObject)
    {
    	try {
    		byte[] initialByteArray = serializedObject.getBytes();
   	     	final int partSize = 300000;
   	     	int partCounter = initialByteArray.length / partSize;
   	     	ArrayList<byte[]> decodedParts = new ArrayList<byte[]>();
   	     	int lengthDecodedArray = 0;
   	        	     
    		for(int i = 0; i < partCounter + 1; i++)
    		{
    			int leftBorder = i*partSize;
   	    	 	int rightBorder = i == partCounter ? (initialByteArray.length): (leftBorder + partSize);
   	    	 	byte[] bufferPart = Arrays.copyOfRange(initialByteArray, leftBorder, rightBorder);
   	    	 	byte decodedPart[] = Base64.getDecoder().decode(bufferPart); 
   	    	 	decodedParts.add(decodedPart);
    	 		lengthDecodedArray += decodedPart.length;
    		}
    		
    		// construct decoded byte array
    		byte[] decodedArray = new byte[lengthDecodedArray];
   	     	ByteBuffer target = ByteBuffer.wrap(decodedArray);
    		for(int i = 0; i < decodedParts.size(); i++)
    			target.put(decodedParts.get(i));
    		
    		ByteArrayInputStream bi = new ByteArrayInputStream(decodedArray);
    		ObjectInputStream si = new ObjectInputStream(bi);
    		Object obj = si.readObject();
     		return obj;
    	} catch (Exception e) {
    	     System.out.println(e);
    	     return null;
    	}
    }  
    
    
//    public static Object deserializeObject(String serializedObject)
//    {
//    	try {
//    	     byte b[] = Base64.getDecoder().decode(serializedObject.getBytes());  //serializedObject.getBytes(); 
//    	     ByteArrayInputStream bi = new ByteArrayInputStream(b);
//    	     ObjectInputStream si = new ObjectInputStream(bi);
//    	     Object obj = si.readObject();
//    	     return obj;
//    	 } catch (Exception e) {
//    	     System.out.println(e);
//    	     return null;
//    	 }
//    }    
    
    
}
