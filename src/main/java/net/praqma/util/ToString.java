package net.praqma.util;

public class ToString
{
	public static <T1> String Array( T1[] array )
	{
		return Array( array, null );
	}
	
	public static <T1> String Array( T1[] array, Integer max )
	{
		Integer c = 1;
		StringBuffer sb = new StringBuffer();
	    for( T1 t : array )
	    {
	    	if( max != null && c > max ) break;
	    	//System.out.println( "(" + t.toString() + ") " );
	    	sb.append( t.toString() + " " );
	    	c++;
	    }
	    
	    return sb.toString();
	}
}
