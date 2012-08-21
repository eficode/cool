package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.w3c.dom.Element;

public class ContextTask extends AbstractTask {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
tracer.entering(ContextTask.class.getSimpleName(), "parse", new Object[]{e, context});
		String view = getValue( "view", e, context );
		String vob = getValue( "vob", e, context );
		String mvfs = "";
		
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			mvfs = e.getAttribute( "mvfs" );
			context.path = new File( mvfs + "/" + view + "/" + vob );
		} else {
			mvfs = e.getAttribute( "linux" );
			context.path = new File( mvfs + "/" + view + "/" + vob );
		}
		
		context.mvfs = mvfs;
		
tracer.exiting(ContextTask.class.getSimpleName(), "parse");
	}

}
