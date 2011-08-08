package net.praqma.clearcase.test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class VersionTest {

	public static void main( String[] args ) throws UCMException, IOException {
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger);
		
		File path = new File( "C:/Temp/views2/chw_test_mainline_dev" );
		File file = new File( path, "Model/snade.txt" );
		
		file.createNewFile();
		
		SnapshotView sv = SnapshotView.GetSnapshotView( path );
		PVob pvob = new PVob( "\\Cool_PVOB" );
		Activity activity = Activity.create( "wolles_" + ( System.currentTimeMillis()/1000 ), pvob, true, null, path );
		Version version = Version.create( file, sv );
		version.checkIn();
		
		
		
	}

}
