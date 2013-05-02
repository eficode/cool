package net.praqma.clearcase;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Find {

    private static Logger logger = Logger.getLogger( Find.class.getName() );

    protected List<String> pathNames = new ArrayList<String>();
    protected File viewRoot;

    public enum Visibility {
        visible,
        nvisible
    }

    public enum Type {
        all,
        avobs
    }

    protected Visibility visible = Visibility.visible;
    protected Type type;

    /* Selections */

    /**
     * Causes directory entries to be processed before the directory itself.
     */
    protected boolean depth = false;

    /**
     * Selects only those objects in the subset of elements owned by user login-name.
     */
    protected String user;

    /**
     * Use extended path names
     */
    protected boolean extendedNames = true;


    /* Actions */

    /**
     * Prints the found selection
     */
    protected boolean print = false;

    public Find() {

    }

    public Find setViewRoot( File viewRoot ) {
        this.viewRoot = viewRoot;

        return this;
    }

    public Find addPathName( String name ) {
        pathNames.add( name );

        return this;
    }

    public Find addPathNames( Collection<String> pathNames ) {
        this.pathNames.addAll( pathNames );

        return this;
    }

    public Find setNotVisible() {
        visible = Visibility.nvisible;

        return this;
    }

    public Find setType( Type type ) {
        this.type = type;

        return this;
    }

    public Find setFindAll() {
        this.type = Type.all;

        return this;
    }

    public Find print() {
        print = true;

        return this;
    }

    public Find setUser( String user ) {
        this.user = user;

        return this;
    }

    public Find useDepth() {
        this.depth = true;

        return this;
    }

    public Find useUnExtendedNames() {
        this.extendedNames = false;

        return this;
    }

    public List<Version> find() throws CleartoolException, UnableToInitializeEntityException {
        logger.fine( "Finding objects in ClearCase" );
        String cmd = getCommandLine();

        List<String> lines;

        try {
            lines = Cleartool.run( cmd, viewRoot ).stdoutList;
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Error while finding", e );
        }

        List<Version> versions = new ArrayList<Version>( lines.size() );

        for( String line : lines ) {
            versions.add( Version.getVersion( line ) );
        }

        return versions;
    }

    public String getCommandLine() {
        StringBuilder sb = new StringBuilder();
        sb.append( "find" );

        if( ( type == null || !type.equals( Type.avobs ) ) ) {
            if( pathNames.size() == 0 ) {
                throw new IllegalStateException( "At least one path name must be specified" );
            }

            for( String pname : pathNames ) {
                sb.append( " " + pname );
            }
        }

        if( type != null ) {
            sb.append( " -" + type );
            sb.append( " -" + visible.toString() );
        }

        /* Selections */

        if( !extendedNames ) {
            sb.append( " -nxname" );
        }

        /* Actions */
        boolean actions = false;
        if( print ) {
            sb.append( " -print" );
            actions = true;
        }

        if( !actions ) {
            throw new IllegalStateException( "No actions defined" );
        }

        return sb.toString();
    }
}
