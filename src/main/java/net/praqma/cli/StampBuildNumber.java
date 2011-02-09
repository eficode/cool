package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.util.debug.Logger;
import net.praqma.util.io.BuildNumberStamper;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

/*
 * Usage
 * 
 * 
 * 
 */

public class StampBuildNumber
{
	
	private static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws UCMException
	{
		Options o = new Options( net.praqma.cool.Version.version );
		
		Option obaseline = new Option( "baseline", "b", true, 1, "Given a Baseline, the buildnumber.file is stamped" );
		Option odir = new Option( "directory", "d", false, 1, "The wanted working directory" );
		o.setOption( obaseline );
		o.setOption( odir );
		
		o.setDefaultOptions();
		
		o.setSyntax( "BuildNumber -b baseline -d dir" );
		o.setDescription( "Automatically stamp a build number into the buildnumber.file given a Baseline" );
		
		o.parse( args );
		
		o.print();
		
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
		
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		Baseline baseline = UCMEntity.GetBaseline( obaseline.getString(), false );
		File dir = new File( odir.getString() );
		int number = BuildNumber.stampIntoCode( baseline, dir );
		
		/* Determine the return value */
		if( number > 0 )
		{
			System.out.println( number + " occurrence" + ( number > 1 ? "s" : "" ) + " found" );
			System.exit( 0 );
		}
		else
		{
			System.err.println( "No occurrences found" );
			System.exit( 1 );
		}
	}
}
