package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class MakeDirectoryTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(MakeDirectoryTask.class.getSimpleName(), "parse", new Object[]{e, context});
		File dir = new File( e.getAttribute( "dir" ) );
		String comment = getComment( e, context );

		Version.makeDirectory( dir, context.path, comment );
		tracer.exiting(MakeDirectoryTask.class.getSimpleName(), "parse");
	}

}
