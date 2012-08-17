package net.praqma.cli;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BuildNumber;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class GetNextBuildNumber {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();

	public static void main( String[] args ) throws ClearCaseException {
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

	public static void run( String[] args ) throws UCMEntityNotFoundException, UnableToLoadEntityException, UnableToCreateEntityException, UnableToGetEntityException, UnableToInitializeEntityException {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options( net.praqma.cool.Version.version );

		Option oproject = new Option( "project", "p", false, 1, "Retrieve the next build number given a project" );
		Option ostream = new Option( "stream", "s", false, 1, "Retrieve the next build number given a stream" );
		o.setOption( oproject );
		o.setOption( ostream );
		
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );

        tracer.finest("Setting default options.");
        
		o.setDefaultOptions();

		o.setSyntax( "GetNextBuildNumber [-p {project} | -s {stream}]" );
		o.setHeader( "Retrieve the next build number given a UCM project or a UCM stream." );

		tracer.finest("Parsing args to Options.");
		
		o.parse( args );

		tracer.finest("Attepmting to load Options...");
		
		try {
			o.checkOptions();
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.fatal( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Checking that a project and a stream option was given.");

		if( !oproject.used && !ostream.used ) {
			tracer.severe("Neither a stream nor a project was given, exiting...");
			
			logger.fatal( "Neither a stream nor a project was given." );
			System.exit( 1 );
		}
		
		tracer.finest("Checking if a project was given");

		Project project = null;

		if( oproject.used ) {
			tracer.finest("Project option was given.");
			tracer.finest("Loading Project from project option.");
			
			project = Project.get( oproject.getString() ).load();
		} else {
			tracer.finest("Project option was not given.");
			tracer.finest("Loading Project from stream option.");
			
			Stream stream = Stream.get( ostream.getString() ).load();
			project = stream.getProject();
		}
		tracer.finest(String.format("project set to: %s", project));
		tracer.finest("Attempting to determine the build number of the project...");
		String number = "";
		try {
			number = BuildNumber.getBuildNumber( project );
			logger.info( number );
			System.exit( 0 );
		} catch( ClearCaseException e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.error( "Could not retrieve the build number" );
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest(String.format("Successfully determined the build number to be: %s", number));
		tracer.finest("Ending execution of method - run");
	}
}
