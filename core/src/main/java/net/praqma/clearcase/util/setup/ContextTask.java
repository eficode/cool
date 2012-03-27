package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.w3c.dom.Element;

public class ContextTask extends AbstractTask {

	@Override
	public void parse( Element e ) throws ClearCaseException {
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			String mvfs = e.getAttribute( "mvfs" );
			String view = e.getAttribute( "view" );
			String vob = e.getAttribute( "vob" );
			EnvironmentParser.setContext( new File( mvfs + "/" + view + "/" + vob ) );
		} else {
			/* Not implemented */
		}
		
	}

}
