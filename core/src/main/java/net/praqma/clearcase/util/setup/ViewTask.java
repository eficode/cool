package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class ViewTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String tag = e.getAttribute( "tag" );
		String stgloc = e.getAttribute( "stgloc" );
		Stream stream = e.getAttribute( "stream" ).length() > 0 ? Stream.get( e.getAttribute( "stream" ) ) : null;
		boolean snapshot = e.getAttribute( "snapshot" ).length() > 0;
		
		if( snapshot) {
			
		} else {
			DynamicView.create( stgloc, tag, stream );
		}
		
	}

}
