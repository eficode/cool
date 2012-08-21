package net.praqma.clearcase.util.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractTask {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public abstract void parse( Element e, Context context ) throws ClearCaseException;
tracer.exiting(AbstractTask.class.getSimpleName(), "parse");
tracer.entering(AbstractTask.class.getSimpleName(), "parse", new Object[]{e, context});
	
	public PVob getPVob( String name ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getPVob", new Object[]{name});
		if( name.length() > 0 ) {
tracer.exiting(AbstractTask.class.getSimpleName(), "getPVob", new PVob( name ));
			return new PVob( name );
		} else {
tracer.exiting(AbstractTask.class.getSimpleName(), "getPVob", null);
			return null;
		}
	}
	
	private static final Pattern rx_variable = Pattern.compile( "(\\$\\{(.*?)\\})" );
	
	/**
	 * Get the tag of an element. Either tag, wintag or linuxtag must be provided or else null is returned.<br/>
	 * wintag and tag are aliases. wintag is preferred. tag is typically used without prepended backward slash.
	 * @param e - Element tag
	 * @param context - The context
	 * @return The tag given the OS context or null
	 */
	public String getTag( Element e, Context context ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getTag", new Object[]{e, context});
		String tag = getValue( "tag", e, context, null ); /* Same as wintag */
		String wintag = getValue( "wintag", e, context, null );
		String lintag = getValue( "linuxtag", e, context, null );
		
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			if( wintag != null ) {
tracer.exiting(AbstractTask.class.getSimpleName(), "getTag", wintag);
				return wintag;
			} else if( tag != null ) {
tracer.exiting(AbstractTask.class.getSimpleName(), "getTag", ( tag.startsWith( "\\" ) ? tag : "\\" + tag ));
				return ( tag.startsWith( "\\" ) ? tag : "\\" + tag );
			} else {
tracer.exiting(AbstractTask.class.getSimpleName(), "getTag", null);
				return null;
			}
		} else {
tracer.exiting(AbstractTask.class.getSimpleName(), "getTag", lintag);
			return lintag;
		}
	}
	
	public String getValue( String name, Element e, Context context ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getValue", new Object[]{name, e, context});
tracer.exiting(AbstractTask.class.getSimpleName(), "getValue", getValue( name, e, context, "" ));
		return getValue( name, e, context, "" );
	}
	
	public String getValue( String name, Element e, Context context, String def ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getValue", new Object[]{name, e, context, def});
		String value = e.getAttribute( name );
		
		Matcher m = rx_variable.matcher( value );

		while( m.find() ) {
			String var = context.variables.get( m.group( 2 ) ).value;			
			value = value.replace( m.group( 1 ), var );
		}
		
tracer.exiting(AbstractTask.class.getSimpleName(), "getValue", value.length() > 0 ? value : def);
		return value.length() > 0 ? value : def;
	}
	
	public boolean getBoolean( String name, Element e, Context context, boolean def ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getBoolean", new Object[]{name, e, context, def});
		String value = e.getAttribute( name );
		
		value = parseField( value, context );
		
		boolean result = def;
		
		if( value.length() == 0 ) {
			result = false;
		} else {
			if( value.matches( "^(?i)off|false$" ) ) {
				result = false;
			} else {
				result = true;
			}
		}
		
tracer.exiting(AbstractTask.class.getSimpleName(), "getBoolean", result);
		return result;
	}
	
	public String parseField( String value, Context context ) {
tracer.entering(AbstractTask.class.getSimpleName(), "parseField", new Object[]{value, context});
		Matcher m = rx_variable.matcher( value );

		while( m.find() ) {
			String var = context.variables.get( m.group( 2 ) ).value;			
			value = value.replace( m.group( 1 ), var );
		}
		
tracer.exiting(AbstractTask.class.getSimpleName(), "parseField", value);
		return value;
	}
	
	public String getComment( Element e, Context context ) {
tracer.entering(AbstractTask.class.getSimpleName(), "getComment", new Object[]{e, context});
		String comment = getValue( "comment", e, context );
tracer.exiting(AbstractTask.class.getSimpleName(), "getComment", comment.length() > 0 ? comment : null);
		return comment.length() > 0 ? comment : null;
	}
	
    public Element getFirstElement( Element e, String tag ) throws DOMException {
tracer.entering(AbstractTask.class.getSimpleName(), "getFirstElement", new Object[]{e, tag});
        NodeList list = e.getChildNodes();

        for( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );

            if( node.getNodeType() == Node.ELEMENT_NODE ) {
                if( node.getNodeName().equals( tag ) ) {
tracer.exiting(AbstractTask.class.getSimpleName(), "getFirstElement", (Element) node);
                    return (Element) node;
                }
            }
        }

        throw new DOMException( DOMError.SEVERITY_WARNING, "Could not GetElement " + tag );
    }

    public List<Element> getElements( Element e ) {
        NodeList list = e.getChildNodes();

        List<Element> result = new ArrayList<Element>();

        for( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );

            if( node.getNodeType() == Node.ELEMENT_NODE ) {
                Element e1 = (Element) node;
                result.add( e1 );
            }
        }

        return result;
    }
}
