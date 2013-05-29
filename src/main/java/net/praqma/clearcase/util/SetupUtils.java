package net.praqma.clearcase.util;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.api.RemoveView;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;

public class SetupUtils {
	private static Logger logger = Logger.getLogger( SetupUtils.class.getName() );
	
	public static void tearDown( PVob pvob ) throws CleartoolException, UnableToInitializeEntityException, ViewException {
		/* The pvob needs to be loaded */
		pvob.load();

        for( Stream stream : pvob.getStreams() ) {
            for( UCMView view : stream.getViews() ) {
                logger.info( "Removing " + view + " from " + stream.getNormalizedName() );

                try {
                    view.load();
                    view.end();
                    view.remove();
                } catch( Exception e ) {
                    logger.log( Level.WARNING, "Unable to remove " + view, e );
                }

                try {
                    if( view.exists() ) {
                        logger.info( "The view was not removed, trying ...." );
                        new RemoveView().all().setTag( view.getViewtag() ).execute();
                    }
                } catch( Exception e ) {
                    logger.log( Level.WARNING, "Unable to remove(second attempt) " + view, e );
                }
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
		
		logger.info( "Removing pvob" );
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
