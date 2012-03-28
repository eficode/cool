package net.praqma.clearcase.util.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;

import org.w3c.dom.Element;

public class StreamTask extends AbstractTask {

	@Override
	public void parse( Element e, File context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		boolean integration = e.getAttribute( "type" ).equals( "integration" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		String in = e.getAttribute( "in" ).length() > 0 ? e.getAttribute( "in" ) : null;
		PVob pvob = getPVob(  e.getAttribute( "pvob" ) );
		
		Element c = getFirstElement( e, "baselines" );
		List<Baseline> baselines = new ArrayList<Baseline>();
		for( Element c2 : getElements( c ) ) {
			baselines.add( Baseline.get( c2.getAttribute( "name" ), pvob ) );
		}
		
		if( integration ) {
			Stream.createIntegration( name, Project.get( in, pvob ), baselines );
		} else {
			
		}
	}

}
