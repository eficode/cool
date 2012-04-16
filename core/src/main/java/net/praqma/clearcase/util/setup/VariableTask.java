package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class VariableTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String value = e.getAttribute( "value" );
		
		context.put( name, value );
	}

}
