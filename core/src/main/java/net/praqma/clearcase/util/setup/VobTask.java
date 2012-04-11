package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class VobTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		boolean ucm = e.getAttribute( "ucmproject" ).length() > 0;
		String tag = Cool.filesep + getValue( "tag", e, context );
		String location = e.getAttribute( "stgloc" );
		String mounted = e.getAttribute( "mounted" );
		
		Vob vob = null;
		try {
			vob = Vob.create( tag, ucm, location, null );
			if( ucm ) {
				context.pvobs.add( (PVob) vob );
			}
		} catch( EntityAlreadyExistsException e1 ) {
			System.out.println( "VOB: " + tag );
			if( ucm ) {
				System.out.println( "THIS IS A PVOB" );
				vob = new PVob( tag );
				context.pvobs.add( (PVob) vob );
			} else {
				System.out.println( "THIS IS NOT A PVOB" );
				vob = new Vob( tag );
			}
			vob.load();
		}
		
		if( mounted.length() > 0 ) {
			vob.mount();
		}
		
	}

}
