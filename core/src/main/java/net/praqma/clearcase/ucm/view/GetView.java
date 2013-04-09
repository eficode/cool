package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Stream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class GetView {

    private static Logger logger = Logger.getLogger( GetView.class.getName() );
    private static final String LOGGER_PREFIX = "[" + GetView.class.getSimpleName() + "] ";

    private SnapshotView snapview;

    private File viewRoot;
    private String viewTag;

    private Stream stream;

    private boolean createIfAbsent = false;
    private boolean validateViewRoot = true;

    public GetView( File viewRoot, String viewTag ) {
        this.viewRoot = viewRoot;
        this.viewTag = viewTag;
    }

    public GetView createIfAbsent() {
        createIfAbsent = true;

        return this;
    }

    public GetView dontValidateViewRoot() {
        validateViewRoot = false;

        return this;
    }

    public GetView setStream( Stream stream ) {
        this.stream = stream;

        return this;
    }

    public GetView get() throws IOException, ClearCaseException {

        if( UCMView.viewExists( viewTag ) ) {
            logger.fine( LOGGER_PREFIX + "The view \"" + viewTag + "\" exists" );

            if( !viewRoot.exists() ) {
                throw new IllegalStateException( "The view root " + viewRoot + " does not exist, but the view tag " + viewTag + " does." );
            }

            if( validateViewRoot ) {
                logger.fine( LOGGER_PREFIX + "Validating view root " + viewRoot );

                try {
                    validateViewRoot();
                } catch( ClearCaseException e ) {
                    /* Try to regenerate the view */
                    try {
                        logger.fine( "[UCMView] Regenerating invalid view root" );
                        SnapshotView.end( viewTag );
                        SnapshotView.regenerateViewDotDat( viewRoot, viewTag );
                    } catch( ClearCaseException e1 ) {
                        throw new ViewException( "Unable to regenerate view", viewRoot.getAbsolutePath(), ViewException.Type.CREATION_FAILED, e1 );
                    }
                }
            }

            snapview = SnapshotView.get( viewRoot );
        } else {
            logger.fine( LOGGER_PREFIX + "The view \"" + viewTag + "\" does not exist" );
            create();
        }

        return this;
    }

    private void create() throws ClearCaseException, IOException {
        if( !viewRoot.mkdirs() ) {
            throw new IOException( "Unable to create view root path " + viewRoot );
        }

        logger.fine( LOGGER_PREFIX + "Creating new view in " + viewRoot.getAbsolutePath() );
        snapview = SnapshotView.create( stream, viewRoot, viewTag );
    }

    public void validateViewRoot() throws ClearCaseException, IOException {
        String vt = SnapshotView.viewrootIsValid( viewRoot );
        logger.fine( LOGGER_PREFIX + "UUID resulted in " + vt );

        /* Not the correct view tag for the given view, delete it and try again */
        if( !vt.equals( viewTag ) ) {
            logger.fine( LOGGER_PREFIX + "View tag is not the same as " + vt );

            /* Delete view */
            logger.fine( LOGGER_PREFIX + "Trying to delete " + viewRoot );
            try {
                FileUtils.deleteDirectory( viewRoot );
            } catch( Exception e ) {
                //throw new UCMViewException( "Unable to recursively prepare view root", e );
                throw new ViewException( "Unable to recursively prepare view root", viewRoot.getAbsolutePath(), ViewException.Type.CREATION_FAILED, e );
            }
        }
    }
}
