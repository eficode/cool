package net.praqma.clearcase;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CancelDeliverException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.DeliverException;
import net.praqma.clearcase.exceptions.DeliverException.Type;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Deliver {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();

	private static final Pattern rx_deliver_find_baseline = Pattern.compile( "Baselines to be delivered:\\s*baseline:", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
	private static final Pattern rx_deliver_find_nobaseline = Pattern.compile( "Baselines to be delivered:\\s*baseline:", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );

	private Baseline baseline;
	private Stream stream;
	private Stream target;
	private File context;
	private String viewtag;
	
	public enum DeliverStatus {
		UNKOWN, NO_DELIVER_ON_STREAM, DELIVER_IN_PROGRESS;
		
		public boolean busy() {
			tracer.entering(DeliverStatus.class.getSimpleName(), "busy");
			
			boolean result = !this.equals( NO_DELIVER_ON_STREAM );
			
			tracer.exiting(DeliverStatus.class.getSimpleName(), "busy", result);
			return result;
		}
	}

	/**
	 * 
	 * @param baseline
	 * @param stream
	 * @param target
	 * @param context - View context of the target view
	 * @param viewtag - View tag of the target view
	 */
	public Deliver( Baseline baseline, Stream stream, Stream target, File context, String viewtag ) {
		tracer.entering(Deliver.class.getSimpleName(), "Deliver", new Object[]{baseline, stream, target, context, viewtag});
		this.baseline = baseline;
		this.stream = stream;
		this.target = target;
		this.context = context;
		this.viewtag = viewtag;
		tracer.exiting(Deliver.class.getSimpleName(), "Deliver");
	}
	
	
	public Deliver( Stream stream, Stream target, File context, String viewtag ) {
		tracer.entering(Deliver.class.getSimpleName(), "Deliver", new Object[]{stream, target, context, viewtag});
		this.stream = stream;
		this.target = target;
		this.context = context;
		this.viewtag = viewtag;
		tracer.exiting(Deliver.class.getSimpleName(), "Deliver");
	}

	public boolean deliverForced( Stream stream, Stream target, File viewcontext, String viewtag ) throws DeliverException, CleartoolException {
		tracer.entering(Deliver.class.getSimpleName(), "deliverForced", new Object[]{stream, target, viewcontext, viewtag});
		
		boolean result = deliver( true, true, true, false );
		
		tracer.exiting(Deliver.class.getSimpleName(), "deliverForced", result);
		
		return result;
	}
	
	public boolean deliver( boolean force, boolean complete, boolean abort, boolean resume ) throws DeliverException, CleartoolException {
		tracer.entering(Deliver.class.getSimpleName(), "deliver", new Object[]{force, complete, abort, resume});
		tracer.finest("Attempting to deliver...");
		
		logger.debug( "Delivering " + baseline + ", " + stream + ", " + target + ", " + context + ", " + viewtag );
		
		boolean result;
		try {
			result = _deliver( force, complete, abort, resume );
		} catch( DeliverException e ) {
			tracer.finest("Caught exception, could not deliver.");
			tracer.finest("Checking if the exception was caught due to it being in progress.");
			if( e.getType().equals( Type.DELIVER_IN_PROGRESS ) ) { //could be a posted delivery
				tracer.finest("Is a delivery in progress.");
				String status = getStatus( stream );
				
				tracer.finest("Checking status to see if delivery has been posted.");
				if( status.replace( System.getProperty( "line.separator" ), " " ).contains( "Operation posted from" ) ) {
					tracer.finest("Delivery has been posted.");
					logger.debug( "Posted delivery" );
					tracer.finest("Attempting to resume delivery...");
					try {
						result = _deliver( force, complete, abort, true );
					} catch( DeliverException e1 ) {
						tracer.finest("Could not resume posted delivery");
						logger.warning( "Could not resume posted delivery: " + e1.getMessage() );
						tracer.severe(String.format("Exception thrown type: %s; message: %s", e1.getClass(), e1.getMessage()));
						throw e1;
					}
				} else {
					tracer.finest("Delivery has not been posted.");
					tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
					throw e;
				}
			} else {
				tracer.finest("Deliver is not in progress.");
				tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
				/* If not a deliver in progress, throw e again */
				throw e;
			}
		}
		
		tracer.finest("Successfully delivered.");
		tracer.exiting(Deliver.class.getSimpleName(), "deliver", result);
		return result;
	}
	
	public Deliver complete() throws DeliverException {
		tracer.entering(Deliver.class.getSimpleName(), "complete");
		
		String cmd = "deliver -complete";
		//cmd += ( baseline != null ? " -baseline " + baseline : "" );
		cmd += ( stream != null ? " -stream " + stream : "" );
		//cmd += ( target != null ? " -target " + target : "" );
		cmd += ( viewtag != null ? " -to " + viewtag : "" );
		cmd += " -f";
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd, context );
		} catch( Exception e ) {
			DeliverException exception = new DeliverException( this, Type.UNABLE_TO_COMPLETE, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Deliver.class.getSimpleName(), "complete", this);
		
		return this;
	}
	
	private static final Pattern rx_rebase_in_progress = Pattern.compile( "which is currently involved in an\\s+active deliver or rebase operation", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );

	private boolean _deliver( boolean force, boolean complete, boolean abort, boolean resume ) throws DeliverException {
		tracer.entering(Deliver.class.getSimpleName(), "_deliver", new Object[]{force, complete, abort, resume});
		String result = "";

		String cmd = "deliver" + ( force ? " -force" : "" ) + ( complete ? " -complete" : "" ) + ( abort ? " -abort" : "" ) + ( resume ? " -resume" : "" );

		if( !resume ) {
			cmd += ( baseline != null ? " -baseline " + baseline : "" );
		}
		cmd += ( stream != null ? " -stream " + stream : "" );
		if( !resume ) {
			cmd += ( target != null ? " -target " + target : "" );
		}
		cmd += ( viewtag != null ? " -to " + viewtag : "" );

		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			result = Cleartool.run( cmd, context, true ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			tracer.finest("Could not run Cleartool command, determining reason...");
			
			logger.warning( "Could not deliver to target " + target + ": " );
			logger.warning( e );			
			
			/* Deliver being cancelled - Untested functionality, but must be tested for first */
			if( e.getMessage().matches( "(?i)(?m)(?s)^.*Operation is currently being canceled.*$" ) ) {
				logger.warning( "(0)Deliver is being cancelled" );
				DeliverException exception = new DeliverException( this, Type.CANCELLING, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* Deliver already in progress */
			if( e.getMessage().contains( "Error: Deliver operation already in progress on stream" ) ) {
				logger.warning( "(1)Deliver already in progress" );
				DeliverException exception =  new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* Rebase in progress */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*Unable to start a deliver operation while a rebase operation is in progress on.*$" ) ) {
				logger.warning( "(2)Rebase in progress" );
				DeliverException exception =  new DeliverException( this, Type.REBASE_IN_PROGRESS, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* Deliver requires rebase */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*requires child development.*streams to rebase to recommended baselines before performing deliver.*$" ) ) {
				logger.warning( "(3)Deliver requires rebase" );
				DeliverException exception =  new DeliverException( this, Type.REQUIRES_REBASE, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* No interproject deliveries */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*does not allow deliver operations from streams in other.*$" ) ) {
				logger.warning( "(4)Interproject deliver denied" );
				DeliverException exception =  new DeliverException( this, Type.INTERPROJECT_DELIVER_DENIED, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* Merge error */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*Unable to perform merge.*Unable to do integration.*Unable to deliver stream.*$" ) ) {
				logger.warning( "(5)Merge error" );
				DeliverException exception =  new DeliverException( this, Type.MERGE_ERROR, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* Deliver in progress */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*which is currently involved in an.*active deliver or rebase operation.*The set activity of this view may not be.*changed until the operation has completed.*$" ) ) {
				logger.warning( "(6)Deliver already in progress" );
				DeliverException exception =  new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			/* If nothing applies.... */
			else {
				DeliverException exception =  new DeliverException( this, Type.UNKNOWN, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Checking if baseline is null");
		
		boolean res = true;
		Matcher m;
		/* Test for baseline == true */
		if( baseline == null ) {
			tracer.finest("baseline is null");
			m = rx_deliver_find_nobaseline.matcher( result );
		} else {
			tracer.finest("baseline is not null");
			m = rx_deliver_find_baseline.matcher( result );
		}
		tracer.finest("Checking Cleartool output to see if delivery failed");
		if( !m.find() ) {
			tracer.finest("Delivery failed.");
			res = false;
		}

		tracer.exiting(Deliver.class.getSimpleName(), "_deliver", res);
		return res;
	}
	
	public static void rollBack( String oldViewTag, Stream oldSourceStream, File context ) throws CleartoolException {

		String cmd = "deliver -cancel -force -stream " + oldSourceStream;

		try {
			if( context.exists() ) {
				Cleartool.run( cmd, context );
			} else {
				SnapshotView.regenerateViewDotDat( context, oldViewTag );
				CmdResult res = Cleartool.run( cmd, context );
			}
		} catch( Exception e ) {
			throw new CleartoolException( "Could not regenerate view to force deliver: " + oldViewTag, e );
		} finally {
			if( context.exists() && oldViewTag != null ) {
				deleteDir( context );
			}
		}
	}
	
	private static boolean deleteDir( File dir ) {
		if( dir.isDirectory() ) {
			String[] children = dir.list();
			for( int i = 0; i < children.length; i++ ) {
				boolean success = deleteDir( new File( dir, children[i] ) );
				if( !success ) {
					return false;
				}
			}
		}
		
		return dir.delete();
	}
	
	public void cancel() throws CancelDeliverException {
		cancel( stream, context );
	}
	
	public static void cancel( Stream stream, File context ) throws CancelDeliverException {
		try {
			String cmd = "deliver -cancel -force" + ( stream != null ? " -stream " + stream : "" );
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CancelDeliverException( stream, e );
		}
	}
	
	public String getStatus() throws CleartoolException {
		return Deliver.getStatus( stream );
	}
	
	public DeliverStatus getDeliverStatus() throws CleartoolException {
		return stringToStatus( Deliver.getStatus( stream ) );
	}
	
	public static String getStatus( Stream stream ) throws CleartoolException {
		try {
			String cmd = "deliver -status -stream " + stream;
			return Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Could not get deliver status: " + e.getMessage(), e );
		}
	}
	
	public boolean isDelivering() throws CleartoolException {
		return Deliver.isDelivering( stream );
	}
	
	public static boolean isDelivering( Stream stream ) throws CleartoolException {
		String r = Deliver.getStatus( stream ).trim();

		if( r.startsWith( "No deliver operation in progress on stream" ) ) {
			return false;
		}

		return true;
	}
	
	public static DeliverStatus stringToStatus( String string ) {
		if( string.contains( "No deliver operation in progress on stream" ) ) {
			return DeliverStatus.NO_DELIVER_ON_STREAM;
		} else if( string.contains( "Deliver operation in progress on stream" ) ) {
			return DeliverStatus.DELIVER_IN_PROGRESS;
		} else {
			return DeliverStatus.UNKOWN;
		}
	}
	
	public File getViewContext() {
		return this.context;
	}
	
	public String getViewtag() {
		return this.viewtag;
	}
}
