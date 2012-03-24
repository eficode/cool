package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.BuildNumberException;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.HyperlinkException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class StampBuildNumber extends Cool {
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		try {
			run( args );
		} catch( ClearCaseException e ) {
			e.print( System.err );
			throw e;
		}
	}

	public static void run( String[] args ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, BuildNumberException, HyperlinkException, IOException, UnableToGetEntityException {
		Options o = new Options( net.praqma.cool.Version.version );

		Option obaseline = new Option( "baseline", "b", true, 1, "Given a Baseline, the buildnumber.file is stamped" );
		Option oignore = new Option( "ignore", "i", false, 0, "Ignore errors: 1) Files that does not exist. 2) Files that does not contain any correct version string." );
		Option odir = new Option( "directory", "d", false, 1, "The wanted working directory" );
		o.setOption( obaseline );
		o.setOption( oignore );
		o.setOption( odir );
		
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );

		o.setDefaultOptions();

		o.setSyntax( "BuildNumber -b baseline -d dir" );
		o.setHeader( "Automatically stamp a build number into the buildnumber.file given a Baseline" );

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			System.err.println( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

		boolean ignoreErrors = false;
		if( oignore.used ) {
			ignoreErrors = true;
		}

		Baseline baseline = Baseline.get( obaseline.getString() ).load();
		File dir = odir.used ? new File( odir.getString() ) : null;
		int number = BuildNumber.stampIntoCode( baseline, dir, ignoreErrors );

		/* Determine the return value */
		if( number > 0 ) {
			System.out.println( number );
			System.exit( 0 );
		} else {
			System.err.println( 0 );
			System.exit( 1 );
		}
	}
}
