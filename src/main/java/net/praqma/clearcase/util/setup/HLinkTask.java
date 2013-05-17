package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class HLinkTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String from = e.getAttribute( "name" );
		
	}

}
