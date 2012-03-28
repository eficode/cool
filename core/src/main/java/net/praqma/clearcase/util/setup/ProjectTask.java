package net.praqma.clearcase.util.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ProjectTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		String model = e.getAttribute( "model" );
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		String in = e.getAttribute( "in" ).length() > 0 ? e.getAttribute( "in" ) : null;
		
		Element c = getFirstElement( e, "components" );
		List<Component> components = new ArrayList<Component>();
		for( Element component : getElements( c ) ) {
			components.add( Component.get( component.getAttribute( "name" ), new PVob( component.getAttribute( "pvob" ) ) ) );
		}
		
		Element ps = getFirstElement( e, "policies" );
		int policy = 0;
		for( Element p : getElements( ps ) ) {
			policy += Project.getPolicyValue( p.getTextContent() );
		}
		
		Project.create( name, in, pvob, policy, comment, model.length() > 0, components );
	}

}
