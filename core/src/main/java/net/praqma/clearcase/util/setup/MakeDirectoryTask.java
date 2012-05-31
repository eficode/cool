package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class MakeDirectoryTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		File dir = new File( e.getAttribute( "dir" ) );
		String comment = getComment( e, context );
		
		Version.makeDirectory( dir, context.path, comment );
	}

}
