package net.praqma.clearcase.util.setup;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Component;

import org.w3c.dom.Element;

public class BaselineTask extends AbstractTask {

	@Override
	public void parse( Element e ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String label = e.getAttribute( "label" );
		String identical = e.getAttribute( "identical" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		PVob pvob = getPVob(  e.getAttribute( "pvob" ) );
		Component component = Component.get( e.getAttribute( "component" ), pvob );
		
		Element c = getFirstElement( e, "components" );
		List<Component> components = new ArrayList<Component>();
		for( Element c2 : getElements( c ) ) {
			components.add( Component.get( c2.getAttribute( "name" ), new PVob( c2.getAttribute( "pvob" ) ) ) );
		}
		
		Baseline.create( name, component, EnvironmentParser.getContext(), LabelBehaviour.valueOf( label ), identical.length() > 0, null, components );
	}

}
