package net.praqma.clearcase.util;

import java.util.Set;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CommandLine;

public class SetupUtils {
	private static Logger logger = Logger.getLogger();
	
	public static void tearDown( PVob pvob ) throws CleartoolException {
		Set<UCMView> views = pvob.getViews();
		
		/* The pvob needs to be loaded */
		pvob.load();
		
		logger.verbose( "Removing views" );
		for( UCMView view : views ) {
			logger.debug( "Removing " + view );
			try {
				view.end();
				view.remove();
			} catch( ViewException e ) {
				ExceptionUtils.log( e, true );
			}
		}
		
		Set<Vob> vobs = pvob.getVobs();
		
		logger.verbose( "Removing vobs" );
		for( Vob vob : vobs ) {
			logger.debug( "Removing " + vob );
			try {
				vob.unmount();
				vob.remove();
			} catch( CleartoolException e ) {
				ExceptionUtils.log( e, true );
			}
		}
		
		logger.verbose( "Removing pvob" );
		pvob.unmount();
		pvob.remove();
		
		/* For Jens' sake */
		logger.debug( "Checking views: " + CommandLine.getInstance().run( "rgy_check -views" ) );
		logger.debug( "Checking vobs: " + CommandLine.getInstance().run( "rgy_check -vobs" ) );
	}
}
