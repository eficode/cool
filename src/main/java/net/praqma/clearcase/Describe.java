package net.praqma.clearcase;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cwolfgang
 * @deprecated
 */
public class Describe {

    private static Logger logger = Logger.getLogger( Describe.class.getName() );

    private UCMEntity enitity;

    private class Describer {
        private String describer;
        private String modifier;
        private boolean multiple = false;

        private Describer( String describer, String modifier ) {
            this.describer = describer;
            this.modifier = modifier;
        }

        private Describer( String describer, String modifier, boolean multiple ) {
            this.describer = describer;
            this.modifier = modifier;
            this.multiple = multiple;
        }
    }

    private List<Describer> describers = new ArrayList<Describer>();

    public Describe( UCMEntity enitity ) {
        this.enitity = enitity;
    }

    public Describe setEntity( UCMEntity enitity ) {
        this.enitity = enitity;

        return this;
    }

    public Describe dependentsOn() {
        describers.add( new Describer( "depends_on", "Cp", true ) );

        return this;
    }

    public static final Pattern rx_find_component = Pattern.compile( "\\{(.*?)\\}", Pattern.MULTILINE | Pattern.DOTALL );

    public Map<String, String[]> describe() throws CleartoolException {
        logger.fine( "Describing " + enitity.getNormalizedName() );
        String result = "";

        String cmd = getCommandLine();
        try {
            result = Cleartool.run( cmd ).stdoutBuffer.toString();
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Error while describing", e );
        }

        return getResults( getElements( result ) );
    }

    public Map<String, String[]> getResults( List<String> elements ) {
        if( elements.size() != describers.size() ) {
            throw new IllegalStateException( "The number of elements and the number of describers does not match" );
        }

        Map<String, String[]> results = new HashMap<String, String[]>(  );

        for( int i = 0 ; i < describers.size() ; ++i ) {
            String[] r;
            if( describers.get( i ).multiple ) {
                r = elements.get( i ).trim().split( "\\s*,\\s*" );
            } else {
                r = new String[] { elements.get( i ).trim() };
            }

            results.put( describers.get( i ).describer, r );
        }

        return results;
    }

    public List<String> getElements( String string ) {
        Matcher m = rx_find_component.matcher( string );
        List<String> elements = new ArrayList<String>(  );
        while( m.find() ) {
            elements.add( m.group( 1 ) );
        }

        return elements;
    }

     public String getCommandLine() {

        StringBuilder sb = new StringBuilder(  );
        sb.append( "describe -fmt" );

        if( describers.isEmpty() ) {
            throw new IllegalStateException( "No describers defined" );
        }

        for( Describer d : describers ) {
            sb.append( " {%[" );
            sb.append( d.describer );
            sb.append( "]" );
            sb.append( d.modifier );
            sb.append( "}" );
        }

         sb.append( " " + enitity.toString() );

        return sb.toString();
    }
}
