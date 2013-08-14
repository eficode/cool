package net.praqma.clearcase.util.setup;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Rebase;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import org.w3c.dom.Element;

import java.util.logging.Logger;

public class RebaseTask extends AbstractTask {
	
	private static Logger logger = Logger.getLogger( RebaseTask.class.getName() );

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {

        String streamName = getValue( "stream", e, context, null );
        String baselineName = getValue( "baseline", e, context, null );
        PVob pvob = new PVob( getValue( "pvob", e, context ) );

        Stream stream = Stream.get( streamName, pvob );
        Baseline baseline = Baseline.get( baselineName, pvob );

        new Rebase( stream ).addBaseline( baseline ).rebase( true );
	}

}
