package net.praqma.clearcase.util;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.Components;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class DevelopmentEnvironment {

	public static void create( File devPath, Baseline offset, Stream integrationStream ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger);
		
		String append = System.currentTimeMillis()/1000 + "";
				
		/* Development */
		SnapshotView devview = Utilities.CreateView( "wolles_test_" + append + "@" + offset.getPVob(), integrationStream, offset, devPath, "wolles_" + append + "dev" );
		System.out.println("Development stream + view created");
		devview.Update( true, true, true, false, Components.MODIFIABLE, null );
		System.out.println("Development view updated");
	}
}
