package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.util.BuildNumberStamper;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class BuildNumber
{
	
	public static void main( String[] args )
	{
		Options o = new Options();
		
		Option omajor    = new Option( "major", "m", true, 1, "The major version of the change set to stamp" );
		Option ominor    = new Option( "minor", "i", true, 1, "The minor version of the change set to stamp" );
		Option opatch    = new Option( "patch", "p", true, 1, "The patch version of the change set to stamp" );
		Option osequence = new Option( "sequence", "s", true, 1, "The sequence version of the change set to stamp" );
		Option ofile     = new Option( "file", "f", false, 1, "The file to stamp" );
		Option ohelp     = new Option( "help", "h", true, 0, "The help" );
		
		o.setOption( omajor );
		o.setOption( ominor );
		o.setOption( opatch );
		o.setOption( osequence );
		o.setOption( ofile );
		
		o.parse( args );
		
		if( ohelp.used )
		{
			o.display();
			System.exit( 0 );
		}
		
		try
		{
			o.checkOptions();
		}
		catch ( Exception e )
		{
			System.err.println( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}
		
		File file = new File( ofile.getString() );
		
		System.out.println( "Stamping file " + file );
		
		BuildNumberStamper stamp = null;
		try
		{
			stamp = new BuildNumberStamper( file );
		}
		catch ( IOException e )
		{
			System.err.println( "Could not create temporary file" );
			System.exit( 1 );
		}
		
		try
		{
			stamp.stampIntoCode( omajor.getString(), ominor.getString(), opatch.getString(), osequence.toString() );
		}
		catch ( IOException e )
		{
			System.err.println( "Could not edit file" );
			System.exit( 1 );
		}
	}
}
