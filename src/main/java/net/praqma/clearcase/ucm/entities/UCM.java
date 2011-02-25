package net.praqma.clearcase.ucm.entities;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyCleartool;
import net.praqma.util.debug.Logger;

public abstract class UCM
{

	
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
			//context = new UCMContext( new UCMStrategyXML() );
			break;
			
		default:
			context = new UCMContext( new UCMStrategyCleartool() );
		}
	}
	
	protected static Logger logger = Logger.getLogger( false );
	
	protected static final String filesep = System.getProperty( "file.separator" );
	protected static final String linesep = System.getProperty( "line.separator" );
	public static final String delim      = "::";

	private static final int HashMap = 0;
	
	private static boolean verbose = false;
	
	public static void setVerbose( boolean verbose )
	{
		UCM.verbose = verbose;
	}
	
	static 
	{
		String v = System.getenv( "verbose" );
		if( v != null )
		{
			UCM.verbose = true;
		}
	}
	
	public static boolean isVerbose()
	{
		return verbose;
	}
	
	//private static HashMap<Integer,List<String>> messages = new java.util.HashMap<Integer, List<String>>();
	private static List<String> messages = new ArrayList<String>();
	
	
	public static void addMessage( String msg )
	{
		messages.add( msg );
	}
	
	public static List<String> getMessages()
	{
		return messages;
	}
	
	public static String getMessagesAsString()
	{
		StringBuffer sb = new StringBuffer();
		
		for( String s : messages )
		{
			sb.append( s );
		}
		
		return sb.toString();
	}
}