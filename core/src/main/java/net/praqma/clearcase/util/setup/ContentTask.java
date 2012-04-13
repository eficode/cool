package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ContentTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		File file = new File( context.path, e.getAttribute( "file" ) );
		String content = e.getAttribute( "content" );
		
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

}
