package net.praqma.clearcase.util;

import java.io.File;
import java.util.Map;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class BootstrapProject {
	
	private static String vobtag = "\\TEST16";
	private static String pvobtag = "\\TEST16_PVOB";
	private static final String dynView = "baseview";
	private static final String bootstrapView = "testbootstrap_int";
	
	
	/**
	 * PVob, Vob, Component name, 
	 * @param options
	 * @throws UCMException
	 */
	
	public static void bootstrap(String name, String componentName, File viewPath ) throws UCMException {
		
		
		vobtag = "\\" + name;
		pvobtag = vobtag + "_PVOB";
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "ccbootstrap.log") );
        Cool.setLogger(logger);
        
        /* Dynamic view path */
        //File viewPath = new File( "M:\\" );
        
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

        /* Create PVob */
        System.out.println("Creating PVob " + pvobtag);	
		PVob pvob = PVob.create(pvobtag, null, "PVOB for testing");
		
		/* Create Vob */
		System.out.println("Creating Vob " + vobtag);
		Vob vob = Vob.create(vobtag, null, "Vob for testing");
		System.out.println("Loading Vob " + vob);
		vob.load();
		System.out.println("Path=" + vob.getStorageLocation());
		vob.mount();
		System.out.println("Mounted Vob " + vob);

		/* Create baseview */
		DynamicView baseview = DynamicView.create(null, dynView, null);
		System.out.println("View=" + baseview.getStorageLocation());
		
		/* Create component */
		Component c = Component.create("test", pvob, "Test", "Test component", new File( viewPath, dynView + vobtag ) );
		System.out.println("Component=" + c);
		
		/* Create project bootstrap */
		System.out.println("Creating bootstrap project");
		Project project = Project.create( "BootStrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
		System.out.println("Creating integration stream");
		
		/* Create integration stream */
		Baseline testInitial = UCMEntity.getBaseline( "test_INITIAL", pvob,	true );
		Stream intStream = Stream.createIntegration( "bootstrap_int", project, testInitial );
		
		/* Baselines */
		
		System.out.println("Creating integration view");
		DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
		//System.out.println("Creating baseline 1");
		//Baseline.create( "Structure_initial", c, new File(viewPath, bootstrapView), false, false );
		
		System.out.println("Creating Structure_1_0");
		Baseline structure = Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), false, true );
		
		System.out.println("Creating Mainline project");
		Project mainlineproject = Project.create( "test_mainline", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                               Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                               Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                               Project.POLICY_DELIVER_REQUIRE_REBASE, "Mainline project", c );
		
		System.out.println("Creating mainline integration stream");
		Stream mainlineIntStream = Stream.createIntegration( "mainline_int", mainlineproject, structure );
		
		System.out.println("Creating development project");
		Project developmentProject = Project.create( "test_development", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                                     Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                                     Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                                     Project.POLICY_DELIVER_REQUIRE_REBASE, "Development project", c );
		
		System.out.println("Creating development integration stream");
		Stream developmentIntStream = Stream.createIntegration( "test_developement_int", developmentProject, structure );
		
		System.out.println("Done...");
	}
	
	
	
	public static void main( String[] args ) throws UCMException {
		
		if( args.length < 1 ) {
			System.err.println( "No name given" );
			System.exit( 1 );
		}
		
		String name = args[0];
		
		vobtag = "\\" + name;
		pvobtag = vobtag + "_PVOB";
		
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
		
		Component c = Component.create("test", pvob, "Test", "Test component", new File( viewPath, dynView + vobtag ) );
		System.out.println("Component=" + c);
		
		
		System.out.println("Creating bootstrap project");
		Project project = Project.create( "TestBootStrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
		System.out.println("Creating integration stream");
		Baseline testInitial = UCMEntity.getBaseline( "test_INITIAL", pvob,	true );
		Stream intStream = Stream.createIntegration( "bootstrap_int", project, testInitial );
		
		/* Baselines */
		
		System.out.println("Creating integration view");
		DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
		System.out.println("Creating baseline 1");
		Baseline.create( "Structure_initial", c, new File(viewPath, bootstrapView), false, false );
		
		System.out.println("Creating baseline 2");
		Baseline structure = Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), false, true );
		
		System.out.println("Creating Mainline project");
		Project mainlineproject = Project.create( "test_mainline", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                               Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                               Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                               Project.POLICY_DELIVER_REQUIRE_REBASE, "Mainline project", c );
		
		System.out.println("Creating mainline integration stream");
		Stream mainlineIntStream = Stream.createIntegration( "mainline_int", mainlineproject, structure );
		
		System.out.println("Creating development project");
		Project developmentProject = Project.create( "test_development", null, pvob, Project.POLICY_INTERPROJECT_DELIVER  | 
				                                                                     Project.POLICY_CHSTREAM_UNRESTRICTED | 
				                                                                     Project.POLICY_DELIVER_NCO_DEVSTR    |
				                                                                     Project.POLICY_DELIVER_REQUIRE_REBASE, "Development project", c );
		
		System.out.println("Creating development integration stream");
		Stream developmentIntStream = Stream.createIntegration( "test_developement_int", developmentProject, structure );
		
		System.out.println("Done...");
	}
}
