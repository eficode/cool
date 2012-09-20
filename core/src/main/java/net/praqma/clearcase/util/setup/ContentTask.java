package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ContentTask extends AbstractTask {
	
	private static Logger logger = Logger.getLogger( ContentTask.class.getName() );

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
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
		
		
	}

}
