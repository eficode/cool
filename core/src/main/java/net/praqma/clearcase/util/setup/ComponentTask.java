package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Component;

import org.w3c.dom.Element;

public class ComponentTask extends AbstractTask {

	@Override
	public void parse( Element e, File context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String root = e.getAttribute( "root" );
		String pvob = Cool.filesep + e.getAttribute( "pvob" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		
		if( name.equals( "" ) && root.equals( "" ) ) {
			throw new ClearCaseException( "Name and root not given" );
		}
		
		Component.create( name, new PVob( pvob ), root, comment, context );
	}

}
