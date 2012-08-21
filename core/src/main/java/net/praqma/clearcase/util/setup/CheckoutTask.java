package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class CheckoutTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(CheckoutTask.class.getSimpleName(), "parse", new Object[]{e, context});
		File file = new File( e.getAttribute( "file" ) );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;

		Version.checkOut( file, context.path, comment );
		tracer.exiting(CheckoutTask.class.getSimpleName(), "parse");
	}

}
