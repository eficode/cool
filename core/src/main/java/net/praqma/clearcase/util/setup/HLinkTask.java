package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class HLinkTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(HLinkTask.class.getSimpleName(), "parse", new Object[]{e, context});
		String name = e.getAttribute( "name" );
		String from = e.getAttribute( "name" );

		tracer.exiting(HLinkTask.class.getSimpleName(), "parse");
	}

}
