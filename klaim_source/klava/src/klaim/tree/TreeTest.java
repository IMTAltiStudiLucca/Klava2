/*
 * Created on 30May,2017
 */
package klaim.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import klava.Tuple;

public class TreeTest {

    public static void main(String[] args) throws InterruptedException {
        TupleSpaceTree space = new TupleSpaceTree();
        
        Hashtable<String, List<Object>>  settings = new Hashtable<>();
        
        settings.put("str_str_int", new ArrayList<Object>(Arrays.asList(new Boolean[]{true, true, false}, new Boolean[]{true, false, true})));
        space.setSettings(settings);
        
        Tuple tuple = new Tuple("str_str_int", new Object[]{"number", "index", 12});
        space.out(tuple);
        
        Tuple template1 = new Tuple("str_str_int", new Object[]{"number", "index", Integer.class});
        space.read(template1);
        System.out.println(template1);
        
        Tuple template2 = new Tuple("str_str_int", new Object[]{"number", String.class, 12});
        space.read(template2);
        System.out.println(template2);

    }

}
