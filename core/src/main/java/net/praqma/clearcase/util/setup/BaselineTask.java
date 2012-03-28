package net.praqma.clearcase.util.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class BaselineTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String label = e.getAttribute( "label" );
		String identical = e.getAttribute( "identical" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		Component component = Component.get( e.getAttribute( "component" ), pvob );
		
		Element c = getFirstElement( e, "components" );
		List<Component> components = new ArrayList<Component>();
		for( Element c2 : getElements( c ) ) {
			components.add( Component.get( c2.getAttribute( "name" ), new PVob( c2.getAttribute( "pvob" ) ) ) );
		}
		
		Baseline.create( name, component, context.path, LabelBehaviour.valueOf( label ), identical.length() > 0, null, components );
	}

}
