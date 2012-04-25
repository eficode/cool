package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ActivityTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = getValue( "name", e, context );
		String comment = getComment( e, context );
		String headline = getValue( "headline", e, context, null );
		String inStr = getValue( "in", e, context, null );
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		
		Stream in = null;
		if( inStr != null ) {
			in = Stream.get( inStr, pvob );
		}
		
		Activity.create( name, in, pvob, true, comment, headline, context.path );
	}

}
