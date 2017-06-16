package klava;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import klava.momi.MoMiTupleItem;
import klava.topology.KlavaProcess;
import klava.topology.KlavaProcessVar;

public class Tuple implements Cloneable, java.io.Serializable { //Comparable<Tuple>

    public AtomicBoolean removed = new AtomicBoolean(false);
    /**
     * 
     */
    private static final long serialVersionUID = -2171133549648235506L;
    
    /**
     * the GUID of this tuple
     */
    protected String id;

    /**
     * Tuple items
     */
    protected Vector<Object> items;

    protected String tupleType;
    /**
     * the ids of tuples already retrieved with this template.
     */
    protected HashSet<String> already_retrieved = new HashSet<String>();

    /**
     * if already retrieved tuples' ids have to be recorded. Default: true.
     */
    protected boolean handleRetrieved = true;

    /**
     * the tuple we have matched with this tuple
     */
    protected Tuple matched = null;

    /**
     * the original template with which we matched this tuple
     */
    protected Tuple original_template = null;

    /**
     * this will be added to id, in order to be sure that two ids are different
     */
    protected static int id_counter = 1;

    public Tuple() {
        items = new Vector<Object>();
        initId();
    }

    public Tuple(Vector<Object> v) {
        items = v;
        initId();
    }

    public Tuple(Tuple t) {
        items = t.items;

        original_template = t.getOriginalTemplate();
        setMatched(t.getMatched());

        if (isHandleRetrieved())
            already_retrieved = t.already_retrieved;

        initId();
    }

    public Tuple(Object t[]) {
        items = new Vector<Object>(t.length);

        for (int i = 0; i < t.length; i++) {
            add(t[i]);
        }
        initId();
    }

    protected synchronized void initId() {
        Date date = new Date();
        long longtime = date.getTime() + Tuple.id_counter++;
        id = String.valueOf(longtime);
    }
    
    public Tuple(String tupleName, Object[] objs) {
        tupleType = tupleName;
        items = new Vector<>();
        for(int i=0; i<objs.length; i++)
        {
            add(objs[i]);
        }
            
        initId();
    }

    public void setItem(int index, Object item) {
        items.setElementAt(item, index);
    }

    public Object getItem(int index) {
        return items.elementAt(index);
    }

    // reset the tuple without creating a new tuple
    public void reset() {
        items = new Vector<Object>();
        matched = null;
        original_template = null;
    }

    public Vector<Object> getItems() {
        return items;
    }

    public String getTupleId() {
        return id;
    }

    public void setTupleId(String _id) {
        id = _id;
    }

    public Tuple getMatched() {
        return matched;
    }

    public void setMatched(Tuple t) {
        matched = t;
    }

    public Tuple getOriginalTemplate() {
        return original_template;
    }

    public void setOriginalTemplate() {
        original_template = (Tuple) clone();
        backup_original_template();
    }

    protected void backup_original_template() {
        for (int i = 0; i < length(); i++) {
            if (original_template.items.elementAt(i) instanceof TupleItem) {
                original_template.items.setElementAt(((TupleItem) (items
                        .elementAt(i))).duplicate(), i);
            }
        }
        /*
         * Notice that we have to use duplicate, because the orginal tuple items
         * of the original_template would be overwritten anyway, when calling
         * copy inside match.
         */
    }

    // reset the original template
    public void resetOriginalTemplate() {
        if (original_template != null) {
            restore_original_template();
        }

        matched = null;
    }

    protected void restore_original_template() {
        Object elem;
        TupleItem my_elem;

        for (int i = 0; i < length(); i++) {
            elem = original_template.getItem(i);
            if (elem instanceof TupleItem) {
                my_elem = ((TupleItem) (getItem(i)));
                my_elem.setValue(elem);
                // we maintain the original reference
            } else {
                setItem(i, elem); // simply copy
            }
        }
        /*
         * Notice that we have to perform a manual copy, since we cannot lose
         * the original reference to tuple items (clone would overwrite them.
         */
    }

    public void resetRetrieved() {
        already_retrieved = new HashSet<String>();
    }

    public void updateAlreadyRetrieved(Tuple t) {
        already_retrieved = t.already_retrieved;
    }

    public void addRetrieved(String _id) {
        if (!isHandleRetrieved())
            return;

        already_retrieved.add(_id);
    }

    public boolean alreadyRetrieved(String id) {
        return (already_retrieved.contains(id));
    }

    public void setHandleRetrieved(boolean b) {
        handleRetrieved = b;
    }

    public boolean isHandleRetrieved() {
        return handleRetrieved;
    }

    public Iterator<String> getAlreadyRetrieved() {
        return already_retrieved.iterator();
    }

    public int length() {
        return items.size();
    }

    public Enumeration<Object> tupleElements() {
        return items.elements();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("( ");

        for (int i = 0; i < length(); i++) {
            sb.append(getItem(i));
            if (i < length() - 1) {
                sb.append(", ");
            }
        }

        // sb.append( " ) " + getTupleId () ) ;
        sb.append(" )");

        /*
         * if (handleRetrieved () && already_retrieved != null) { Enumeration
         * retrieved = already_retrieved.keys (); sb.append (" {"); while
         * (retrieved.hasMoreElements ()) { sb.append (retrieved.nextElement
         * ()); if (retrieved.hasMoreElements ()) sb.append (", "); } sb.append
         * ("}"); }
         */

        return sb.toString();
    }

    public void clear() {
        items = new Vector<Object>();
    }

    /**
     * Adds an element to the tuple.
     * 
     * If the element is a TupleItem and it is not formal, then a copy will be
     * actually inserted, so that the original reference can then be reused
     * without problems of interfering with the one inserted in this tuple.
     * 
     * @param o
     */
    public void add(Object o) {
        if (o instanceof TupleItem) {
            TupleItem tupleItem = (TupleItem) o;

            /*
             * if it's not formal, make sure we insert a copy, otherwise, if the
             * original reference is reset, also the element inserted here will
             * be reset
             */
            if (!tupleItem.isFormal()) {
                items.addElement(tupleItem.duplicate());
                return;
            }
        }
        items.addElement(o);
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        Tuple new_tuple = new Tuple((Vector<Object>) items.clone());
        new_tuple.id = id; // also the id is copied
        new_tuple.tupleType = tupleType;
        return new_tuple;
    }

    public void copy(Tuple t) {
        copy_elems(t);

        setMatched(t);

        if (isHandleRetrieved())
            addRetrieved(t.getTupleId());
    }

    /**
     * Copy the elements of the passed tuple in this tuple (except for the
     * original template).
     * 
     * @param t
     */
    public void copy_tuple(Tuple t) {
        copy_elems(t);

        setMatched(t.getMatched());

        if (isHandleRetrieved())
            already_retrieved = t.already_retrieved;
    }

    protected void copy_elems(Tuple t) {
        Object elem;
        Object my_elem;

        for (int i = 0; i < length(); i++) {
            elem = t.getItem(i);
            my_elem = getItem(i);
            if (my_elem instanceof TupleItem) {
                ((TupleItem) my_elem).setValue(elem);
            } else {
                // simple copy
                setItem(i, elem);
            }
        }
    }

    public boolean preMatch(Tuple toBeMatched) {
        if (length() != toBeMatched.length()) {
            return false;
        }

        if (toBeMatched.isHandleRetrieved())
            return (!toBeMatched.alreadyRetrieved(getTupleId()));

        return true;
    }

    public boolean match(Tuple template) {
        if (!preMatch(template)) {
            return false;
        }

        // we make operations on a copy of the original tuple
        Tuple t;

        t = (Tuple) template.clone();
        t.id = id; // we also set the id of this tuple

        for (int i = 0; i < length(); i++) {
            Object MyElement = getItem(i);
            Object ItsElement = t.getItem(i);

            // we skip bad tuple items
            if (MyElement == null)
                continue;

            if (ItsElement == null)
                return false;

            if (ItsElement instanceof Class
                    && ItsElement.equals(MyElement.getClass())) {
                try {
                    if (!(ItsElement.equals(getClass()))) {
                        // for security fields of tuple type are not allowed
                        t.setItem(i, MyElement);
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
            } else if (ItsElement instanceof TupleItem) {
                TupleItem itsElement = (TupleItem) ItsElement;
                if (itsElement.isFormal()) {
                    if (itsElement instanceof MoMiTupleItem) {
                        MoMiTupleItem momi_item = (MoMiTupleItem) itsElement;
                        if (!((MoMiTupleItem) MyElement).getType().subtype(
                                momi_item.getType()))
                            return false;
                    } else if (itsElement instanceof KlavaProcessVar) {
                        if (!(MyElement instanceof KlavaProcess))
                            return false;
                    } else if (!MyElement.getClass().getName().equals(
                            ItsElement.getClass().getName())) {
                        return false;
                    }
                    t.setItem(i, MyElement);
                } else if (ItsElement.getClass().equals(MyElement.getClass())
                        && ItsElement.equals(MyElement)) {
                    continue;
                } else
                    return false;
            } else if (!ItsElement.getClass().equals(MyElement.getClass())) {
                return false;
            } else if ((ItsElement.equals(MyElement))
                    || (MyElement instanceof Tuple
                            && ItsElement instanceof Tuple && ((Tuple) MyElement)
                            .match((Tuple) ItsElement))) {
                continue;
            } else {
                return false;
            }
        }

        // first we save the orginal template
        template.setOriginalTemplate();
        // we update the original
        template.copy(t);
        return true;
    }
    
    /*
    public boolean match(Tuple template) {
        if (!preMatch(template)) {
            return false;
        }

        // we make operations on a copy of the original tuple
        Tuple t;

        t = (Tuple) template.clone();
        t.id = id; // we also set the id of this tuple

        for (int i = 0; i < length(); i++) {
            Object MyElement = getItem(i);
            Object ItsElement = t.getItem(i);

            // we skip bad tuple items
            if (MyElement == null)
                continue;

            if (ItsElement == null)
                return false;

            if (ItsElement instanceof Class
                    && ItsElement.equals(MyElement.getClass())) {
                try {
                    if (!(ItsElement.equals(getClass()))) {
                        // for security fields of tuple type are not allowed
                        t.setItem(i, MyElement);
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
            } else if (ItsElement instanceof TupleItem) {
                TupleItem itsElement = (TupleItem) ItsElement;
                if (itsElement.isFormal()) {
                    if (itsElement instanceof MoMiTupleItem) {
                        MoMiTupleItem momi_item = (MoMiTupleItem) itsElement;
                        if (!((MoMiTupleItem) MyElement).getType().subtype(
                                momi_item.getType()))
                            return false;
                    } else if (itsElement instanceof KlavaProcessVar) {
                        if (!(MyElement instanceof KlavaProcess))
                            return false;
                    } else if (!MyElement.getClass().getName().equals(
                            ItsElement.getClass().getName())) {
                        return false;
                    }
                    t.setItem(i, MyElement);
                } else if (ItsElement.getClass().equals(MyElement.getClass())
                        && ItsElement.equals(MyElement)) {
                    continue;
                } else
                    return false;
            } else if (!ItsElement.getClass().equals(MyElement.getClass())) {
                return false;
            } else if ((ItsElement.equals(MyElement))
                    || (MyElement instanceof Tuple
                            && ItsElement instanceof Tuple && ((Tuple) MyElement)
                            .match((Tuple) ItsElement))) {
                continue;
            } else {
                return false;
            }
        }

        // first we save the orginal template
        template.setOriginalTemplate();
        // we update the original
        template.copy(t);
        return true;
    }*/

    /**
     * Calls equals on each element. Also checks the identifier.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof Tuple))
//            return false;
//
//        Tuple t = (Tuple) obj;
//
//        if (length() != t.length())
//            return false;
//        
//        int comparisionResult = this.compareTo(t);
//        
//        return (comparisionResult == 0);
//    }

    /**
     * if it's a tuple representation removes ( and )
     * 
     * @param s
     * @return
     */
    public static String cleanString(String s) {
        // if it's a tuple representation remove ( and )
        if (s == null)
            return s;

        s = s.trim();
        if (s != null && s.startsWith("(") && s.endsWith(")")) {
            String n = s.substring(1, s.length() - 1).trim();
            return n;
        } else
            return s;
    }
    
    public String getTupleType()
    {
        return this.tupleType;
    }
    
    public void setTupleType(String tupleType)
    {
        this.tupleType = tupleType;       
    }

//    @Override
//    public int compareTo(Tuple tupleToCompare) {
//        int lengthDifference = this.length() - tupleToCompare.length();
//        
//        if(lengthDifference == 0)
//        {
//            for(int i=0; i<this.length(); i++)
//            {
//                if(mask!= null && mask[i] == false)
//                    continue;
//                Comparable<Object> obj1 = (Comparable<Object>) this.getItem(i);
//                Comparable<Object> obj2 = (Comparable<Object>) tupleToCompare.getItem(i);
//                int comparisionResult = obj1.compareTo(obj2);
//                if(comparisionResult == 0)
//                    continue;
//                else 
//                    return comparisionResult;
//            }
//        }
//        else if(lengthDifference < 0)
//            return -1;
//        else if (lengthDifference > 0)
//            return 1;
//
//        return 0;
//    }
}
