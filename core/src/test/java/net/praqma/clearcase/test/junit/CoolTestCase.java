package net.praqma.clearcase.test.junit;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.ConsoleAppender;

import junit.framework.TestCase;

public abstract class CoolTestCase extends TestCase {

	protected static Logger logger = Logger.getLogger();
	protected static ConsoleAppender appender = new ConsoleAppender();

	protected static boolean rolling = true;
	protected static boolean tearDownAsMuchAsPossible = true;
	protected boolean failed = false;
	
	protected File defaultSetup = new File( CoolTestCase.class.getClassLoader().getResource( "setup.xml" ).getFile() );
	
	protected PVob pvob;
	
	protected File viewPath;

	static {
		appender.setTemplate( "[%level]%space %message%newline" );
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}
	
	public CoolTestCase() {
		logger.verbose( "Constructor" );
		viewPath = new File( System.getProperty( "viewpath", "views" ) );
		try {
			this.pvob = PVob.create( Cool.filesep + System.getProperty( "pvob", "TESTING_PVOB" ), null, "Testing PVOB" );
		} catch( CleartoolException e ) {
			logger.fatal( "Unable to create PVOB!" );
			failed = true;
		}
	}
	
	public void bootStrap( File file ) throws Exception {
		EnvironmentParser parser = new EnvironmentParser( file );
		parser.parse();
	}
	
	public boolean hasFailed() {
		return failed;
	}

	@Override
	public void setUp() {

	}

	@Override
	public void runTest() throws Throwable {

	}

	@Override
	public void tearDown() {
		logger.info( "Tear down ClearCase" );

		try {
			SetupUtils.tearDown( pvob );
			logger.info( "Tear down is successful" );
		} catch( ClearCaseException e ) {
			logger.fatal( "Tear down failed" );
		}
	}
}
