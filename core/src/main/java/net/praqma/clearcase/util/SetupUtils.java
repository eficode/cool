package net.praqma.clearcase.util;

import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;

public class SetupUtils {
	private static Logger logger = Logger.getLogger();
	
	public static void tearDown( PVob pvob ) throws CleartoolException {
		List<UCMView> views = pvob.getViews();
		
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
		
		List<Vob> vobs = pvob.getVobs();
		
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
	}
}
