package net.praqma.clearcase.util.setup;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class StreamTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = getValue( "name", e, context );
		boolean integration = e.getAttribute( "type" ).equals( "integration" );
		String comment = getComment( e, context );
		String in = getValue( "in", e, context, null );
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		boolean readonly = getValue( "readonly", e, context ).length() > 0;
		
		if( in == null ) {
			throw new ClearCaseException( "StreamTask: In can not be null" );
		}
		
		List<Baseline> baselines = null;
		
		try {
			Element c = getFirstElement( e, "baselines" );
			baselines = new ArrayList<Baseline>();
			for( Element baseline : getElements( c ) ) {
				PVob bpvob = new PVob( Cool.filesep + getValue( "pvob", baseline, context ) );
				baselines.add( Baseline.get( baseline.getAttribute( "name" ), bpvob ) );
			}
		} catch( Exception e1 ) {
			/* No baselines given, skipping */
		}
		
		if( integration ) {
			Stream.createIntegration( name, Project.get( in, pvob ), baselines );
		} else {
			Stream.create( Stream.get( in, pvob ), name, readonly, baselines );
		}
	}

}
