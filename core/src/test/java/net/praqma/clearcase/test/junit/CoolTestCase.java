package net.praqma.clearcase.test.junit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Version;
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
	
	/**
	 * This variable is null until bootStrap is called.
	 */
	public Context context;

	protected static boolean rolling = true;
	protected static boolean tearDownAsMuchAsPossible = true;
	protected boolean failed = false;
	
	protected File defaultSetup = new File( CoolTestCase.class.getClassLoader().getResource( "setup.xml" ).getFile() );
	
	public String uniqueTimeStamp = "" + System.currentTimeMillis() / 60000;
	
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
		context = parser.parse( variables );
		logger.info( "CONTEXT PVOBS: " + context.pvobs );
		if( context.pvobs.size() > 0 ) {
			pvob = context.pvobs.get( 0 );
			ClearCase.createSimpleAttributeType( "test-vob", pvob, true );
			/* Set a test attribute */
			pvob.setAttribute( "test-vob", "initial", true );
		} else {
			failed = true;
		}
	}
	
	public void addNewContent( Component component, File viewpath, String filename ) throws ClearCaseException {
		Version.checkOut( new File( component.getShortname() ), viewpath );
		File file = new File( new File( viewpath, component.getShortname() ), filename );
		
		if( !file.exists() ) {
			try {
				file.createNewFile();
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			}
		}
		
		Version.addToSourceControl( file, file.isDirectory(), viewpath );
		writeContent( file, "blaha" );
		Version.recursiveCheckin( viewpath );
	}
	
	public void writeContent( File file, String content ) throws ClearCaseException {
		FileWriter fw = null;
		try {
			fw = new FileWriter( file, true );
			fw.write( content );
		} catch( IOException e1 ) {
			throw new ClearCaseException( e1 );
		} finally {
			try {
				fw.close();
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			}
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
//		if( pvob != null ) {
//			try {
//				pvob.setAttribute( "test-vob", "done", true );
//			} catch( UnableToSetAttributeException e ) {
//				logger.error( "Unable to set attribute: " + e.getMessage() );
//				e.log();
//			}
//		}
	}
}
