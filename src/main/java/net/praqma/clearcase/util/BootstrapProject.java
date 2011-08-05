package net.praqma.clearcase.util;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class BootstrapProject {
	public static void main( String[] args ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "ccbootstrap.log") );
        Cool.setLogger(logger);

		
		PVob pvob = PVob.create("\\TEST_PVOB", null, "PVOB for testing");
		Vob vob = Vob.create("\\TEST", null, "Vob for testing");
		vob.load();
		System.out.println("Path=" + vob.getStorageLocation());
		vob.mount();
		
		Component c = Component.create("test", pvob, null, "Test component");
		System.out.println("Component=" + c);
		
		DynamicView view = DynamicView.create("test_view", null);
	}
}
