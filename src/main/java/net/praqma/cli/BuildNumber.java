package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.util.io.BuildNumberStamper;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

/*
 * Usage
 * java -classpath COOL-0.1.5.jar net.praqma.cli.BuildNumber -f stamptest.txt -m 12 -p 1234 -s 22221 --minor 22b
 * 
 * 
 */

public class BuildNumber
{
	
	public static void main( String[] args )
	{
		Options o = new Options( net.praqma.cool.Version.version );
		
		Option omajor    = new Option( "major", "m", false, 1, "The major version of the change set to stamp" );
		Option ominor    = new Option( "minor", "i", false, 1, "The minor version of the change set to stamp" );
		Option opatch    = new Option( "patch", "p", false, 1, "The patch version of the change set to stamp" );
		Option osequence = new Option( "sequence", "s", false, 1, "The sequence version of the change set to stamp" );
		Option ofile     = new Option( "file", "f", true, 1, "The file to stamp" );
		
		o.setOption( omajor );
		o.setOption( ominor );
		o.setOption( opatch );
		o.setOption( osequence );
		o.setOption( ofile );
		
		o.setDefaultOptions();
		
		o.setSyntax( "BuildNumber <options> -f file" );
		o.setDescription( "Automatically stamp a build number into a source/header file." + Options.linesep + "For example:" + Options.linesep + "private static final String major = \"0\"; // buildnumber.major" );
		
		o.parse( args );
		
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
		
		if( o.verbose() )
		{
			o.print();
		}
		
		try
		{
			stamp.stampIntoCode( omajor.getString(), ominor.getString(), opatch.getString(), osequence.getString() );
		}
		catch ( IOException e )
		{
			System.err.println( "Could not edit file: " + e.getMessage() );
			System.exit( 1 );
		}
	}
}
