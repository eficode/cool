package net.praqma.clearcase.command;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.Vob;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: cwolfgang
 * Date: 09-11-12
 * Time: 11:02
 */
@SuppressFBWarnings("")
public class ListVobs extends Command<List<Vob>> {

    private static Logger logger = Logger.getLogger( ListVobs.class.getName() );

    public static final Pattern rx = Pattern.compile( "^(\\*{0,1})\\s+(\\S+)\\s+(\\S+)\\s+(.*?)$" );

    public enum VobType {
        CVOB,
        PVOB,
        BOTH;

        public boolean is( boolean isPvob ) {
            if( ( this.equals( CVOB ) && isPvob ) ||
                ( this.equals( PVOB ) && !isPvob ) ) {
                return false;
            }

            return true;
        }

        public static VobType get( boolean ucmvob ) {
            if( ucmvob ) {
                return PVOB;
            } else {
                return CVOB;
            }
        }
    }

    private VobType type = VobType.BOTH;

    public ListVobs() {
        this.cmd.append( "lsvob" );
    }

    public ListVobs setVobType( VobType type ) {
        this.type = type;

        return this;
    }

    @Override
    public List<Vob> get() {
        List<Vob> pvobs = new LinkedList<Vob>();

        for( String line : result.stdoutList ) {

            Matcher m = rx.matcher( line );
            if( m.find() ) {
                if( type.is( m.group( 4 ).contains( "ucmvob" ) ) ) {
                    //logger.finer( "UCM PVOB; 1: " + m.group( 1 ) + ", 2: " + m.group( 2 ) + ", 3: " + m.group( 3 ) + ", 4: " + m.group( 4 ) );
                    pvobs.add( new Vob( m.group( 2 ) ) );
                }
            }
        }

        return pvobs;
    }
}
