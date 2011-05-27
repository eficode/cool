package net.praqma.cli;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Cool;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

/*
 * Usage
 * 
 * 
 * 
 */

public class StampBuildNumber extends Cool
{
	
	public static void main( String[] args ) throws UCMException
	{
		try
		{
			run( args );
		}
		catch( UCMException e )
		{
			System.err.println( UCM.getMessagesAsString() );
			throw e;
		}
	}
	
	public static void run( String[] args ) throws UCMException
	{
		Options o = new Options( net.praqma.cool.Version.version );
		
		Option obaseline = new Option( "baseline", "b", true, 1, "Given a Baseline, the buildnumber.file is stamped" );
		Option oignore   = new Option( "ignore", "i", false, 0, "Ignore errors: 1) Files that does not exist. 2) Files that does not contain any correct version string." );
		Option odir = new Option( "directory", "d", false, 1, "The wanted working directory" );
		o.setOption( obaseline );
		o.setOption( oignore );
		o.setOption( odir );
		
		o.setDefaultOptions();
		
		o.setSyntax( "BuildNumber -b baseline -d dir" );
		o.setHeader( "Automatically stamp a build number into the buildnumber.file given a Baseline" );
		
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
		
		boolean ignoreErrors = false;
		if( oignore.used )
		{
			ignoreErrors = true;
		}
		
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		Baseline baseline = UCMEntity.getBaseline( obaseline.getString(), false );
		File dir = odir.used ? new File( odir.getString() ) : null;
		int number = BuildNumber.stampIntoCode( baseline, dir, ignoreErrors );
		
		/* Determine the return value */
		if( number > 0 )
		{
			System.out.println( number );
			System.exit( 0 );
		}
		else
		{
			System.err.println( 0 );
			System.exit( 1 );
		}
	}
}
