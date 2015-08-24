package net.praqma.clearcase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

public class Environment {

    private static Logger logger = Logger.getLogger( Environment.class.getName() );

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
	
	private static PVob pvob;
	
	protected File viewPath;

	public Environment() {
		viewPath = new File( System.getProperty( "viewpath", "views" ) );
	}
	
	public static long getUniqueTimestamp() {
		return System.currentTimeMillis() / 1000;
	}

	public void bootStrap() throws Exception {
		bootStrap( defaultSetup );
	}
	
	public void bootStrap( File file ) throws Exception {
		logger.info( "Bootstrapping from " + file + ( file.exists() ? "" : ", which does not exist!?" ) );
		try {
			EnvironmentParser parser = new EnvironmentParser( file );
			context = parser.parse( variables );
            logger.fine( context.toString() );
			logger.info( "CONTEXT PVOBS: " + context.pvobs );
			if( context.pvobs.size() > 0 ) {

				/* There should be only one pvob defined, get it */
				for( String key : context.pvobs.keySet() ) {
					pvob = context.pvobs.get( key );
					break;
				}

				ClearCase.createSimpleAttributeType( "test-vob", pvob, true );
				/* Set a test attribute */
				pvob.setAttribute( "test-vob", "initial", true );
			} else {
				throw new ClearCaseException( "No PVob available" );
			}
		} catch( Exception ex ) {
			// this. and classname not callable
			logger.info( "net.praqma.clearcase.test.junit.CoolTestCase.java:" + " caught exception: " + ex );
			throw ex;
		}
	}
	
	public void addNewContent( Component component, File viewpath, String filename ) throws ClearCaseException {
		Version.checkOut( new File( component.getShortname() ), viewpath );
		File file = new File( new File( viewpath, component.getShortname() ), filename );		
		writeContent( file, "blaha" );		
		Version.addToSourceControl( file, viewpath, null, true );
	}

    public void addNewContent( File file, File path ) throws ClearCaseException {
        Version.checkOut( file, path );
        writeContent( file, "content #" + System.currentTimeMillis() );
        Version.checkIn( file, false, path );
    }

	public void addNewElement( Component component, File viewpath, String filename ) throws ClearCaseException {
        File compFile = new File( viewpath, component.getShortname() );
		File file = new File( compFile, filename );
		
		logger.fine( "FILE IS " + viewpath );
		logger.fine( "FILE IS " + component );
		logger.fine( "FILE IS " + filename );
		logger.fine( "FILE IS " + file );
		
        //If this is a  new element in source control. Add it.
		if( !file.exists() ) {
			try {
				file.createNewFile();
                writeContent( file, "blaha" );
                Version.addToSourceControl( file, viewpath, null, true );
			} catch( IOException e1 ) {
				throw new ClearCaseException( e1 );
			}
		} else {
            //Add new content to the old file
            addNewContent(file, viewpath);
        }	
	}
	
	public void writeContent( File file, String content ) throws ClearCaseException {
		FileWriter fw = null;
		try {
			fw = new FileWriter( file, true );
			fw.write( content );
		} catch( IOException e1 ) {
            logger.log( Level.WARNING, "Failed to write", e1 );
			throw new ClearCaseException( e1 );
		} finally {
            if( fw != null ) {
                try {
                    fw.close();
                } catch( IOException e1 ) {
                    throw new ClearCaseException( e1 );
                }
            }
		}
	}
	
	public PVob getPVob() {
		return pvob;
	}
	


}
