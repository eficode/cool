package net.praqma.clearcase.command;

import net.praqma.clearcase.PVob;

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
public class ListPVobs extends Command<List<PVob>> {

    private static Logger logger = Logger.getLogger( ListPVobs.class.getName() );

    public static final Pattern rx = Pattern.compile( "^(\\*{0,1})\\s+(\\S+)\\s+(\\S+)\\s+(.*?)$" );

    public ListPVobs() {
        this.cmd.append( "lsvob" );
    }

    @Override
    public List<PVob> get() {
        List<PVob> pvobs = new LinkedList<PVob>();

        for( String line : result.stdoutList ) {

            Matcher m = rx.matcher( line );
            if( m.find() ) {
                if( m.group( 4 ).contains( "ucmvob" ) ) {
                    //logger.finer( "UCM PVOB; 1: " + m.group( 1 ) + ", 2: " + m.group( 2 ) + ", 3: " + m.group( 3 ) + ", 4: " + m.group( 4 ) );
                    pvobs.add( new PVob( m.group( 2 ) ) );
                }
            }
        }

        return pvobs;
    }
}
