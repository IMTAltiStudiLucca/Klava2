package axillary;

import common.TupleLogger;
import klava.Tuple;
import klava.TupleSpace;

public class OperationWrapper {
	public static void out(TupleSpace space, Tuple tuple, boolean isLogged){
		if(isLogged) {
	        TupleLogger.begin("out::local");
			space.out(tuple);
	        TupleLogger.end("out::local");
		} else
			space.out(tuple);
        return;
	}
	
	public static void read(TupleSpace space, Tuple template, boolean isLogged) throws InterruptedException{
		if(isLogged) {
	        TupleLogger.begin("read::local");
			space.read(template);
	        TupleLogger.end("read::local");
		} else
			space.read(template);
		return;
	}
	
	public static void in(TupleSpace space, Tuple template, boolean isLogged) throws InterruptedException{
		if(isLogged) {
	        TupleLogger.begin("in::local");
			space.in(template);
	        TupleLogger.end("in::local");
		} else
			space.read(template);
		return;
	}
	
	public static boolean read_nb(TupleSpace space, Tuple template, boolean isLogged) throws InterruptedException{
		boolean res = false;
		if(isLogged) {
	        TupleLogger.begin("read_nb::local");
	        res = space.read_nb(template);
	        TupleLogger.end("read_nb::local");
		} else
			res = space.read_nb(template);
		return res;
	}
	
	public static boolean in_nb(TupleSpace space, Tuple template, boolean isLogged) throws InterruptedException{
		boolean res = false;
		if(isLogged) {
	        TupleLogger.begin("in_nb::local");
	        res = space.in_nb(template);
	        TupleLogger.end("in_nb::local");
		} else
			res = space.in_nb(template);
		return res;
	}
}
