package net.praqma.clearcase.util;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class BootstrapProject {
	
	private static final String vobtag = "\\TEST12";
	private static final String pvobtag = "\\TEST12_PVOB";
	private static final String dynView = "test_view";
	private static final String bootstrapView = "testbootstrap_int";
	
	public static void main( String[] args ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "ccbootstrap.log") );
        Cool.setLogger(logger);
        
        /* Dynamic view path */
        File viewPath = new File( "M:\\" );
        
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
        
        try {
        	DynamicView dv = new DynamicView(null,bootstrapView);
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

		DynamicView view = DynamicView.create(null, dynView, null);
		System.out.println("View=" + view.getStorageLocation());
		
		Component c = Component.create("test", pvob, "Test", "Test component");
		System.out.println("Component=" + c);
		
		
		System.out.println("Creating project");
		Project project = Project.create( "TestBootStrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
		System.out.println("Creating integration stream");
		Stream intStream = Stream.createIntegration( "test_int", project, c );
		
		/* Baselines */
		
		System.out.println("Creating integration view");
		DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
		System.out.println("Creating baseline 1");
		Baseline.create( "Structure_initial", c, new File(viewPath, bootstrapView), false, false );
		
		System.out.println("Creating baseline 2");
		Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), false, true );
		
		System.out.println("Creating Mainline project");
		Project mainlineproject = Project.create( "test_mainline", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                               Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                               Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                               Project.POLICY_DELIVER_REQUIRE_REBASE, "Mainline project", c );
		
		System.out.println("Creating mainline integration stream");
		Stream mainlineIntStream = Stream.createIntegration( "mainline_int", mainlineproject, c );
		
		System.out.println("Creating development project");
		Project developmentProject = Project.create( "test_development", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                                     Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                                     Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                                     Project.POLICY_DELIVER_REQUIRE_REBASE, "Development project", c );
		
		System.out.println("Done...");
	}
}
