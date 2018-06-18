package net.praqma.clearcase.api;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.CmdResult;

import java.util.List;

/**
 * @author cwolfgang
 */
@SuppressFBWarnings("")
public class RemoveView extends Command<Boolean> {

    /**
     * Specifies that the view-related records are to be removed from all VOBs in which such records can be found.
     */
    private boolean all = false;

    /**
     * dynamic-view-tag<br />
     * Specifies the dynamic view to be removed.
     * dynamic-view-tag specifies the view tag of a dynamic view.
     * rmview removes the view storage directory and all relevant entries from the network's view registry.<br /><br />
     *
     * dynamic-view-storage-dir-pname<br />
     * Specifies the storage location directory where the dynamic view resides. Be sure that the current working directory is not within this view storage area.<br /><br />
     *
     * snapshot-view-pname<br />
     * Specifies the path to your snapshot view.
     * This is the directory in which you load your files and do your work.
     * rmview removes the view storage directory and all relevant entries from the network's view registry.
     * Be sure that the current working directory is not anywhere within this view storage area.<br /><br />
     *
     * snapshot-view-storage-dir-pname<br />
     *
     * Note: This option is intended for deleting view storage associated with a snapshot view that was deleted using an operating system command.
     * Only rmview effectively deletes a view; in normal circumstances, you should specify snapshot-view-pname instead of this argument to delete a snapshot view.<br /><br />
     *
     * Specifies the directory within a storage location where the snapshot view resides.
     * rmview removes the view storage directory and all relevant entries from the network's view registry.
     * Be sure that the current working directory is not within this view storage area.<br />
     */
    private String tag;

    /**
     * Specifies the view whose records are to be removed from one or more VOBs.
     */
    private String uuid;

    public RemoveView setTag( String tag ) {
        this.tag = tag;

        return this;
    }

    public RemoveView setUuid( String uuid ) {
        this.uuid = uuid;

        return this;
    }

    public RemoveView all() {
        this.all = true;

        return this;
    }

    @Override
    public Boolean execute() throws CleartoolException {
        runCommand();
        return true;
    }

    @Override
    public String getCommandLine() {
        StringBuilder b = new StringBuilder(  );

        b.append( "rmview -force" );

        if( all ) {
            b.append( " -all" );
        }

        if( tag != null && !tag.isEmpty() ) {
            b.append( " -tag " ).append( tag );
        }

        return b.toString();
    }

    @Override
    public String getCommand() {
        return "rmview";
    }
}
