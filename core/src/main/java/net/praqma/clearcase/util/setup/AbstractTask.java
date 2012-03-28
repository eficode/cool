package net.praqma.clearcase.util.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractTask {

	public abstract void parse( Element e, File context ) throws ClearCaseException;
	
	public PVob getPVob( String name ) {
		if( name.length() > 0 ) {
			return new PVob( Cool.filesep + name );
		} else {
			return null;
		}
	}
	
    public Element getFirstElement( Element e, String tag ) throws DOMException {
        NodeList list = e.getChildNodes();

        for( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );

            if( node.getNodeType() == Node.ELEMENT_NODE ) {
                if( node.getNodeName().equals( tag ) ) {
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
