package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.w3c.dom.Element;

public class ContextTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
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
		
	}

}
