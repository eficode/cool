package net.praqma.clearcase.test.junit;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;

public class Bootstrap {
	
	private static Logger logger = Logger.getLogger();
	
	public PVob pvob;
	public String pvobStr;
	public File basepath;
	
	public String dynamicViewTag;
	public String bootstrapViewTag;
	
	public File prefix;
	public File bootstrappath;
	
	public File viewpath;
	public String basepathStr;
	public DynamicView baseView;
	public DynamicView bootstrapView;
	
	public Project project;
	public Stream integrationStream;
	
	public Component systemComponent;
	public Component modelComponent;
	public Component clientComponent;
	
	public Baseline structure;
	
	protected boolean removePvob;
	protected boolean fail;
	protected boolean tearDownAsMuchAsPossible = true;
	
	public Bootstrap() {
		
		/* Check options */
		pvobStr = System.getProperty( "pvob", "TESTING_PVOB" );
		basepathStr = System.getProperty( "path", "" );
		prefix = new File( System.getProperty( "path", "m:/" ) );
		viewpath = new File( System.getProperty( "viewpath", "views" ) );
		dynamicViewTag = System.getProperty( "viewtag", "TestDynamicView" );
		bootstrapViewTag = System.getProperty( "bootstrapviewtag", "TestBootstrapView" );
	}
	
	public boolean setUp() {

		String pvob = Cool.filesep + pvobStr;

		removePvob = false;
		PVob pv = PVob.get( pvob );
		if( pv == null ) {
			logger.info( "Creating " + pvob );
			try {
				logger.verbose( "Creating pvob " + pvob );
				this.pvob = PVob.create( pvob, null, "testing" );
				removePvob = true;
				this.pvob.mount();
				logger.verbose( "Creating dynamic view" );
				baseView = DynamicView.create( null, dynamicViewTag, null );
				logger.verbose( "Starting view" );
				new DynamicView( null, dynamicViewTag ).startView();
				
			} catch( ClearCaseException e ) {
				e.print( System.err );
				fail = true;
			}
		} else {
			logger.fatal( "The PVob " + pvob + " already exists" );
			fail = true;
		}
		
		/* Base path */
		basepath = new File( prefix, dynamicViewTag + "/" + this.pvob.getName() );

		logger.verbose( "Preparing " + basepath.getAbsolutePath() );
		basepath.mkdirs();
		logger.verbose( "Preparing " + viewpath.getAbsolutePath() );
		viewpath.mkdirs();
		
		logger.verbose( "Base path is " + basepath.getAbsolutePath() );
		
		return fail;
	}


	public boolean bootStrap( String projectName, String integrationName ) throws Exception {
		try {
			/* Unrooted component */
			systemComponent = Component.create( "_System", pvob, null, "Unrooted system component", basepath );
			
			/* Rooted components */
			modelComponent = Component.create( "Model", pvob, "Model", "Model component", basepath ).load();
			clientComponent = Component.create( "Client", pvob, "Client", "Client component", basepath ).load();
			
			project = Project.create( projectName, null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Test", modelComponent, clientComponent );
			
			/**/
			Baseline SystemINITIAL = Baseline.get( "_System_INITIAL", pvob ).load();
			Baseline ModelINITIAL = Baseline.get( "Model_INITIAL", pvob ).load();
			Baseline ClientINITIAL = Baseline.get( "Client_INITIAL", pvob ).load();
			integrationStream = Stream.createIntegration( integrationName, project, SystemINITIAL, ModelINITIAL, ClientINITIAL );
			
			/**/
			bootstrapView = DynamicView.create( null, bootstrapViewTag, integrationStream );
			bootstrappath = new File( prefix, bootstrapViewTag + "/" + this.pvob.getName() );
			
			structure = Baseline.create( "Structure", systemComponent, bootstrappath, LabelBehaviour.DEFAULT, false, null, new Component[] { modelComponent, clientComponent } );
			
			return true;
		} catch( Exception e ) {
			throw e;
		}
	}


	public boolean tearDown() {
		logger.info( "Tear down ClearCase" );
		boolean tearDownSuccess = true;

		if( removePvob ) {
			try {
				/* Removing views */
				Set<String> viewTags = UCMView.getViews().keySet();
				for( String viewTag : viewTags ) {
					try {
						UCMView.getViews().get( viewTag ).end().remove();
					} catch( ClearCaseException e ) {
						tearDownSuccess = false;
						e.log();
						if( !tearDownAsMuchAsPossible ) {
							throw e;
						}
					}
				}
				
				/* Removing baseview */
				/*
				logger.verbose( "Removing base view" );
				try {
					baseView.remove();
				} catch( ClearCaseException e ) {
					e.print( appender.getOut() );
					if( !tearDownAsMuchAsPossible ) {
						throw e;
					}
				}
				*/
				
				try {
					logger.info( "Removing PVob " + pvob );
					/* Unmount before remove */
					pvob.unmount();
					pvob.remove();
				} catch( ClearCaseException e ) {
					tearDownSuccess = false;
					e.log();
					if( !tearDownAsMuchAsPossible ) {
						throw e;
					}
				}
				
				/**/
				logger.info( "Removing " + viewpath );
				FileUtils.deleteDirectory( viewpath );
				
			} catch( ClearCaseException e ) {
				tearDownSuccess = false;
				logger.fatal( "Failed to tear down ClearCase" );
				e.print( System.err );
			} catch( Exception e ) {
				tearDownSuccess = false;
				logger.error( "Failed to tear down: " + e.getMessage() );
			} finally {
				logger.debug( "Clearing views" );
				UCMView.getViews().clear();
			}
			
		}
		
		return tearDownSuccess;		
	}

	
}
