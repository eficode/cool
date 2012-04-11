package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class VobTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		boolean ucm = e.getAttribute( "ucmproject" ).length() > 0;
		String tag = Cool.filesep + getValue( "tag", e, context );
		String location = e.getAttribute( "stgloc" );
		String mounted = e.getAttribute( "mounted" );
		
		Vob vob = Vob.create( tag, ucm, location, null );
		if( mounted.length() > 0 ) {
			vob.mount();
		}
		
		if( ucm ) {
			context.pvobs.add( (PVob) vob );
		}
	}

}
