package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ComponentTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String root = e.getAttribute( "root" ) != null && e.getAttribute( "root" ).length() > 0 ? e.getAttribute( "root" ) : null;
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		
		if( name.equals( "" ) && root.equals( "" ) ) {
			throw new ClearCaseException( "Name and root not given" );
		}
		
		context.components.put( name, Component.create( name, pvob, root, comment, context.path ) );
	}

}
