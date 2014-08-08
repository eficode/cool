package net.praqma.clearcase.api;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Describe extends Command<List<String>> {

    private static final Logger logger = Logger.getLogger( Describe.class.getName() );

    private String itemSeparator = "\n";

    private boolean acceptEmpty = false;

    public enum Type {
        SHORT( "-short" ),
        LONG( "-long" ),
        FMT( "-fmt" );

        private String text;

        private Type( String text ) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private Type type;

    private boolean objectId = false;

    public static final Property activity = new Property( "activity" ).extended( true );
    public static final Property memberOfClosure = new Property( "member_of_closure" );
    public static final Property dependsOn = new Property( "depends_on" );
    public static final Property dependsOnClosure = new Property( "depends_on_closure" );
    public static final Property initialBaseline = new Property( "initial_bl" ).extended( true );
    public static final Property versions = new Property( "versions" ).commaSeparate();

    public static class Property {

        private String name;
        private String type = "p";

        private boolean commaSeparated = false;
        private boolean extended = false;

        public Property( String name ) {
            this.name = name;
        }

        public Property commaSeparate() {
            commaSeparated = true;

            return this;
        }

        public Property commaSeparate( boolean commaSeparated ) {
            this.commaSeparated = commaSeparated;

            return this;
        }

        public Property extended( boolean extended ) {
            this.extended = extended;

            return this;
        }

        public boolean isCommaSeparated() {
            return commaSeparated;
        }

        public boolean isExtended() {
            return extended;
        }

        public Property setType( String type ) {
            this.type = type;

            return this;
        }

        public String getModifiers() {
            StringBuilder b = new StringBuilder(  );

            if( extended ) {
                b.append( "X" );
            }

            if( commaSeparated ) {
                b.append( "C" );
            }

            return b.toString();
        }

        @Override
        public String toString() {
            return "%[" + this.name + "]" + getModifiers() + type;
        }

        @Override
        public Property clone() {
            Property p = new Property( name );
            p.type = type;
            p.commaSeparated = commaSeparated;

            return p;
        }
    }

    private List<Property> properties = new ArrayList<Property>(  );

    private String objectSelector;

    public Describe( String objectSelector ) {
        this.objectSelector = objectSelector;
    }

    public Describe( UCMEntity entity ) {
        this.objectSelector = entity.getFullyQualifiedName();
    }

    public Describe addModifier( Property property ) {
        properties.add( property );
        type = Type.FMT;

        return this;
    }

    public Describe addModifier( Property property, boolean commaSeparate ) {
        return addModifier( property, commaSeparate, false );
    }

    public Describe addModifier( Property property, boolean commaSeparate, boolean extended ) {
        properties.add( property.clone().commaSeparate( commaSeparate ).extended( extended ) );
        type = Type.FMT;

        return this;
    }

    public Describe getObjectId() {
        this.objectId = true;
        type = Type.FMT;

        return this;
    }

    public Describe setPath( File path ) {
        this.root = path;
        return this;
    }

    /**
     * Allow the execution of the {@link Describe} to return null instead of an exception. See FB case 9988.
     */
    public Describe doAcceptEmpty() {
        this.acceptEmpty = true;
        return this;
    }

    @Override
    public List<String> execute() throws CleartoolException {
        CmdResult result = runCommand();

        return result.stdoutList;
    }
    
    public String executeGetFirstLine(boolean robust) throws CleartoolException {
        int retryCnt = 3;
        long retryInterval = 30*1000;
        String res = null;
        
        if (!robust) {
            CmdResult result = runCommand();

            if( result.stdoutList.size() > 0 ) {
                return result.stdoutList.get( 0 );
            } else {
                if( acceptEmpty ) {
                    return null;
                } else {
                    throw new CleartoolException( "No results found" );
                }
            }
        } else {            
            for (int i = 0; i<retryCnt; i++) {
                try {
                    return executeGetFirstLine(false);
                } catch (CleartoolException ex) {
                    if (ex.getMessage().contains("No results found")) {
                        logger.log(Level.WARNING, String.format("Failed to describe revision. Attempt number %s. Retrying in 30 seconds", i+1 ));
                    } else {
                        throw ex;
                    }
                }                
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ex) {
                   throw new CleartoolException("Thread sleep failed.");
                }
            }
            
            if(acceptEmpty) {
                return null;
            } else {
                throw new CleartoolException( "No results found" );
            } 
        }
    }
 
    public String executeGetFirstLine() throws CleartoolException {
        return executeGetFirstLine(false);
    }

    /**
     * This method will return a grouped result by property for easier access.
     */
    public Map<String, String[]> describe() throws CleartoolException {
        CmdResult result = runCommand();
        return getResults( getElements( result.stdoutBuffer.toString() ) );
    }

    @Override
    public String getCommand() {
        return "describe";
    }

    @Override
    public String getCommandLine() {
        StringBuilder b = new StringBuilder(  );
        b.append( "describe " ).append( type ).append( " " );

        switch( type ) {
            case FMT:
                if( properties.size() > 0 ) {
                    int i = 0;
                    for( ; i < properties.size() - 1 ; ++i ) {
                        b.append( properties.get( i ) ).append( itemSeparator );
                    }

                    b.append( properties.get( i ) );
                }

                if( objectId ) {
                    if( properties.size() > 0 ) {
                        b.append( itemSeparator );
                    }
                    b.append( "%On" );
                }
                break;
        }

        if( hasWhiteSpaces( objectSelector ) || hasSpecialCharacters( objectSelector ) ) {
            b.append( " \"" ).append( objectSelector ).append( "\"" );
        } else {
            b.append( " " ).append( objectSelector );
        }

        return b.toString();
    }


    public Map<String, String[]> getResults( List<String> elements ) {
        logger.finest( "Get results for " + elements );

        if( elements.size() != properties.size() ) {
            throw new IllegalStateException( "The number of elements and the number of describers does not match" );
        }

        Map<String, String[]> results = new HashMap<String, String[]>(  );

        for( int i = 0 ; i < properties.size() ; ++i ) {
            String[] r;
            if( properties.get( i ).isCommaSeparated() ) {
                r = elements.get( i ).trim().split( "\\s*,\\s*" );
            } else {
                r = elements.get( i ).trim().split( "\\s+" );
            }

            results.put( properties.get( i ).name, r );
        }

        return results;
    }

    public List<String> getElements( String string ) {
        logger.finest( "Get elements for " + string );

        String[] lines = string.split( itemSeparator );
        List<String> elements = new ArrayList<String>(  );
        for( String line : lines ) {
            elements.add( line );
        }

        return elements;
    }
}
