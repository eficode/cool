package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class SetActivityTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String astr = e.getAttribute( "activity" );
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		PVob pvob = new PVob( Cool.filesep + getValue( "pvob", e, context ) );
		
		Activity activity = Activity.get( astr, pvob );
		
		UCMView.setActivity( activity, context.path, null, comment );
	}

}
