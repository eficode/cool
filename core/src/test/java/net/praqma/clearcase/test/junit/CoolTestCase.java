package net.praqma.clearcase.test.junit;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
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
	}
	
	public void bootStrap() throws Exception {
		bootStrap( defaultSetup );
	}
	
	public void bootStrap( File file ) throws Exception {
		logger.info( "Bootstrapping from " + file + ( file.exists() ? "" : ", which does not exist!?") );
		EnvironmentParser parser = new EnvironmentParser( file );
		Context context = parser.parse();
		System.out.println( "CONTEXT: " + context.pvobs );
		if( context.pvobs.size() > 0 ) {
			pvob = context.pvobs.get( 0 );
		} else {
			failed = true;
		}
	}
	
	public PVob getPVob() {
		return pvob;
	}
	
	public boolean hasFailed() {
		return failed;
	}

	@Override
	public void setUp() {

	}

	@Override
	public void runTest() throws Throwable {
		super.runTest();
	}

	@Override
	public void tearDown() {
		logger.info( "Tear down ClearCase" );

		if( pvob != null ) {
			try {
				SetupUtils.tearDown( pvob );
				logger.info( "Tear down is successful" );
			} catch( ClearCaseException e ) {
				logger.fatal( "Tear down failed" );
			}
		} else {
			logger.info( "PVob was null" );
		}
	}
}
