package net.praqma.cli;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class CleanView {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
	public static void main( String[] args ) throws IOException, ClearCaseException {
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

	public static void run( String[] args ) throws ClearCaseException, IOException {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options();

		Option opath = new Option( "path", "p", false, 1, "ClearCase view to be cleaned" );
		Option oroot = new Option( "root", "r", false, 0, "Clean root directory" );

		o.setOption( opath );
		o.setOption( oroot );

        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        tracer.finest("Setting default options.");
        
        o.setDefaultOptions();
        
        tracer.finest("Parsing args to Options.");
        
        o.parse( args );
        
        tracer.finest("Checking if Options are verbose.");
        
        if( o.isVerbose() ) {
        	tracer.finest("Options are verbose, setting log appender to verbose.");
        	
        	app.setMinimumLevel( LogLevel.VERBOSE );
        } else {
        	tracer.finest("Options are not verbose, setting log appender to info.");
        	
        	app.setMinimumLevel( LogLevel.INFO );
        }

        tracer.finest("Attepmting to load Options...");
        
		try {
			o.checkOptions();
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.error( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Checking if path option was given.");
		
		File viewroot = null;
		if( opath.isUsed() ) {
			tracer.finest("Path option was given.");
			
			viewroot = new File( opath.getString() );
		} else {
			tracer.finest("Path option was not given, using home directory.");
			
			viewroot = new File( System.getProperty( "user.dir" ) );
		}
		tracer.finest(String.format("Root folder path set to %s", viewroot.getAbsolutePath()));
		tracer.finest("Checking if root option was given.");
		
		boolean exclude = true;
		if( oroot.isUsed() ) {
			tracer.finest("root option was given, setting exlcude to false.");
			exclude = false;
		}

		tracer.finest("Getting SnapshotView from root folder path");
		
		SnapshotView view = SnapshotView.getSnapshotViewFromPath( viewroot );
		
		tracer.finest("Swiping view.");
		Map<String, Integer> info = view.swipe( exclude );
		
		tracer.finest("Map<String, Integer> info set to:");
		for (Entry<String, Integer> entry : info.entrySet()) {
    		tracer.finest(String.format("    String: %s, Integer: %s", entry.getKey(), entry.getValue()));
    	}
		
		logger.info( "Removed " + info.get( "files_deleted" ) + " file" + ( info.get( "files_deleted" ) == 1 ? "" : "s" ) );
		logger.info( "Removed " + info.get( "dirs_deleted" ) + " director" + ( info.get( "dirs_deleted" ) == 1 ? "y" : "ies" ) );
		
		tracer.finest("Ending execution of method - run");
	}

}
