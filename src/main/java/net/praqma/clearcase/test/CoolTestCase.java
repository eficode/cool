package net.praqma.clearcase.test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.annotations.TestConfiguration;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToListProjectsException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

import junit.framework.TestCase;

public abstract class CoolTestCase extends TestCase {

	protected static Logger logger = Logger.getLogger();
	protected static ConsoleAppender appender = new ConsoleAppender();

	protected static boolean rolling = true;
	protected static boolean tearDownAsMuchAsPossible = true;

	static {
		System.out.println( "STATIC" );
		appender.setTemplate( "[%level]%space %message%newline" );
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}

	protected PVob pvob;
	protected boolean removePvob = false;
	protected boolean fail = false;

	protected String dynamicView = "TestDynamicView";
	private DynamicView baseView;

	public DynamicView getBaseView() {
		return baseView;
	}

	@Override
	protected void setUp() {
		logger.debug( "Setup ClearCase" );

		TestConfiguration config = getClass().getAnnotation( TestConfiguration.class );
		String project = config.project();
		String pvob = Cool.filesep + config.pvob();

		removePvob = false;
		PVob pv = PVob.get( pvob );
		if( pv == null ) {
			logger.info( "Creating " + pvob );
			try {
				logger.verbose( "Creating pvob " + pvob );
				this.pvob = (PVob) PVob.create( pvob, true, null, "testing" );
				logger.verbose( "Creating dynamic view" );
				baseView = DynamicView.create( null, dynamicView, null );
				logger.verbose( "Starting view" );
				new DynamicView( null, dynamicView ).startView();
				removePvob = true;
			} catch( ClearCaseException e ) {
				e.print( System.err );
				fail = true;
			}
		} else {
			logger.fatal( "The PVob " + pvob + " already exists" );
			fail = true;
		}
	}

	@Override
	protected void runTest() throws Throwable {
		logger.info( "RUN TEST" );
		if( !fail ) {
			super.runTest();
		} else {
			logger.fatal( "ClearCase not set up, unable to run test" );
			throw new Exception( "ClearCase not set up, unable to run test" );
		}
	}

	@Override
	public void runBare() throws Throwable {
		logger.info( "BEFORE BARE" );
		Thread t = Thread.currentThread();
		String o = getClass().getName() + '.' + t.getName();
		t.setName( "Executing " + getName() );
		try {
			super.runBare();
		} finally {
			t.setName( o );
		}
		logger.info( "AFTER BARE" );
	}

	@Override
	protected void tearDown() {
		logger.info( "Tear down ClearCase" );

		if( removePvob ) {
			try {
				/* Removing views */
				Set<String> viewTags = UCMView.getViews().keySet();
				for( String viewTag : viewTags ) {
					try {
						UCMView.getViews().get( viewTag ).remove();
					} catch( ClearCaseException e ) {
						e.print( appender.getOut() );
						if( !tearDownAsMuchAsPossible ) {
							throw e;
						}
					}
				}
				
				/* Removing baseview */
				logger.verbose( "Removing base view" );
				try {
					baseView.remove();
				} catch( ClearCaseException e ) {
					e.print( appender.getOut() );
					if( !tearDownAsMuchAsPossible ) {
						throw e;
					}
				}
				
				try {
					logger.info( "Removing PVob " + pvob );
					pvob.remove();
				} catch( ClearCaseException e ) {
					e.print( appender.getOut() );
					if( !tearDownAsMuchAsPossible ) {
						throw e;
					}
				}
			} catch( ClearCaseException e ) {
				logger.fatal( "Unable to tear down ClearCase" );
				e.print( System.err );
			}
		}
	}
}
