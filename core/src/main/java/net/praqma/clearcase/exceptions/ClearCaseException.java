package net.praqma.clearcase.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.praqma.util.execute.AbnormalProcessTerminationException;

public class ClearCaseException extends Exception {
	
	
	private static Logger logger = Logger.getLogger( ClearCaseException.class.getName() );

	protected List<String> information = new ArrayList<String>();

	public ClearCaseException() {
		super();
	}

	public ClearCaseException( String s ) {
		super( s );
	}

	public ClearCaseException( Exception e ) {
		super( e );
	}

	public ClearCaseException( String s, Exception e ) {
		super( s, e );
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
	
	public void printInformation( PrintWriter stream ) {
		for( String info : information ) {
			stream.println( info );
		}
	}

	public void print( PrintStream out ) {
		out.println( this.getMessage() );
		this.printInformation( out );
		if( this.getCause() != null ) {
			if( this.getCause() instanceof AbnormalProcessTerminationException ) {
				out.println( "Command was: " + ( (AbnormalProcessTerminationException) this.getCause() ).getCommand() );
			}
			out.println( this.getCause().getMessage() );
		}
	}
	
	public void print( PrintWriter out ) {
		out.println( this.getMessage() );
		this.printInformation( out );
		if( this.getCause() != null ) {
			if( this.getCause() instanceof AbnormalProcessTerminationException ) {
				out.println( "Command was: " + ( (AbnormalProcessTerminationException) this.getCause() ).getCommand() );
			}
			out.println( this.getCause().getMessage() );
		}
	}
	
	public void log() {
		logger.warning( this.getMessage() );
		for( String info : information ) {
			logger.warning( info );
		}
		if( this.getCause() != null ) {
			if( this.getCause() instanceof AbnormalProcessTerminationException ) {
				logger.warning( "Command was: " + ( (AbnormalProcessTerminationException) this.getCause() ).getCommand() );
			}
			logger.warning( this.getCause().getMessage() );
		}
	}

}