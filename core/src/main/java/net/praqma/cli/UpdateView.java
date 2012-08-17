package net.praqma.cli;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.Components;
import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;
import net.praqma.clearcase.ucm.view.SnapshotView.UpdateInfo;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class UpdateView {
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

	public static void run( String[] args ) throws ClearCaseException, IOException {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options();

		Option opath = new Option( "path", "p", false, 1, "ClearCase view to be cleaned" );
		Option oswipe = new Option( "swipe", "s", false, 0, "Swipe view" );
		Option ogen = new Option( "generate", "g", false, 0, "Generate stream" );
		Option ooverwrite = new Option( "overwrite", "o", false, 0, "Overwrite" );
		
		Option oall = new Option( "all", "a", false, 0, "All components" );
		Option omodifiable = new Option( "modifiable", "m", false, 0, "Modifiable components" );

		o.setOption( opath );
		o.setOption( oswipe );
		o.setOption( ogen );
		o.setOption( ooverwrite );
		
		o.setOption( oall );
		o.setOption( omodifiable );
		
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        tracer.finest("Setting default options.");

        o.setDefaultOptions();
        
        tracer.finest("Parsing args to Options.");
        
        o.parse( args );
        
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
		tracer.finest("Checking if swipe option was given.");
		
		boolean swipe = false;
		if( oswipe.isUsed() ) {
			tracer.finest("Swipe option was given.");
			swipe = true;
		}
		tracer.finest(String.format("Swipe set to %s.", swipe));
		tracer.finest("Checking if generate option was given.");
		
		boolean generate = false;
		if( ogen.isUsed() ) {
			tracer.finest("Generate option was given.");
			generate = true;
		}
		tracer.finest(String.format("generate set to %s.", generate));
		tracer.finest("Checking if overwrite option was given.");
		
		boolean overwrite = false;
		if( ooverwrite.isUsed() ) {
			tracer.finest("Overwrite option was given.");
			overwrite = true;
		}
		tracer.finest(String.format("overwrite set to %s.", overwrite));
		tracer.finest("Checking if all and modifiable options were given.");
		
		if( oall.isUsed() && omodifiable.isUsed() ) {
			tracer.finest("Both all and modifiable options were used, exiting.");
			
			logger.error( "Only all or modifiable can be chosen" );
			o.display();
			System.exit( 1 );
		}
		
		if( !oall.isUsed() && !omodifiable.isUsed() ) {
			tracer.finest("Neither all nor modifiable options were used, exiting");
			
			logger.error( "One of all and modifiable must be chosen" );
			o.display();
			System.exit( 1 );
		}

		tracer.finest("Getting SnapshotView from root folder path");
		
		SnapshotView view = SnapshotView.getSnapshotViewFromPath( viewroot );
		
		tracer.finest("Checking if all or modifiable options were given.");
		
		LoadRules loadRules = null;
		if( oall.isUsed() ) {
			tracer.finest("All option was given, setting loadRules.");
			
			loadRules = new LoadRules( view, Components.ALL );
		} else {
			tracer.finest("Modifiable options was given, setting loadRiles");
			
			loadRules = new LoadRules( view, Components.MODIFIABLE );
		}
		
		tracer.finest("Setting UpdateInfo from options");
		
		UpdateInfo info = view.Update( swipe, generate, overwrite, false, loadRules );
		
		tracer.finest(String.format("info set to: %s", info));
		tracer.finest("Checking if swipe is true.");
		
		if( swipe ) {
			tracer.finest("swipe is true.");
			
			logger.info( "Removed " + info.filesDeleted + " file" + ( info.filesDeleted == 1 ? "" : "s" ) );
			logger.info( "Removed " + info.dirsDeleted + " director" + ( info.dirsDeleted == 1 ? "y" : "ies" ) );		
		}
		
		tracer.finest("Ending execution of method - run");
	}

}
