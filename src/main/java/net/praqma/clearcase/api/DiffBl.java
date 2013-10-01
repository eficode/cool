package net.praqma.clearcase.api;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.List;

/**
 * @author cwolfgang
 */
public class DiffBl extends Command<List<String>> {

    private boolean versions = false;

    private boolean activities = false;

    private boolean nmerge = false;

    private Diffable d1;

    /**  If null, the predecessor switch is enabled */
    private Diffable d2;

    public DiffBl( Diffable d1, Diffable d2 ) {
        this.d1 = d1;
        this.d2 = d2;
    }

    public DiffBl setViewRoot( File path ) {
        this.root = path;

        return this;
    }

    public File getViewRoot() {
        return root;
    }

    public DiffBl setVersions( boolean versions ) {
        this.versions = versions;

        return this;
    }

    public DiffBl setActivities( boolean activities ) {
        this.activities = activities;

        return this;
    }

    public DiffBl setNmerge( boolean nmerge ) {
        this.nmerge = nmerge;

        return this;
    }

    @Override
    public List<String> execute() throws CleartoolException {
        CmdResult result = runCommand();

        return result.stdoutList;
    }

    @Override
    public String getCommandLine() {
        StringBuilder sb = new StringBuilder();

        sb.append( "diffbl " );

        if( versions ) {
            sb.append( "-versions " );
        }

        if( activities ) {
            sb.append( "-activities " );
        }

        if( nmerge ) {
            sb.append( "-nmerge " );
        }

        if( d2 != null ) {
            sb.append( d2.getFullyQualifiedName() ).append( " " );
        } else {
            sb.append( "-predecessor " );
        }

        sb.append( d1.getFullyQualifiedName() );

        return sb.toString();
    }

    @Override
    public String getCommand() {
        return "diffbl";
    }
}
