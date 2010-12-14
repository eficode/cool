package net.praqma.clearcase.ucm.entities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyCleartool;
import net.praqma.clearcase.ucm.persistence.UCMStrategyXML;
import net.praqma.utils.Debug;

public abstract class UCM
{
	static
	{
		System.out.println( "Setting configuration" );
		//UCM.class.getClassLoader().getResourceAsStream( "log4j.conf" );
		

		Properties props = new Properties();
		try
		{
			props.load( UCM.class.getClassLoader().getResourceAsStream( "log4j.xml" ) );
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PropertyConfigurator.configure( props );
		//PropertyConfigurator.configure( "/log4j.conf" );
	}
	
	/* Make sure, that we're using the same instance of the context! */
	public static UCMContext context = null;
	
	public enum ContextType
	{
		XML,
		CLEARTOOL
	}
	
	public static void SetContext( ContextType ct )
	{
		if( context != null )
		{
			logger.warning( "Context is already set" );
			return;
		}
		
		logger.log( "Setting context type to " + ct.toString() );
		
		switch( ct )
		{
		case XML:
			context = new UCMContext( new UCMStrategyXML() );
			break;
			
		default:
			context = new UCMContext( new UCMStrategyCleartool() );
		}
	}
	
	protected static Debug logger = Debug.GetLogger( false );
	
	protected static final String filesep = System.getProperty( "file.separator" );
	protected static final String linesep = System.getProperty( "line.separator" );
	public static final String delim      = "::";
}