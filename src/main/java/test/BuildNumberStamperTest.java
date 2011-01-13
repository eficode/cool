package test;

import java.io.File;
import java.io.IOException;

import net.praqma.util.BuildNumberStamper;

public class BuildNumberStamperTest
{
	public static void main( String[] args ) throws IOException
	{
		File myfile = new File( "stamptest.txt" );
		
		System.out.println( "MYFILE=" + myfile.exists() );
		
		BuildNumberStamper stamp = new BuildNumberStamper( myfile );
		
		stamp.stampIntoCode( "1" );
		
		System.out.println( "out!" );
	}
}
