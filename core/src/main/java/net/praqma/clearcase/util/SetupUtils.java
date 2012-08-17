package net.praqma.clearcase.util;

import java.util.Set;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CommandLine;

public class SetupUtils {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	
	public static void tearDown( PVob pvob ) throws CleartoolException {
		tracer.entering(SetupUtils.class.getSimpleName(), "tearDown", pvob);
		tracer.finest("Getting Views from PVob.");
		
		Set<UCMView> views = pvob.getViews();
		
		tracer.finest("Loading PVob.");
		/* The pvob needs to be loaded */
		pvob.load();
		
		tracer.finest("Removing Views.");
		
		logger.verbose( "Removing views" );
		for( UCMView view : views ) {
			tracer.finest(String.format("Attempting to remove View: %s", view));
			
			logger.debug( "Removing " + view );
			try {
				view.end();
				view.remove();
				tracer.finest(String.format("Successfully removed View: %s", view));
			} catch( ViewException e ) {
				tracer.finest(String.format("Could not remove View: %s", view));
				tracer.severe(String.format("Caught exception: %s", e));
				ExceptionUtils.log( e, true );
			}
		}
		tracer.finest("Getting Vobs from PVob");
		
		Set<Vob> vobs = pvob.getVobs();
		
		tracer.finest("Removing Vobs");
		
		logger.verbose( "Removing vobs" );
		for( Vob vob : vobs ) {
			logger.debug( "Removing " + vob );
			
			tracer.finest(String.format("Attempting to remove Vob: %s", vob));
			
			try {
				vob.unmount();
				vob.remove();
				tracer.finest(String.format("Successfully removed Vob: %s", vob));
			} catch( CleartoolException e ) {
				tracer.finest(String.format("Could not remove Vob: %s", vob));
				tracer.severe(String.format("Caught exception: %s", e));
				
				ExceptionUtils.log( e, true );
			}
		}
		tracer.finest("Removing PVob.");
		
		logger.verbose( "Removing pvob" );
		pvob.unmount();
		pvob.remove();
		
		/* For Jens' sake */
		/*
		try {
			logger.debug( "Checking views: " + CommandLine.getInstance().run( "rgy_check -views" ).stdoutBuffer );
		} catch( Exception e ) {
			// Because rgy_check returns 1 if anything stranded
			e.printStackTrace();
		}
		*/
		
		/*
		try {
			logger.debug( "Checking vobs: " + CommandLine.getInstance().run( "rgy_check -vobs" ).stdoutBuffer );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		*/
		
		tracer.exiting(SetupUtils.class.getSimpleName(), "TearDown");
	}
}
