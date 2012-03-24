package net.praqma.clearcase.test.junit;

import net.praqma.clearcase.ucm.view.DynamicView;
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
	
	protected Bootstrap bootstrap;
	
	public CoolTestCase() {
		logger.verbose( "Constructor" );
	}

	public DynamicView getBaseView() {
		return bootstrap.baseView;
	}
	
	public boolean bootStrap( String projectName, String integrationName ) throws Exception {
		return bootstrap.bootStrap( projectName, integrationName );
	}

	@Override
	public void setUp() {
		logger.debug( "Setup ClearCase" );

		bootstrap = new Bootstrap();
		bootstrap.setUp();
	}

	@Override
	public void runTest() throws Throwable {
		if( !bootstrap.fail ) {
			super.runTest();
		} else {
			logger.fatal( "ClearCase not set up, unable to run test" );
			throw new Exception( "ClearCase not set up, unable to run test" );
		}
	}
	
	public boolean hasFailed() {
		return bootstrap.fail;
	}

	@Override
	public void tearDown() {
		logger.info( "Tear down ClearCase" );
		boolean tearDownSuccess = bootstrap.tearDown();

		
		if( tearDownSuccess ) {
			logger.info( "Tear down is successful" );
		} else {
			logger.fatal( "Tear down failed" );
		}
		
		bootstrap = null;
	}
}
