package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

import org.w3c.dom.Element;

public class ContentTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private static Logger logger = Logger.getLogger();

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(ContentTask.class.getSimpleName(), "parse", new Object[]{e, context});
		File file = new File( context.path, e.getAttribute( "file" ) );
		String content = getValue( "content", e, context, "" );

		FileWriter fw = null;
		try {
			fw = new FileWriter( file, true );
			fw.write( content );
		} catch( IOException e1 ) {
			throw new ClearCaseException( e1 );
		} finally {
			try {
				fw.close();
			} catch( Exception e1 ) {
				//throw new ClearCaseException( e1 );
			}
		}
		tracer.exiting(ContentTask.class.getSimpleName(), "parse");
	}

}
