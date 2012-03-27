package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.exceptions.ClearCaseException;

import org.w3c.dom.Element;

public class ViewTask extends AbstractTask {

	@Override
	public void parse( Element e ) throws ClearCaseException {
		String tag = e.getAttribute( "tag" );
		String stgloc = e.getAttribute( "stgloc" );
		boolean snapshot = e.getAttribute( "snapshot" ).length() > 0;
		
		if( snapshot) {
			
		} else {
			
		}
		
	}

}
