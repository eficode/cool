package net.praqma.clearcase.util;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.exceptions.NotMountedException;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class BootstrapProject {

	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private static final String dynView = "COOLbaseview";
	private static final String bootstrapView = "COOLbootstrapview";

	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();



	public static void main( String[] args ) throws UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException, CleartoolException, UCMEntityNotFoundException, UnableToGetEntityException, ViewException, EntityAlreadyExistsException, NotMountedException {
		tracer.entering(BootstrapProject.class.getSimpleName(), "main", args);

		tracer.finest("Checking if there are 1 or less args.");
		if( args.length < 2 ) {
			tracer.finest("There are 1 or less args.");
			System.err.println( "No path given" );
			tracer.finest("Exiting with exit code 1.");
			System.exit( 1 );
		}

		String vname = args[0];
		String cname = args[1];

		tracer.finest("Calling bootstrap method.");
		bootstrap( vname, cname, new File( "M:\\" ), Project.POLICY_INTERPROJECT_DELIVER  | 
				Project.POLICY_CHSTREAM_UNRESTRICTED | 
				Project.POLICY_DELIVER_NCO_DEVSTR    |
				Project.POLICY_DELIVER_REQUIRE_REBASE );
		tracer.exiting(BootstrapProject.class.getSimpleName(), "main");
	}

	public static void bootstrap( String vobname, String componentName, File viewPath, int policies ) throws UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException, CleartoolException, UCMEntityNotFoundException, UnableToGetEntityException, ViewException, EntityAlreadyExistsException, NotMountedException {
		tracer.entering(BootstrapProject.class.getSimpleName(), "bootstrap", new Object[]{vobname, componentName, viewPath, policies});

		System.out.println("Bootstrapping");
		String vobtag = "\\" + vobname;
		String pvobtag = vobtag + "_PVOB";

		Logger.addAppender( app );

		/* Dynamic view path */
		//File viewPath = new File( "M:\\" );

		tracer.finest("Getting Vob.");
		Vob v = Vob.get( vobtag );
		tracer.finest("Checking if Vob exists");
		if( v != null ) {
			tracer.finest("Vob exists.");
			System.out.println("Vob exists. Removing");
			//v.unmount();
			tracer.finest("Removing Vob");
			v.remove();
		}

		tracer.finest("Getting PVob.");
		PVob pv = PVob.get( pvobtag );
		tracer.finest("Checking if PVob exists.");
		if( pv != null ) {
			tracer.finest("PVob exists.");
			System.out.println("PVob exists. Removing");
			//pv.unmount();
			tracer.finest("Removing PVob");
			pv.remove();
		}

		tracer.finest("Attempting to remove DynamicView.");
		try {
			System.out.println("Removing baseview");
			DynamicView dv = new DynamicView(null,dynView);
			dv.remove();
			tracer.finest("Successfully removed DynamicView.");
		} catch( Exception e ) {
			tracer.finest("Error while removing DynamicView.");
			System.out.println("Error while removing: " + e.getMessage());
		}

		tracer.finest("Creating PVob.");
		/* Create PVob */
		System.out.println("Creating PVob " + pvobtag);	
		PVob pvob = PVob.create(pvobtag, null, "PVOB for testing");

		tracer.finest("Creating Vob.");
		/* Create Vob */
		System.out.println("Creating Vob " + vobtag);
		Vob vob = Vob.create(vobtag, null, "Vob for testing");
		System.out.println("Loading Vob " + vob);
		vob.load();
		System.out.println("Path=" + vob.getStorageLocation());
		vob.mount();
		System.out.println("Mounted Vob " + vob);

		tracer.finest("Creating DynamicView.");
		/* Create baseview */
		DynamicView baseview = DynamicView.create(null, dynView, null);
		System.out.println("View=" + baseview.getStorageLocation());

		tracer.finest("Creating Component.");
		/* Create component */
		Component c = Component.create(componentName, pvob, componentName, "Test component", new File( viewPath, dynView + vobtag ) );
		System.out.println("Component=" + c);

		tracer.finest("Creating Project bootstrap.");
		/* Create project bootstrap */
		System.out.println("Creating bootstrap project");
		Project project = Project.create( "Bootstrap", null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Bootstrap project", true, c );
		System.out.println("Creating integration stream");

		tracer.finest("Creating integration Stream.");
		/* Create integration stream */
		Baseline testInitial = Baseline.get( componentName + "_INITIAL", pvob );
		Stream intStream = Stream.createIntegration( "Bootstrap_int", project, testInitial );

		tracer.finest("Creating integration View.");
		/* Baselines */

		System.out.println("Creating integration view");
		DynamicView bootstrap_int = DynamicView.create(null, bootstrapView, intStream);
		//System.out.println("Creating baseline 1");
		//Baseline.create( "Structure_initial", c, new File(viewPath, bootstrapView), false, false );

		tracer.finest("Creating Baseline structure.");
		System.out.println("Creating Structure_1_0");
		Baseline structure = Baseline.create( "Structure_1_0", c, new File(viewPath, bootstrapView), LabelBehaviour.FULL, true );

		tracer.finest("Creating Project mainlineproject.");
		System.out.println("Creating Mainline project");
		Project mainlineproject = Project.create( "Mainline", null, pvob, policies, "Mainline project", true, c );

		tracer.finest("Creating Stream mainlineIntStream.");
		System.out.println("Creating Mainline integration stream");
		Stream mainlineIntStream = Stream.createIntegration( "Mainline_int", mainlineproject, structure );

		tracer.finest("Creating Project developmentProject.");
		System.out.println("Creating development project");
		Project developmentProject = Project.create( "Development", null, pvob, policies, "Development project", true, c );

		tracer.finest("Creating Stream developmentIntStream.");
		System.out.println("Creating development integration stream");
		Stream developmentIntStream = Stream.createIntegration( "Development_int", developmentProject, structure );

		System.out.println("Bootstrapping done");
		tracer.exiting(BootstrapProject.class.getSimpleName(), "bootstrap");
	}
}
