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
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.ConsoleAppender;

import junit.framework.TestCase;

public abstract class CoolTestCase extends TestCase {

	protected static Logger logger = Logger.getLogger();
	protected static ConsoleAppender appender = new ConsoleAppender();

	protected static boolean rolling = true;
	protected static boolean tearDownAsMuchAsPossible = true;

	static {
		appender.setTemplate( "[%level]%space %message%newline" );
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}

	protected PVob pvob;
	private String pvobStr;
	protected boolean removePvob = false;
	protected boolean fail = false;

	protected File prefix;

	protected String dynamicViewTag = "TestDynamicView";
	protected String bootstrapViewTag = "";
	
	protected DynamicView baseView;
	protected DynamicView bootstrapView;
	private String basepathStr;
	protected File basepath;
	protected File viewpath;
	protected File bootstrappath;
	
	protected Project project;
	protected Stream integrationStream;
	
	protected Component systemComponent;
	protected Component modelComponent;
	protected Component clientComponent;
	
	protected Baseline structure;
	
	public CoolTestCase() {
		logger.verbose( "Constructor" );
		
		/* Check options */
		pvobStr = System.getProperty( "pvob", "TESTING_PVOB" );
		basepathStr = System.getProperty( "path", "" );
		prefix = new File( System.getProperty( "path", "m:/" ) );
		viewpath = new File( System.getProperty( "viewpath", "views" ) );
		dynamicViewTag = System.getProperty( "viewtag", "TestDynamicView" );
		bootstrapViewTag = System.getProperty( "bootstrapviewtag", "TestBootstrapView" );
	}

	public DynamicView getBaseView() {
		return baseView;
	}
	
	public boolean bootStrap( String projectName, String integrationName ) {
		try {
			/* Unrooted component */
			systemComponent = Component.create( "_System", pvob, null, "Unrooted system component", basepath );
			
			/* Rooted components */
			modelComponent = Component.create( "Model", pvob, "Model", "Model component", basepath );
			clientComponent = Component.create( "Client", pvob, "Client", "Client component", basepath );
			
			project = Project.create( projectName, null, pvob, Project.POLICY_INTERPROJECT_DELIVER, "Test", modelComponent, clientComponent );
			
			/**/
			Baseline SystemINITIAL = Baseline.get( "_System_INITIAL", pvob, true );
			Baseline ModelINITIAL = Baseline.get( "Model_INITIAL", pvob, true );
			Baseline ClientINITIAL = Baseline.get( "Client_INITIAL", pvob, true );
			integrationStream = Stream.createIntegration( integrationName, project, SystemINITIAL, ModelINITIAL, ClientINITIAL );
			
			/**/
			bootstrapView = DynamicView.create( null, bootstrapViewTag, integrationStream );
			bootstrappath = new File( prefix, bootstrapViewTag + "/" + this.pvob.getName() );
			
			structure = Baseline.create( "Structure", systemComponent, bootstrappath, LabelBehaviour.DEFAULT, false, null, new Component[] { modelComponent, clientComponent } );
			
			return true;
		} catch( ClearCaseException e ) {
			e.print( appender.getOut() );
			return false;
		} catch( Exception e ) {
			logger.error( "Error: " + e.getMessage() );
			return false;
		}
	}

	@Override
	public void setUp() {
		logger.debug( "Setup ClearCase" );

		String pvob = Cool.filesep + pvobStr;

		removePvob = false;
		PVob pv = PVob.get( pvob );
		if( pv == null ) {
			logger.info( "Creating " + pvob );
			try {
				logger.verbose( "Creating pvob " + pvob );
				this.pvob = PVob.create( pvob, null, "testing" );
				this.pvob.mount();
				logger.verbose( "Creating dynamic view" );
				baseView = DynamicView.create( null, dynamicViewTag, null );
				logger.verbose( "Starting view" );
				new DynamicView( null, dynamicViewTag ).startView();
				removePvob = true;
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
	}

	@Override
	public void runTest() throws Throwable {
		if( !fail ) {
			super.runTest();
		} else {
			logger.fatal( "ClearCase not set up, unable to run test" );
			throw new Exception( "ClearCase not set up, unable to run test" );
		}
	}
	
	public boolean hasFailed() {
		return fail;
	}

	@Override
	public void tearDown() {
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
						e.print( appender.getOut() );
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
					e.print( appender.getOut() );
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
		
		if( tearDownSuccess ) {
			logger.info( "Tear down is successful" );
		} else {
			logger.fatal( "Tear down failed" );
		}
	}
}
