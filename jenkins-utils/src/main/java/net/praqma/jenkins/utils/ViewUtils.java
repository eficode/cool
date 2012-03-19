package net.praqma.jenkins.utils;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.Components;

/**
 * Helper class for UCM views
 * 
 * @author wolfgang
 *
 */
public class ViewUtils {
	
	public static SnapshotView createView( PrintStream logger, Stream stream, String loadModule, File viewroot, String viewtag ) throws IOException, ViewException, CleartoolException, UCMEntityNotFoundException, UnableToCreateEntityException, UnableToGetEntityException, UnableToLoadEntityException {
		return createView( logger, stream, loadModule, viewroot, viewtag, true );
	}
	
	public static SnapshotView createView( PrintStream logger, Stream stream, String loadModule, File viewroot, String viewtag, boolean update ) throws IOException, ViewException, CleartoolException, UCMEntityNotFoundException, UnableToCreateEntityException, UnableToGetEntityException, UnableToLoadEntityException {

		SnapshotView snapview = null;

		logger.println( "[UCMView] View root: " + viewroot.getAbsolutePath() );
		logger.println( "[UCMView] View tag : " + viewtag );

		boolean pathExists = false;

		/* Determine if there is a view path if not, create it */
		if( viewroot.exists() ) {
			pathExists = true;
			logger.println( "[UCMView] Reusing view root" );
		} else {
			if( !viewroot.mkdir() ) {
				//throw new UCMViewException( "Could not create folder for view root:  " + viewroot.toString() );
				throw new ViewException( "Could not create folder for view root", viewroot.getAbsolutePath(), Type.CREATION_FAILED, null );
			}
		}

		/* Determine if the view tag exists, if not create it */
		logger.println( "[UCMView] Determine if view tag exists" );
		if( UCMView.viewExists( viewtag ) ) {
			logger.println( "[UCMView] Reusing view tag" );
			try {
				String vt = SnapshotView.viewrootIsValid( viewroot );
				logger.println( "[UCMView] UUID resulted in " + vt );
				
				/* Not the correct view tag for the given view, delete it and try again */
				if( !vt.equals( viewtag ) && pathExists ) {
					logger.println( "[UCMView] View tag is not the same as " + vt );
					
					/* Delete view */
					FilePath path = new FilePath( viewroot );
					logger.println( "[UCMView] Trying to delete " + path );
					try {
						path.deleteRecursive();
					} catch( Exception e ) {
						//throw new UCMViewException( "Unable to recursively prepare view root", e );
						throw new ViewException( "Unable to recursively prepare view root", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e );
					}
					
					/* Try to create the view again */
					return createView( logger, stream, loadModule, viewroot, viewtag );
				}
			} catch( ClearCaseException e ) {
				
				/* Try to regenerate the view */
				try {
					logger.println( "[UCMView] Regenerating invalid view root" );
					SnapshotView.end( viewtag );
					SnapshotView.regenerateViewDotDat( viewroot, viewtag );
				} catch( ClearCaseException e1 ) {
					//throw new UCMViewException( "Unable to regenerate view", e1 );
					throw new ViewException( "Unable to regenerate view", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e1 );
				}
			} catch( Exception e ) {
				logger.println( "[UCMView] Unable to make workspace: " + e.getMessage() );
				//throw new UCMViewException( "Unable to make workspace", e );
				throw new ViewException( "Unable to make workspace", viewroot.getAbsolutePath(), Type.CREATION_FAILED, e );
			}

			logger.println( "[UCMView] Getting snapshotview" );

			snapview = UCMView.getSnapshotView( viewroot );
		} else {
			
			/* The view does not exist, create it */
			logger.println( "[UCMView] Creating new view" );
			snapview = SnapshotView.create( stream, viewroot, viewtag );

			logger.println( "[UCMView] Created new view in local workspace: " + viewroot.getAbsolutePath() );
			
		}

		/* Update view */
		if( update ) {
			logger.println( "[UCMView] Updating view using " + loadModule.toLowerCase() + " modules." );
			LoadRules lr = new LoadRules( snapview, Components.valueOf( loadModule.toUpperCase() ) );
			
			snapview.Update( true, true, true, false, lr );
		}

		return snapview;
	}
}
