package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class MakeElementTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(MakeElementTask.class.getSimpleName(), "parse", new Object[]{e, context});
		File file = new File( e.getAttribute( "file" ) );
		String comment = getComment( e, context );

		File newfile = new File( context.path, e.getAttribute( "file" ) );

		if( !newfile.exists() ) {
			try {
				newfile.createNewFile();
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			}
		}

		Version.makeElement( file, context.path, comment );
		tracer.exiting(MakeElementTask.class.getSimpleName(), "parse");
	}

}
