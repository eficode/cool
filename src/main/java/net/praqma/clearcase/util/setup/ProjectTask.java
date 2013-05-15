package net.praqma.clearcase.util.setup;

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
		String name = getValue( "name", e, context );
		String comment = getComment( e, context );
		String model = getValue( "model", e, context );
		PVob pvob = new PVob( getValue( "pvob", e, context ) );
		String in = getValue( "in", e, context ).length() > 0 ? getValue( "in", e, context ) : null;
		
		List<Component> components = null;
		try {
			Element c = getFirstElement( e, "components" );
			components = new ArrayList<Component>();
			for( Element component : getElements( c ) ) {
				PVob cpvob = new PVob( getValue( "pvob", component, context ) );
				components.add( Component.get( component.getAttribute( "name" ), cpvob ) );
			}
		}  catch( Exception e1 ) {
			/* No components given, skipping */
		}
		
		int policy = 0;
		try {
			Element ps = getFirstElement( e, "policies" );
			for( Element p : getElements( ps ) ) {
				policy += Project.getPolicyValue( p.getTextContent() );
			}
		} catch( Exception e1 ) {
			/* No policies given, skipping */
		}
		
		context.projects.put( name, Project.create( name, in, pvob, policy, comment, model.length() == 0, components ) );
	}

}
