package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * @author cwolfgang
 */
public class UpdateView {

    private static final Logger logger = Logger.getLogger( UpdateView.class.getName() );

    private boolean swipe = false;
    private boolean generate = false;
    private boolean overwrite = false;
    private boolean excludeRoot = false;

    private boolean removeDanglingComponentFolders = false;

    private SnapshotView.LoadRules2 loadRules;

    private SnapshotView view;

    /* Results */
    private int totalFilesToBeDeleted = 0;
    private boolean success = false;
    private int filesDeleted = 0;
    private int dirsDeleted = 0;

    public UpdateView( SnapshotView view ) {
        this.view = view;
    }

    public UpdateView swipe() {
        this.swipe = true;

        return this;
    }

    public UpdateView generate() {
        this.generate = true;

        return this;
    }

    public UpdateView overwrite() {
        this.overwrite = true;

        return this;
    }

    public UpdateView excludeRoot() {
        this.excludeRoot = true;

        return this;
    }

    public UpdateView setLoadRules( SnapshotView.LoadRules2 loadRules ) {
        this.loadRules = loadRules;
        return this;
    }

    public int getTotalFilesToBeDeleted() {
        return totalFilesToBeDeleted;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getFilesDeleted() {
        return filesDeleted;
    }

    public int getDirsDeleted() {
        return dirsDeleted;
    }

    public UpdateView update() throws ClearCaseException, IOException {

        if( generate ) {
            logger.fine( "Generate config spec for stream" );
            this.view.getStream().generate();
        }

        if( swipe ) {
            logger.fine( "Swipe view" );
            Map<String, Integer> sinfo = view.swipe( excludeRoot );
            success = sinfo.get( "success" ) == 1 ? true : false;
            totalFilesToBeDeleted = sinfo.containsKey( "total" ) ? sinfo.get( "total" ) : 0;
            dirsDeleted = sinfo.containsKey( "dirs_deleted" ) ? sinfo.get( "dirs_deleted" ) : 0;
            filesDeleted = sinfo.containsKey( "files_deleted") ? sinfo.get( "files_deleted" ) : 0;

            logger.fine( "SWIPED" );
        }

        // Cache current directory and chdir into the viewroot
        String result = updateView( view, overwrite, loadRules );
        logger.fine( result );

        if( removeDanglingComponentFolders ) {
            removeComponentFolders();
        }

        return this;
    }

    private static String updateView( SnapshotView view, boolean overwrite, SnapshotView.LoadRules2 loadrules ) throws CleartoolException, ViewException {
        String result = "";

        logger.fine( view.getViewRoot().getAbsolutePath() );

        String cmd = "setcs -stream";
        try {
            Cleartool.run( cmd, view.getViewRoot(), false );
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Unable to set cs stream: " + view.getViewRoot() , e );
        }

        logger.fine( "Updating view with " + loadrules );

        cmd = "update -force " + ( overwrite ? " -overwrite " : "" );
        if( loadrules != null ) {
            cmd += loadrules.getLoadRules();
        }
        try {
            result = Cleartool.run( cmd, view.getViewRoot(), true ).stdoutBuffer.toString();
        } catch( AbnormalProcessTerminationException e ) {
            Matcher m = SnapshotView.rx_view_rebasing.matcher( e.getMessage() );
            if( m.find() ) {
                logger.log( Level.WARNING, "The view is currently rebasing the stream" + m.group( 1 ), e);
                throw new ViewException( "The view is currently rebasing the stream " + m.group( 1 ), view.getViewRoot().getAbsolutePath(), ViewException.Type.REBASING, e );
            } else {
                logger.log( Level.WARNING, "Unable to update view", e );
                throw new ViewException( "Unable to update view", view.getViewRoot().getAbsolutePath(), ViewException.Type.UNKNOWN, e );
            }
        }


        Matcher match = SnapshotView.pattern_cache.matcher( result );
        if( match.find() ) {
            return match.group( 1 );
        }

        return "";
    }

    public UpdateView removeDanglingComponentFolders() {
        this.removeDanglingComponentFolders = true;

        return this;
    }

    private void removeComponentFolders() throws ClearCaseException, IOException {
        List<Baseline> bls = view.getStream().getFoundationBaselines();
        logger.finest( "Baselines: " + bls );
        for( Baseline bl : bls ) {
            String dir = bl.getComponent().getRootDir();
            logger.finer( "Root directory: " + dir );
        }
    }

    public void b( File path, int i ) {
        if( i == 0 ) {

        } else {

        }
    }
}
