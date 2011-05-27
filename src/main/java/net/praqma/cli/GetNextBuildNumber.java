package net.praqma.cli;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Cool;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class GetNextBuildNumber extends Cool
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
		
		Option oproject = new Option( "project", "p", false, 1, "Retrieve the next build number given a project" );
		Option ostream  = new Option( "stream", "s", false, 1, "Retrieve the next build number given a stream" );
		o.setOption( oproject );
		o.setOption( ostream );
		
		o.setDefaultOptions();
		
		o.setSyntax( "GetNextBuildNumber [-p {project} | -s {stream}]" );
		o.setHeader( "Retrieve the next build number given a UCM project or a UCM stream." );
		
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
		
		if( !oproject.used && !ostream.used )
		{
			System.err.println( "Neither a stream nor a project was given." );
			System.exit( 1 );
		}
		
		/* ClearCase */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		Project project = null;
		
		if( oproject.used )
		{
			project = UCMEntity.getProject( oproject.getString(), false );
		}
		else
		{
			Stream stream = UCMEntity.getStream( ostream.getString(), false );
			project = stream.getProject();
		}
		
		try
		{
			String number = BuildNumber.getBuildNumber( project );
			System.out.println( number );
			System.exit( 0 );
		}
		catch( UCMException e )
		{
			System.err.println( "Could not retrieve the build number" );
			System.exit( 1 );
		}
	}
}
