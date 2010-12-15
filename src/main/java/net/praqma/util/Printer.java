package net.praqma.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Printer
{

	public static <T1> void ArrayPrinter ( T1[] array )
	{
		ArrayPrinter( array, null );
	}
	
	public static <T1> void ArrayPrinter ( T1[] array, Integer max )
	{
		Integer c = 1;
	    for( T1 t : array )
	    {
	    	if( max != null && c > max ) break;
	    	System.out.println( "(" + t.toString() + ") " );
	    	c++;
	    }
	}
	
	public static <T1, T2> void HashMapPrinter ( HashMap<T1, T2> hm )
	{
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<T1, T2>> it = hm.entrySet().iterator();
	    while( it.hasNext() )
	    {
	    	Map.Entry<T1, T2> entry = (Map.Entry<T1, T2>)it.next();
	    	System.out.print( "(" + entry.getKey().toString() + ", " + entry.getValue().toString() + ")" );
	    	if( it.hasNext() ) System.out.print( ", " );
	    }
	    
	    System.out.println( "" );
	}
	
	public static <T> void ListPrinter ( List<T> list )
	{
	    for( T t : list )
	    {
	    	System.out.print( "(" + t.toString() + ") " );
	    }
	    
	    System.out.println( "" );
	}
}
