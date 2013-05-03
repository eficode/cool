package net.praqma.clearcase.command;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Type;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cwolfgang
 */
public class ListType {

    private static Logger logger = Logger.getLogger( ListType.class.getName() );

    private boolean local = false;

    public enum Kind {
        attype( "attype", null ),
        brtype( "brtype", Branch.class ),
        eltype( "eltype", null ),
        hltype( "hltype", null ),
        lbtype( "lbtype", null ),
        trtype( "trtype", null );

        String name;
        Class<? extends Type> clazz;

        Kind( String name, Class<? extends Type> clazz ) {
            this.name = name;
            this.clazz = clazz;
        }
    }

    private Kind kind;

    private File viewRoot;

    public ListType setViewRoot( File path ) {
        this.viewRoot = path;

        return this;
    }

    public ListType setType( Kind kind ) {
        this.kind = kind;

        return this;
    }

    public ListType setBranchType() {
        this.kind = Kind.brtype;

        return this;
    }

    public ListType setLocal() {
        this.local = true;

        return this;
    }

    public static Pattern rx = Pattern.compile( "^--.*\"(.*?)\"$" );

    public <T extends Type> List<T> list() throws CleartoolException {
        logger.fine( "Listing " + kind );

        String cmd = getCommandLine();

        List<String> lines;

        try {
            lines = Cleartool.run( cmd, viewRoot ).stdoutList;
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Error while listing " + kind, e );
        }

        return getTypes( lines );
    }

    public <T extends Type> List<T> getTypes( List<String> lines ) {
        List<T> types = new ArrayList<T>( lines.size() );

        for( String line : lines ) {
            Matcher m = rx.matcher( line );
            if( m.find() ) {
                try {
                    Constructor c = kind.clazz.getConstructor( String.class );
                    T instance = (T) c.newInstance( m.group( 1 ) );
                    types.add( instance );
                } catch( Exception e ) {
                    logger.log( Level.SEVERE, "Unable to add element", e );
                }
            }
        }

        return types;
    }

    public String getCommandLine() {
        StringBuilder sb = new StringBuilder();
        sb.append( "lstype" );

        if( kind == null ) {
            throw new IllegalStateException( "No kind defined" );
        }

        sb.append( " -kind " ).append( kind.name );

        if( local ) {
            sb.append( " -local" );
        }

        return sb.toString();
    }
}
