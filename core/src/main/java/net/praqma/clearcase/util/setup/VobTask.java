package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

import org.w3c.dom.Element;

public class VobTask extends AbstractTask {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
	private static Logger logger = Logger.getLogger();

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(VobTask.class.getSimpleName(), "parse", new Object[]{e, context});
		tracer.finest("Retrieving required information.");
		
		boolean ucm = e.getAttribute( "ucmproject" ).length() > 0;
		String store = getValue( "storetag", e, context, null );
		String tag = getTag( e, context );
		String location = e.getAttribute( "stgloc" );
		boolean mount = e.getAttribute( "mounted" ).length() > 0;
		
		tracer.finest("Attempting to create and load PVob...");
		try {
			tracer.finest("Checking if ucm flag is set.");
			if( ucm ) {
				tracer.finest("ucm flag is set.");
				tracer.finest("Creating PVob.");
				/* TODO Add a test attribute to the pvob */
				PVob vob = PVob.create( tag, location, null );
				tracer.finest("Adding PVob to Context PVobs");
				context.pvobs.put( tag, vob );
				tracer.finest("Checking if mount flag is set.");
				if( mount ) {
					tracer.finest(String.format("mount flag is set, mounting PVob: %s", vob));
					vob.mount();
				}
				tracer.finest(String.format("Loading PVob: %s", vob));
				vob.load();
			} else {
				tracer.finest("ucm flag is not set.");
				Vob vob = Vob.create( tag, ucm, location, null );
				if( mount ) {
					vob.mount();
				}
				vob.load();
			}
		} catch( EntityAlreadyExistsException e1 ) {
			tracer.finest("Could not create Vob");
			tracer.finest(String.format("Caught exception: %s", e1));
			tracer.finest("Checking if ucm flag is set.");
			if( ucm ) {
				tracer.finest("ucm flag is set.");
				logger.debug( "The pvob already exists, tear it down" );
				/* TODO Make sure this pvob has a test attribute */
				
				tracer.finest("Tearing down PVob.");
				PVob vob = new PVob( tag );
				/* Tear it down */
				SetupUtils.tearDown( vob );
				
				/* TODO Add a test attribute to the pvob */
				
				tracer.finest("Creating new PVob");
				vob = PVob.create( tag, location, null );
				tracer.finest("Adding PVob to Context PVobs");
				context.pvobs.put( tag, vob );
				tracer.finest("Checking if mount flag is set.");
				if( mount ) {
					tracer.finest(String.format("mount flag is set, mounting PVob: %s", vob));
					vob.mount();
				}
				tracer.finest(String.format("Loading PVob: %s", vob));
				vob.load();
			} else {
				tracer.finest("ucm flag is not set, creating new Vob");
				Vob vob = new Vob( tag );
				tracer.finest("Checking if mount flag is set.");
				if( mount ) {
					tracer.finest(String.format("mount flag is set, mounting Vob: %s", vob));
					vob.mount();
				}
				tracer.finest(String.format("Loading Vob: %s", vob));
				vob.load();
			}
			
		}
		
		tracer.finest("Checking if store is null.");
		/* Store */
		if( store != null ) {
			tracer.finest("store is not null, adding vob to the context.");
			context.put( store, tag );
		}
		tracer.exiting(VobTask.class.getSimpleName(), "parse");
	}

}
