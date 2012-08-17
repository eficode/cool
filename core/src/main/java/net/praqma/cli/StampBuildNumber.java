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
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class StampBuildNumber extends Cool {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Attempting to execute run method with the given args...");
		
		try {
			run( args );
		} catch( ClearCaseException e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			e.print( System.err );
			throw e;
		}
		tracer.finest("Successfully completed run method");
		tracer.finest("Ending execution of method - main");
	}

	public static void run( String[] args ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, BuildNumberException, HyperlinkException, IOException, UnableToGetEntityException, UnableToInitializeEntityException {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options( net.praqma.cool.Version.version );

		Option obaseline = new Option( "baseline", "b", true, 1, "Given a Baseline, the buildnumber.file is stamped" );
		Option oignore = new Option( "ignore", "i", false, 0, "Ignore errors: 1) Files that does not exist. 2) Files that does not contain any correct version string." );
		Option odir = new Option( "directory", "d", false, 1, "The wanted working directory" );
		o.setOption( obaseline );
		o.setOption( oignore );
		o.setOption( odir );
		
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        tracer.finest("Setting default options.");

		o.setDefaultOptions();

		o.setSyntax( "BuildNumber -b baseline -d dir" );
		o.setHeader( "Automatically stamp a build number into the buildnumber.file given a Baseline" );

		tracer.finest("Parsing args to Options.");
		
		o.parse( args );

		tracer.finest("Attepmting to load Options...");
		
		try {
			o.checkOptions();
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			System.err.println( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Checking if ignore option was given.");
		
		boolean ignoreErrors = false;
		if( oignore.used ) {
			tracer.finest("Ignore option was given.");
			
			ignoreErrors = true;
		}
		tracer.finest(String.format("ignoreErrors set to %s.", ignoreErrors));

		tracer.finest("Loading Baseline.");
		Baseline baseline = Baseline.get( obaseline.getString() ).load();
		tracer.finest(String.format("baseline set to %s.", baseline));
		
		tracer.finest("Loading directory.");
		File dir = odir.used ? new File( odir.getString() ) : null;
		tracer.finest(String.format("dir set to %s.", dir));
		
		tracer.finest("Determining build number.");
		int number = BuildNumber.stampIntoCode( baseline, dir, ignoreErrors );
		tracer.finest(String.format("number set to %s.", number));

		tracer.finest("Checking value of number.");
		
		/* Determine the return value */
		if( number > 0 ) {
			tracer.finest("number is greater than 0");
			System.out.println( number );
			
			tracer.finest("Exiting with exit code 0");
			System.exit( 0 );
		} else {
			tracer.finest("number is less than 0");
			System.err.println( 0 );
			
			tracer.finest("Exiting with exit code 1");
			System.exit( 1 );
		}
		
		tracer.finest("Ending execution of method - run");
	}
}
