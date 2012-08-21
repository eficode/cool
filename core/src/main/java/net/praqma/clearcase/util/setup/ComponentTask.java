package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class ComponentTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(ComponentTask.class.getSimpleName(), "parse", new Object[]{e, context});
		String name = e.getAttribute( "name" );
		String root = e.getAttribute( "root" ) != null && e.getAttribute( "root" ).length() > 0 ? e.getAttribute( "root" ) : null;
		PVob pvob = new PVob( getValue( "pvob", e, context ) );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;

		if( name.equals( "" ) && root.equals( "" ) ) {
			throw new ClearCaseException( "Name and root not given" );
		}

		context.components.put( name, Component.create( name, pvob, root, comment, context.path ) );
		tracer.exiting(ComponentTask.class.getSimpleName(), "parse");
	}

}
