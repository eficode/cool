package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
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
		
		Baseline baseline = UCMEntity.GetBaseline( obaseline.getString() );
		File dir = new File( odir.getString() );
		BuildNumber.stampIntoCode( baseline, dir );
	}
}
