package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class AddElementTask extends AbstractTask {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(AddElementTask.class.getSimpleName(), "parse", new Object[]{e, context});
		File file = new File( context.path, e.getAttribute( "file" ) );
		String content = getValue( "content", e, context, null );

		if( !file.exists() ) {
			try {
				file.createNewFile();
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			}
		}

		if( content != null ) {
			FileWriter fw = null;
			try {
				fw = new FileWriter( file, true );
				fw.write( content );
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			} finally {
				try {
					fw.close();
				} catch( IOException e1 ) {
					throw new ClearCaseException( e1 );
				}
			}
		}

		Version.addToSourceControl( file, context.path, null, true );
		tracer.exiting(AddElementTask.class.getSimpleName(), "parse");
	}

}
