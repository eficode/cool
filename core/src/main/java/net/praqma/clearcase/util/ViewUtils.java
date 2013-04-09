package net.praqma.clearcase.util;

import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.Components;
import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;
import net.praqma.clearcase.ucm.view.UCMView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * Helper class for UCM views
 * 
 * @author wolfgang
 *
 */
public class ViewUtils {

    private static Logger logger = Logger.getLogger( ViewUtils.class.getName() );

	public static SnapshotView createView( Stream stream, String loadModule, File viewroot, String viewtag ) throws ViewException, UnableToInitializeEntityException, CleartoolException, IOException, UnableToLoadEntityException {

		SnapshotView snapview = null;

		logger.fine( "[UCMView] View root: " + viewroot.getAbsolutePath() );
        logger.fine( "[UCMView] View tag : " + viewtag );

		boolean pathExists = false;

		/* Determine if there is a view path if not, create it */
		if( viewroot.exists() ) {
			pathExists = true;
            logger.fine( "[UCMView] Reusing view root" );
		} else {
			if( !viewroot.mkdir() ) {
				//throw new UCMViewException( "Could not create folder for view root:  " + viewroot.toString() );
				throw new ViewException( "Could not create folder for view root", viewroot.getAbsolutePath(), Type.CREATION_FAILED, null );
			}
		}

		/* Determine if the view tag exists, if not create it */
        logger.fine( "[UCMView] Determine if view tag exists" );
		if( UCMView.viewExists( viewtag ) ) {
            logger.fine( "[UCMView] Reusing view tag" );

			try {
				String vt = SnapshotView.viewrootIsValid( viewroot );
                logger.fine( "[UCMView] UUID resulted in " + vt );
				
				/* Not the correct view tag for the given view, delete it and try again */
				if( !vt.equals( viewtag ) && pathExists ) {
                    logger.fine( "[UCMView] View tag is not the same as " + vt );
					
					/* Delete view */
                    logger.fine( "[UCMView] Trying to delete " + viewroot );
					try {
                        FileUtils.deleteDirectory( viewroot );
					} catch( Exception e ) {
						//throw new UCMViewException( "Unable to recursively prepare view root", e );
						throw new ViewException( "Unable to recursively prepare view root", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e );
					}
					
					/* Try to create the view again */
					return createView( stream, loadModule, viewroot, viewtag );
				}
			} catch( ClearCaseException e ) {
				
				/* Try to regenerate the view */
				try {
                    logger.fine( "[UCMView] Regenerating invalid view root" );
					SnapshotView.end( viewtag );
					SnapshotView.regenerateViewDotDat( viewroot, viewtag );
				} catch( ClearCaseException e1 ) {
					//throw new UCMViewException( "Unable to regenerate view", e1 );
					throw new ViewException( "Unable to regenerate view", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e1 );
				}
			} catch( Exception e ) {
                logger.fine( "[UCMView] Unable to make workspace: " + e.getMessage() );
				//throw new UCMViewException( "Unable to make workspace", e );
				throw new ViewException( "Unable to make workspace", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e );
			}

            logger.fine( "[UCMView] Getting snapshotview" );

			snapview = SnapshotView.get( viewroot );
		} else {
			
			/* The view does not exist, create it */
            logger.fine( "[UCMView] Creating new view" );
			snapview = SnapshotView.create( stream, viewroot, viewtag );

            logger.fine( "[UCMView] Created new view in local workspace: " + viewroot.getAbsolutePath() );
			
		}

		/* Update view */
        /*
		if( update ) {
            logger.fine( "[UCMView] Updating view using " + loadModule.toLowerCase() + " modules." );
			LoadRules lr = new LoadRules( snapview, Components.valueOf( loadModule.toUpperCase() ) );
			
			snapview.Update( true, true, true, false, lr );
		}
		*/

		return snapview;
	}
}
