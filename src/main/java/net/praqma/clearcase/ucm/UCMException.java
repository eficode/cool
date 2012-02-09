package net.praqma.clearcase.ucm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
public class UCMException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5325867242379727760L;
	transient private static Logger logger = Logger.getLogger();
	public UCMType type = UCMType.DEFAULT;

	public String stdout = null;
	
	private List<String> information = new ArrayList<String>();

	public enum UCMType {
		DEFAULT,
		ENTITY_ERROR,
		ENTITY_NAME_ERROR,
		EXISTENCE,
		HLINK_ZERO_MATCHES,
		LOAD_FAILED,
		TAG_CREATION_FAILED,
		UNKNOWN_HLINK_TYPE,
		VIEW_ERROR,
		CREATION_FAILED,
		NOTHING_CHANGED,
		DELIVER_REQUIRES_REBASE,
		INTERPROJECT_DELIVER_DENIED,
		MERGE_ERROR,
		DELIVER_IN_PROGRESS,
		VIEW_CURRENTLY_REBASING,
		UNKOWN_VOB,
		UNKNOWN_USER,
		ENTITY_NOT_FOUND
	}

	public UCMException() {
		super();
	}

	public UCMException( String s ) {
		super( s );
	}
	
	public UCMException( Exception e ) {
		super( e );
	}
	
	public UCMException( String s, Exception e ) {
		super( s, e );
	}

	/**
	 * @deprecated since 0.3.39
	 * @param s
	 * @param stdout
	 */
	public UCMException( String s, String stdout ) {
		super( s );
		this.stdout = stdout;
	}

	public UCMException( String s, UCMType type ) {
		super( s );

		this.type = type;
	}
	
	public UCMException( Exception e, UCMType type ) {
		super( e );

		this.type = type;
	}
	
	public UCMException( String s, Exception e, UCMType type ) {
		super( s, e );

		this.type = type;
	}

	/**
	 * @deprecated since 0.3.39
	 * @param s
	 * @param stdout
	 * @param type
	 */
	public UCMException( String s, String stdout, UCMType type ) {
		super( s );

		this.type = type;
		this.stdout = stdout;
	}
	
	public void addInformation( String info ) {
		information.add( info );
	}
	
	public List<String> getInformation() {
		return information;
	}
	
	public String getInformationAsString() {
		StringBuffer sb = new StringBuffer();
		for( String info : information ) {
			sb.append( info );
			sb.append( System.getProperty( "line.separator" ) );
		}
		
		return sb.toString();
	}
	
	public void printInformation( PrintStream stream ) {
		for( String info : information ) {
			stream.println( info );
		}
	}

}