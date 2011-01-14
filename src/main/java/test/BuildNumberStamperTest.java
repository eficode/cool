package test;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import net.praqma.util.BuildNumberStamper;

public class BuildNumberStamperTest
{
	public static void main( String[] args ) throws IOException
	{
		File myfile = new File( "stamptest.txt" );
		
		net.praqma.util.Printer.ArrayPrinter( args );
		
		System.out.println( "MYFILE=" + myfile.exists() );
		
		BuildNumberStamper stamp = new BuildNumberStamper( myfile );
		
		stamp.stampIntoCode( "1", "2", "3", Calendar.getInstance().getTimeInMillis() + "" );
		
		System.out.println( "out!" );
	}
}
