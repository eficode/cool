package net.praqma.cli;

import java.util.List;

import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class RemoveAllViews {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );

	public static void main( String[] args ) throws CleartoolException {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options( "1.0.0" );

		Option oregion = new Option( "region", "r", true, 1, "Name of the region" );

		o.setOption( oregion );
		
		tracer.finest("Setting default options.");

		o.setDefaultOptions();
		
		tracer.finest("Parsing args to Options.");

		o.parse( args );

		app.setTemplate( "[%level]%space %message%newline" );
		Logger.addAppender( app );
		
		tracer.finest("Checking if Options are verbose.");

		if( o.isVerbose() ) {
			tracer.finest("Options are verbose, setting log appender to debug.");
			
			app.setMinimumLevel( LogLevel.DEBUG );
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

		tracer.finest("Creating new Site.");
		
		Site site = new Site( "My site" );
		
		tracer.finest("Creating Region from region option.");
		
		Region region = new Region( oregion.getString(), site );
		
		tracer.finest(String.format("region set to: %s", region));

		tracer.finest("Getting views from region.");
		
		List<UCMView> views = region.getViews();

		for( UCMView view : views ) {
			try {
				tracer.finest(String.format("Removing view: %s", view));
				logger.info( "Removing " + view.getViewtag() );
				view.remove();
			} catch( ClearCaseException e ) {
				tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
				tracer.finest(String.format("Failed to remove view: %s", view));
				
				logger.warning( "Failed to remove " + view.getViewtag() );
				
			}
		}
		tracer.finest("Ending execution of method - main");
	}
}
