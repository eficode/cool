package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class ActivityTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(ActivityTask.class.getSimpleName(), "parse", new Object[]{e, context});
		String name = getValue( "name", e, context );
		String comment = getComment( e, context );
		String headline = getValue( "headline", e, context, null );
		String inStr = getValue( "in", e, context, null );
		PVob pvob = new PVob( getValue( "pvob", e, context ) );

		Stream in = null;
		if( inStr != null ) {
			in = Stream.get( inStr, pvob );
		}

		Activity.create( name, in, pvob, true, comment, headline, context.path );
		tracer.exiting(ActivityTask.class.getSimpleName(), "parse");
	}

}
