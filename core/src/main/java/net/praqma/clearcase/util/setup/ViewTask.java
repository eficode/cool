package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;

import org.w3c.dom.Element;

public class ViewTask extends AbstractTask {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		tracer.entering(ViewTask.class.getSimpleName(), "parse", new Object[]{e, context});
		tracer.finest("Getting required values.");
		String tag = getValue( "tag", e, context );
		String stgloc = e.getAttribute( "stgloc" );
		boolean snapshot = e.getAttribute( "snapshot" ).length() > 0;
		
		Stream stream = null;
		tracer.finest("Attempting to get the PVob from stream...");
		try {
			tracer.finest("Getting PVob and Stream.");
			Element s = getFirstElement( e, "stream" );
			PVob pvob = new PVob( getValue( "pvob", s, context ) );
			String name = getValue( "name", s, context );
			stream = Stream.get( name, pvob );
			tracer.finest("Successfully fetched the Stream");
		} catch( Exception e1 ) {
			tracer.finest("Could not get the Stream.");
			/* No stream given */
		}
		
		tracer.finest("Checking if snapshot flag is set.");
		if( snapshot ) {
			tracer.finest("snapshot flag is set.");
		} else {
			tracer.finest("snapshot flag is not set.");
			tracer.finest(String.format("Checking if DynamicView with tag %s exits.", tag));
			if( DynamicView.viewExists( tag ) ) {
				tracer.finest("DynamicView exists.");
				/* Remove it! */
				UCMView v = UCMView.getView( tag );
				tracer.finest("Attempting to end View...");
				try {
					v.end();
					tracer.finest("Successfully ended View.");
				} catch( ClearCaseException e1 ) {
					tracer.finest("Could not end View.");
					/* Not ended */
				}
				tracer.finest(String.format("Removing View: %s", v));
				v.remove();
			}
			tracer.finest("Creating new DynamicView and adding to the Context Views.");
			context.views.put( tag, DynamicView.create( stgloc, tag, stream ) );
		}
		tracer.exiting(ViewTask.class.getSimpleName(), "parse");
		
	}

}
