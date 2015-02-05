package net.praqma.clearcase.util.setup;


import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.HyperLink;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class HLinkTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String type = e.getAttribute( "type" );
        PVob pvob = new PVob( getValue( "pvob", e, context ) );
		String from = e.getAttribute( "from" );
        String to = e.getAttribute("to");
        String comment = e.getAttribute("comment");
        
        //Create the hyperlink type and associate that stream one should deliver to stream 2.
        HyperLink.createType(type, pvob, comment);
        HyperLink.createOfType(type, "Created hyperlink", context.streams.get(from), context.streams.get(to));        
	}

}
