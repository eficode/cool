package test;

import java.io.File;
import java.util.Calendar;

import net.praqma.util.io.BuildNumberStamper;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class BuildNumberStamperTest
{
	public static void main( String[] args ) throws Exception
	{
		File myfile = new File( "stamptest.txt" );
		
		net.praqma.util.structure.Printer.arrayPrinter( args );
		
		Options o = new Options();
		
		Option o1 = new net.praqma.util.option.Option( "major", "", true, 1 );
		Option o2 = new net.praqma.util.option.Option( "minor", "", true, 1 );
		Option o3 = new net.praqma.util.option.Option( "patch", "p", true, 1 );
		Option o4 = new net.praqma.util.option.Option( "do", "", false, 1 );
		
		o.setOption( o1 );
		o.setOption( o2 );
		o.setOption( o3 );
		o.setOption( o4 );
		
		//o.print();
		
		o.parse( args );
		o.checkOptions();
		
		o.print();
		
		System.out.println( "MYFILE=" + myfile.exists() );
		
		BuildNumberStamper stamp = new BuildNumberStamper( myfile );
		
		stamp.stampIntoCode( o1.getString(), o2.getString(), o3.getString(), Calendar.getInstance().getTimeInMillis() + "" );
		
		System.out.println( "out!" );
	}
}
