package net.praqma.clearcase.ucm.view;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Stream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
@SuppressFBWarnings("")
public class GetView {

    private static final Logger logger = Logger.getLogger( GetView.class.getName() );
    private static final String LOGGER_PREFIX = "[" + GetView.class.getSimpleName() + "] ";

    private SnapshotView snapview;

    private File viewRoot;
    private String viewTag;

    private Stream stream;

    private boolean createIfAbsent = false;
    private boolean validateViewRoot = false;

    public GetView( File viewRoot, String viewTag ) {
        this.viewRoot = viewRoot;
        this.viewTag = viewTag;
    }

    public GetView createIfAbsent() {
        createIfAbsent = true;

        return this;
    }

    public GetView validateView() {
        validateViewRoot = true;

        return this;
    }

    public GetView setStream( Stream stream ) {
        this.stream = stream;

        return this;
    }

    public SnapshotView get() throws IOException, ClearCaseException {

        if( snapview != null ) {
            return snapview;
        }

        if( UCMView.viewExists( viewTag ) ) {
            logger.fine( LOGGER_PREFIX + "The view \"" + viewTag + "\" exists" );

            if( viewRoot.exists() ) {

                if( validateViewRoot ) {
                    logger.fine( LOGGER_PREFIX + "Validating view root " + viewRoot );

                    try {
                        validateViewRoot2();
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
            } else {
                logger.fine( "The view tag \"" + viewTag + "\" exists, but the view root " + viewRoot.getAbsolutePath() + " does not" );
                SnapshotView.end( viewTag );
                SnapshotView.regenerateViewDotDat( viewRoot, viewTag );
            }

            snapview = SnapshotView.get( viewRoot );
        } else {
            logger.fine( LOGGER_PREFIX + "The view \"" + viewTag + "\" does not exist" );
            if( createIfAbsent ) {
                if( stream == null ) {
                    throw new IllegalStateException( "No Stream given" );
                }

                create();
            }
        }

        return snapview;
    }

    private void create() throws ClearCaseException, IOException {
        if( !viewRoot.exists() && !viewRoot.mkdirs() ) {
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
            throw new IllegalStateException( "View tag does not match path(" + viewTag + " == " + vt + ")" );
        }
    }

    public void validateViewRoot2() throws ClearCaseException, IOException {

        try {
            validateViewRoot();
        } catch( IllegalStateException e ) {
            /* Not the correct view tag for the given view, delete it and try again */
            logger.fine( LOGGER_PREFIX + e.getMessage() );

            /* Delete view */
            logger.fine( LOGGER_PREFIX + "Trying to delete " + viewRoot );
            try {
                FileUtils.deleteDirectory( viewRoot );
            } catch( Exception e1 ) {
                throw new ViewException( "Unable to recursively prepare view root", viewRoot.getAbsolutePath(), ViewException.Type.CREATION_FAILED, e1 );
            }
        }
    }
}
