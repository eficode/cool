package net.praqma.clearcase.util;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class BootstrapProject {
	
	private static final String vobtag = "\\TEST4";
	private static final String pvobtag = "\\TEST4_PVOB";
	private static final String dynView = "test_view";
	
	public static void main( String[] args ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "ccbootstrap.log") );
        Cool.setLogger(logger);
        
        Vob v = Vob.get( vobtag );
        if( v != null ) {
        	System.out.println("Vob exists. Removing");
        	//v.unmount();
        	v.remove();
        }
        
        PVob pv = PVob.get( pvobtag );
        if( pv != null ) {
        	System.out.println("PVob exists. Removing");
        	//pv.unmount();
        	pv.remove();
        }
        
        try {
        	DynamicView dv = new DynamicView(null,dynView);
        	dv.removeView();
        } catch( Exception e ) {
        	System.out.println("Error while removing: " + e.getMessage());
        }

        System.out.println("Creating PVob " + pvobtag);	
		PVob pvob = PVob.create(pvobtag, null, "PVOB for testing");
		System.out.println("Creating Vob " + vobtag);
		Vob vob = Vob.create(vobtag, null, "Vob for testing");
		System.out.println("Loading Vob " + vob);
		vob.load();
		System.out.println("Path=" + vob.getStorageLocation());
		vob.mount();
		System.out.println("Mounted Vob " + vob);
		
		Component c = Component.create("test", pvob, null, "Test component");
		System.out.println("Component=" + c);
		
		DynamicView view = DynamicView.create(null,dynView);
		System.out.println("View=" + view.getStorageLocation());
		
		System.out.println("Creating project");
		Project project = Project.create( "TestBootStrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
		System.out.println("Creating integration stream");
		Stream intStream = Stream.createIntegration( "test_int", project, c );
		
		
		
		System.out.println("Done...");
	}
}
