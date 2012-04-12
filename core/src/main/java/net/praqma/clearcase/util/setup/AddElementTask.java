package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class AddElementTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		File file = new File( e.getAttribute( "file" ) );
		
		Version.addToSourceControl( file, file.isDirectory(), context.path );
	}

}
