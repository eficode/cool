package net.praqma.clearcase.util.setup;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.*;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.view.*;
import net.praqma.clearcase.util.setup.EnvironmentParser.*;
import org.w3c.dom.*;

import java.util.logging.*;

@SuppressFBWarnings("")
public class ViewTask extends AbstractTask {

    private static final Logger log = Logger.getLogger( ViewTask.class.getName() );

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String tag = getValue( "tag", e, context );
		String stgloc = e.getAttribute( "stgloc" );
		boolean snapshot = e.getAttribute( "snapshot" ).length() > 0;
		
		Stream stream = null;
		try {
			Element s = getFirstElement( e, "stream" );
			PVob pvob = new PVob( getValue( "pvob", s, context ) );
			String name = getValue( "name", s, context );
			stream = Stream.get( name, pvob );
		} catch( Exception e1 ) {
			/* No stream given */
		}

        String testString = new String( tag );

		if( snapshot ) {
			
		} else {
			if( DynamicView.viewExists( tag ) ) {
				/* Remove it! */
				UCMView v = UCMView.getView( tag );
				try {
					v.end();
				} catch( ClearCaseException e1 ) {
					/* Not ended */
				}
				v.remove();
			}
			
			context.views.put( tag, DynamicView.create( stgloc, tag, stream ) );
		}
		
	}

}
