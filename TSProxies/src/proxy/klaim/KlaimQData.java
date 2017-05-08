package proxy.klaim;

import klava.KlavaException;
import klava.TupleItem;

// Customized String

public class KlaimQData implements TupleItem {

    /**
     * 
     */
    private static final long serialVersionUID = 1258252785854239806L;

 //   public String string;
    public Integer[] data;

    public KlaimQData() {
    	data = null;
    }


    public KlaimQData(Integer[] data) {
    	data = new Integer[1];
    	data[0] = 11;
    			
    	//strValue = new String(s);
    }


    public String toString() {
        return "";
    }



    /**
     * @see klava.TupleItem#isFormal()
     */
    public boolean isFormal() {
        return (data == null);
    }


    /**
     * @see klava.TupleItem#setValue(java.lang.Object)
     */
    public void setValue(Object o) {
    	data = ((KlaimQData) o).data;
//        try {
//            if (o != null) {
//                String s = ((KlaimQData) o).string;
//                if (s == null)
//                    string = null;
//                else
//                    string = new String(s);
//            }
//        } catch (ClassCastException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
//        if (o == null)
//            return false;
//        
//        if (isFormal()) {
//            if (o instanceof KlaimQData) {
//            	KlaimQData new_name = (KlaimQData) o;
//                if (new_name.isFormal())
//                    return true;
//            }
//        }
//        
//        return string.equals(o.toString());
    	return true;
    }


    public Object duplicate() {
    	KlaimQData newObj = new KlaimQData(data);
        return newObj;
    }


	public void setValue(String o) throws KlavaException {
		data = null;
		
	}
	
	
	public static String convertArrayToString(int[] array)
	{
		StringBuilder buf = new StringBuilder(array.length);
		if(array == null || array.length == 0)
			return "";
		else{
			for(int i=0; i<array.length; i++)
			{
				buf.append(array[i] + ";");
			}
		}
		
		return buf.toString();
	}
	
	
	public static int[] convertStringToArray(String str)
	{
		if(str == null)
			return null;
		else{
			String[] strArray = str.split(";");
			
			int result[] = new int[strArray.length];
	        for (int i = 0; i < result.length; i++) {
	          result[i] = Integer.parseInt(strArray[i]);
	        }
		    return result;
			
		}
	}
	
	
	public static String convertIntegerArrayToString(Integer[] array)
	{
		StringBuilder buf = new StringBuilder(array.length);
		if(array == null || array.length == 0)
			return "";
		else{
			for(int i=0; i<array.length; i++)
			{
				buf.append(array[i] + ";");
			}
		}
		
		return buf.toString();
	}
	
	
	public static Integer[] convertStringToIntegerArray(String str)
	{
		if(str == null)
			return null;
		else{
			String[] strArray = str.split(";");
			
			Integer result[] = new Integer[strArray.length];
	        for (int i = 0; i < result.length; i++) {
	          result[i] = Integer.parseInt(strArray[i]);
	        }
		    return result;
			
		}
	}

//	public static String convertArrayToString(int[] array)
//	{ 
//		return Arrays.toString(array);
//	}
//	
//	public static int[] convertStringToArray(String str)
//	{
//		String[] strings = str.replace("[", "").replace("]", "").replace(" ", "").replace("\n", "").split(",");
//		 
//		int j = 0;
//		Integer[] array = Arrays.stream(strings).map(t -> Integer.parseInt(t)).toArray(Integer[]::new);
//		return toPrimitive(array);
//		
//		
//		//return DataGeneration.fromStringToIntArray(str);
//	}
//	

	
	
	
	
	
	
	public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return null;
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].intValue();
        }
        return result;
    }
	
}
