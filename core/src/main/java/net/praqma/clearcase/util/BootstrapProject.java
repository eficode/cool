package net.praqma.clearcase.util;

import java.io.File;
import java.util.Map;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class BootstrapProject {
	
	private static final String dynView = "COOLbaseview";
	private static final String bootstrapView = "COOLbootstrapview";
	
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();
	
	

	public static void main( String[] args ) throws ViewException, CleartoolException, UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, NothingNewException, UnableToGetEntityException {
		
		if( args.length < 2 ) {
			System.err.println( "No path given" );
			System.exit( 1 );
		}
		
		String vname = args[0];
		String cname = args[1];
		
		bootstrap( vname, cname, new File( "M:\\" ), Project.POLICY_INTERPROJECT_DELIVER  | 
                                                     Project.POLICY_CHSTREAM_UNRESTRICTED | 
                                                     Project.POLICY_DELIVER_NCO_DEVSTR    |
                                                     Project.POLICY_DELIVER_REQUIRE_REBASE );
	}
	
	public static void bootstrap( String vobname, String componentName, File viewPath, int policies ) throws ViewException, CleartoolException, UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, NothingNewException, UnableToGetEntityException {
		
		System.out.println("Bootstrapping");
		String vobtag = "\\" + vobname;
		String pvobtag = vobtag + "_PVOB";
		
		Logger.addAppender( app );
        
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
        	System.out.println("Removing baseview");
        	DynamicView dv = new DynamicView(null,dynView);
        	dv.remove();
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
		Component c = Component.create(componentName, pvob, componentName, "Test component", new File( viewPath, dynView + vobtag ) );
		System.out.println("Component=" + c);
		
		/* Create project bootstrap */
		System.out.println("Creating bootstrap project");
		Project project = Project.create( "Bootstrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", c );
		System.out.println("Creating integration stream");
		
		/* Create integration stream */
		Baseline testInitial = Baseline.get( componentName + "_INITIAL", pvob );
		Stream intStream = Stream.createIntegration( "Bootstrap_int", project, testInitial );
		
		/* Baselines */
		
		System.out.println("Creating integration view");
		DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
		//System.out.println("Creating baseline 1");
		//Baseline.create( "Structure_initial", c, new File(viewPath, bootstrapView), false, false );
		
		System.out.println("Creating Structure_1_0");
		Baseline structure = Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), false, true );
		
		System.out.println("Creating Mainline project");
		Project mainlineproject = Project.create( "Mainline", null, pvob, policies, "Mainline project", c );
		
		System.out.println("Creating Mainline integration stream");
		Stream mainlineIntStream = Stream.createIntegration( "Mainline_int", mainlineproject, structure );
		
		System.out.println("Creating development project");
		Project developmentProject = Project.create( "Development", null, pvob, policies, "Development project", c );
		
		System.out.println("Creating development integration stream");
		Stream developmentIntStream = Stream.createIntegration( "Development_int", developmentProject, structure );
		
		System.out.println("Bootstrapping done");
	}
	
	
	}
