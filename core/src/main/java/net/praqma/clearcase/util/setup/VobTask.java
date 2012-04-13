package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.util.debug.Logger;

import org.w3c.dom.Element;

public class VobTask extends AbstractTask {
	
	private static Logger logger = Logger.getLogger();

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		boolean ucm = e.getAttribute( "ucmproject" ).length() > 0;
		String tag = Cool.filesep + getValue( "tag", e, context );
		String location = e.getAttribute( "stgloc" );
		boolean mount = e.getAttribute( "mounted" ).length() > 0;
		
		try {
			if( ucm ) {
				/* TODO Add a test attribute to the pvob */
				PVob vob = PVob.create( tag, location, null );
				context.pvobs.add( vob );
				if( mount ) {
					vob.mount();
				}
				vob.load();
			} else {
				Vob vob = Vob.create( tag, ucm, location, null );
				if( mount ) {
					vob.mount();
				}
				vob.load();
			}
		} catch( EntityAlreadyExistsException e1 ) {
			if( ucm ) {
				logger.debug( "The pvob already exists, tear it down" );
				/* TODO Make sure this pvob has a test attribute */
				
				PVob vob = new PVob( tag );
				/* Tear it down */
				SetupUtils.tearDown( vob );
				
				/* TODO Add a test attribute to the pvob */
				vob = PVob.create( tag, location, null );
				context.pvobs.add( vob );
				if( mount ) {
					vob.mount();
				}
				vob.load();
			} else {
				Vob vob = new Vob( tag );
				if( mount ) {
					vob.mount();
				}
				vob.load();
			}
			
		}
		
	}

}
