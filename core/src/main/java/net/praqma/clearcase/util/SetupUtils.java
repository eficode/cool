package net.praqma.clearcase.util;

import java.util.Set;
import java.util.logging.Logger;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;

public class SetupUtils {
	private static Logger logger = Logger.getLogger( SetupUtils.class.getName() );
	
	public static void tearDown( PVob pvob ) throws CleartoolException {
		Set<UCMView> views = pvob.getViews();

        logger.fine( "Views: " + views );
		
		/* The pvob needs to be loaded */
		pvob.load();
		
		logger.info( "Removing views" );
		for( UCMView view : views ) {
			logger.info( "Removing " + view );
			try {
				view.end();
				view.remove();
			} catch( ViewException e ) {
				ExceptionUtils.log( e, true );
			}
		}
		
		Set<Vob> vobs = pvob.getVobs();
		
		logger.info( "Removing vobs" );
		for( Vob vob : vobs ) {
			logger.info( "Removing " + vob );
			try {
				vob.unmount();
				vob.remove();
			} catch( CleartoolException e ) {
				ExceptionUtils.log( e, true );
			}
		}
		
		logger.config( "Removing pvob" );
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
	}
}
