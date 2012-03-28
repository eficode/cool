package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;

import org.w3c.dom.Element;

public class PVobTask extends AbstractTask {

	@Override
	public void parse( Element e, File context ) throws ClearCaseException {
		String ucm = e.getAttribute( "ucmproject" );
		String name = Cool.filesep + e.getAttribute( "name" );
		String location = e.getAttribute( "stgloc" );
		String mounted = e.getAttribute( "mounted" );
		
		Vob vob = Vob.create( name, ucm.length() > 0, location, null );
		if( mounted.length() > 0 ) {
			vob.mount();
		}
	}

}
