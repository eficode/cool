package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyCleartool;

public abstract class Cool {
	/* Make sure, that we're using the same instance of the context! */
	public static UCMContext context = null;

	public enum ContextType {
		XML, CLEARTOOL
	}

	public static void setContext( ContextType ct ) {
		if( context != null ) {
			return;
		}

		switch ( ct ) {
		case XML:
			// context = new UCMContext( new UCMStrategyXML() );
			break;

		default:
			context = new UCMContext( new UCMStrategyCleartool() );
		}
	}

	protected static final String filesep = System.getProperty( "file.separator" );
	protected static final String qfs = filesep.equals( "\\" ) ? "\\\\" : filesep;
	protected static final String linesep = System.getProperty( "line.separator" );
	public static final String delim = "::";

	private static final int HashMap = 0;

	private static boolean verbose = false;

	public static void setVerbose( boolean verbose ) {
		Cool.verbose = verbose;
	}

	static {
		String v = System.getenv( "verbose" );
		if( v != null ) {
			Cool.verbose = true;
		}
	}

	public static boolean isVerbose() {
		return verbose;
	}

	// private static HashMap<Integer,List<String>> messages = new
	// java.util.HashMap<Integer, List<String>>();
	private static List<String> messages = new ArrayList<String>();

	public static void addMessage( String msg ) {
		messages.add( msg );
	}

	public static List<String> getMessages() {
		return messages;
	}

	public static String getMessagesAsString() {
		StringBuffer sb = new StringBuffer();

		for( String s : messages ) {
			sb.append( s );
		}

		return sb.toString();
	}
	
	@Deprecated
	public static void setLogger( Object o ) {
		
	}
}
