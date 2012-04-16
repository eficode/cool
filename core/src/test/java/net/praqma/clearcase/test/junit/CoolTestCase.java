package net.praqma.clearcase.test.junit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
	
	/**
	 * This map is used to overwrite those variables detected by the environment parser.<br><br>
	 * The most common variables to overwrite are <b>pvobname</b> and <b>vobname</b>.
	 */
	public Map<String, String> variables = new HashMap<String, String>();
	
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
		Context context = parser.parse( variables );
		logger.info( "CONTEXT PVOBS: " + context.pvobs );
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
		/* No need for tear down, this is done prior to setup */
	}
}
