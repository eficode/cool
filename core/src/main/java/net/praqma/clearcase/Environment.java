package net.praqma.clearcase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class Environment {

	protected static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	protected static Logger logger = Logger.getLogger();
	protected static ConsoleAppender appender = new ConsoleAppender();
	
	/**
	 * This variable is null until bootStrap is called.
	 */
	public Context context;

	protected File defaultSetup = new File( Environment.class.getClassLoader().getResource( "setup.xml" ).getFile() );
	
	public String uniqueTimeStamp = "" + getUniqueTimestamp();
	
	/**
	 * This map is used to overwrite those variables detected by the environment parser.<br><br>
	 * The most common variables to overwrite are <b>pvobname</b> and <b>vobname</b>.
	 */
	public Map<String, String> variables = new HashMap<String, String>();
	
	protected static PVob pvob;
	
	protected File viewPath;

	static {
		appender.setTemplate( "[%level]%space %message%newline" );
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}
	
	public Environment() {
		tracer.entering(Environment.class.getSimpleName(), "Environment");
		viewPath = new File( System.getProperty( "viewpath", "views" ) );
		tracer.exiting(Environment.class.getSimpleName(), "Environment");
	}
	
	public static long getUniqueTimestamp() {
		tracer.entering(Environment.class.getSimpleName(), "getUniqueTimestamp");
		
		long result = System.currentTimeMillis() / 60000;
		
		tracer.exiting(Environment.class.getSimpleName(), "getUniqueTimestamp", result);
		
		return result;
	}

	public void bootStrap() throws Exception {
		tracer.entering(Environment.class.getSimpleName(), "bootStrap");
		bootStrap( defaultSetup );
		tracer.exiting(Environment.class.getSimpleName(), "bootStrap");
	}
	
	public void bootStrap( File file ) throws Exception {
		tracer.entering(Environment.class.getSimpleName(), "bootStrap", file);
		
		logger.info( "Bootstrapping from " + file + ( file.exists() ? "" : ", which does not exist!?" ) );
		
		tracer.finest("Attempting to parse file.");
		
		try {
			EnvironmentParser parser = new EnvironmentParser( file );
			context = parser.parse( variables );
			logger.info( "CONTEXT PVOBS: " + context.pvobs );
			
			tracer.finest(String.format("Checking if the parser found any PVobs: %s", context.pvobs));
			if( context.pvobs.size() > 0 ) {
				tracer.finest("Parser found at least 1 PVob (There should only be 1)");
				tracer.finest("Getting the PVob");
				
				/* There should be only one pvob defined, get it */
				for( String key : context.pvobs.keySet() ) {
					pvob = context.pvobs.get( key );
					break;
				}
				
				tracer.finest(String.format("Adding a test attribute to PVob: %s", pvob));

				ClearCase.createSimpleAttributeType( "test-vob", pvob, true );
				/* Set a test attribute */
				pvob.setAttribute( "test-vob", "initial", true );
			} else {
				ClearCaseException exception = new ClearCaseException( "No PVob available" );
				
				tracer.severe(String.format("Throwing exception type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
		} catch( Exception ex ) {
			// this. and classname not callable
			logger.info( "net.praqma.clearcase.test.junit.CoolTestCase.java:" + " caught exception: " + ex );
			tracer.severe(String.format("Exception thrown type: %s; message: %s", ex.getClass(), ex.getMessage()));
			throw ex;
		}
		tracer.exiting(Environment.class.getSimpleName(), "bootStrap");
	}
	
	public void addNewContent( Component component, File viewpath, String filename ) throws ClearCaseException {
		tracer.entering(Environment.class.getSimpleName(), "addNewContent", new Object[]{component, viewpath, filename});
		tracer.finest("Checking out latest version of file.");
		
		Version.checkOut( new File( component.getShortname() ), viewpath );
		
		tracer.finest("Opening file.");
		
		File file = new File( new File( viewpath, component.getShortname() ), filename );
		
		tracer.finest("Writing new content to file.");
		
		writeContent( file, "blaha" );
		
		tracer.finest("Adding file to source control.");
		
		Version.addToSourceControl( file, viewpath, null, true );
		
		tracer.exiting(Environment.class.getSimpleName(), "addNewContent");
	}
	
	public void addNewElement( Component component, File viewpath, String filename ) throws ClearCaseException {
		tracer.entering(Environment.class.getSimpleName(), "addNewContent", new Object[]{component, viewpath, filename});
		tracer.finest("Opening file.");
		
		File file = new File( new File( viewpath, component.getShortname() ), filename );
		
		logger.debug( "FILE IS " + viewpath );
		logger.debug( "FILE IS " + component );
		logger.debug( "FILE IS " + filename );
		logger.debug( "FILE IS " + file );
		
		tracer.finest(String.format("Checking if file %s does not exist", file.getAbsolutePath()));
		
		if( !file.exists() ) {
			tracer.finest("File does not exist.");
			tracer.finest("Attempting to create file...");
			try {
				file.createNewFile();
			} catch( IOException e1 ) {
				ClearCaseException exception = new ClearCaseException( e1 );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			tracer.finest("Successfully created file.");
		}
		tracer.finest("Writing to file");
		
		writeContent( file, "blaha" );
		
		tracer.finest("Adding file %s to source control.");
		
		Version.addToSourceControl( file, viewpath, null, true );
		
		tracer.exiting(Environment.class.getSimpleName(), "addNewElement");
	}
	
	public void writeContent( File file, String content ) throws ClearCaseException {
		tracer.entering(Environment.class.getSimpleName(), "writeContent", new Object[]{file, content});
		tracer.finest("Attempting to write content to file...");

		FileWriter fw = null;
		try {
			fw = new FileWriter( file, true );
			fw.write( content );
		} catch( IOException e1 ) {
			ClearCaseException exception = new ClearCaseException( e1 );

			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));

			throw exception;
		} finally {
			tracer.finest("Attempting to close file...");
			try {
				fw.close();
			} catch( IOException e1 ) {
				ClearCaseException exception = new ClearCaseException( e1 );

				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));

				throw exception;
			}
			tracer.finest("Successfully closed file.");
		}
		tracer.finest("Successfully wrote content to file.");
		tracer.exiting(Environment.class.getSimpleName(), "writeContent");
	}
	
	public PVob getPVob() {
		tracer.entering(Environment.class.getSimpleName(), "getPVob");
		tracer.exiting(Environment.class.getSimpleName(), "getPVob", pvob);
		return pvob;
	}
	


}
