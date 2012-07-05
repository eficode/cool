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
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Deliver {
	private static Logger logger = Logger.getLogger();

	private static final Pattern rx_checkMergeError = Pattern.compile( "An error occurred while merging file elements in the target view.*?Unable to perform merge", Pattern.DOTALL );
	private static final Pattern rx_checkDeliverDenied = Pattern.compile( "does not allow deliver operations from streams in other", Pattern.DOTALL );
	private static final Pattern rx_checkProgress = Pattern.compile( "which is currently involved in an.*?active deliver or rebase operation", Pattern.DOTALL );
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
			return !this.equals( NO_DELIVER_ON_STREAM );
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
		this.baseline = baseline;
		this.stream = stream;
		this.target = target;
		this.context = context;
		this.viewtag = viewtag;
	}
	
	
	public Deliver( Stream stream, Stream target, File context, String viewtag ) {
		this.stream = stream;
		this.target = target;
		this.context = context;
		this.viewtag = viewtag;
	}

	public boolean deliverForced( Stream stream, Stream target, File viewcontext, String viewtag ) throws DeliverException, CleartoolException {
		return deliver( true, true, true, false );
	}
	
	public boolean deliver( boolean force, boolean complete, boolean abort, boolean resume ) throws DeliverException, CleartoolException {
		logger.debug( "Delivering " + baseline + ", " + stream + ", " + target + ", " + context + ", " + viewtag );
		try {
			return _deliver( force, complete, abort, resume );
		} catch( DeliverException e ) {
			if( e.getType().equals( Type.DELIVER_IN_PROGRESS ) ) { //could be a posted delivery
				String status = getStatus( stream );
				if( status.replace( System.getProperty( "line.separator" ), " " ).contains( "Operation posted from" ) ) {
					logger.debug( "Posted delivery" );
					try {
						return _deliver( force, complete, abort, true );
					} catch( DeliverException e1 ) {
						logger.warning( "Could not resume posted delivery: " + e1.getMessage() );
						throw e1;
					}
				} else {
					throw e;
				}
			}

			/* If not a deliver in progress, throw e again */
			throw e;
		}
	}
	
	public Deliver complete() throws DeliverException {
		String cmd = "deliver -complete";
		//cmd += ( baseline != null ? " -baseline " + baseline : "" );
		cmd += ( stream != null ? " -stream " + stream : "" );
		//cmd += ( target != null ? " -target " + target : "" );
		cmd += ( viewtag != null ? " -to " + viewtag : "" );
		cmd += " -f";
		
		try {
			Cleartool.run( cmd, context );
			return this;
		} catch( Exception e ) {
			throw new DeliverException( this, Type.UNABLE_TO_COMPLETE, e );
		}
	}
	
	private static final Pattern rx_rebase_in_progress = Pattern.compile( "which is currently involved in an\\s+active deliver or rebase operation", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );

	private boolean _deliver( boolean force, boolean complete, boolean abort, boolean resume ) throws DeliverException {
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

		try {
			result = Cleartool.run( cmd, context, true ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			logger.warning( "Could not deliver to target " + target + ": " );
			logger.warning( e );			
			
			/* Deliver being cancelled - Untested functionality, but must be tested for first */
			if( e.getMessage().matches( "(?i)(?m)(?s)^.*Operation is currently being canceled.*$" ) ) {
				logger.warning( "(0)Deliver is being cancelled" );
				throw new DeliverException( this, Type.CANCELLING, e );
			}
			/* Deliver already in progress */
			if( e.getMessage().contains( "Error: Deliver operation already in progress on stream" ) ) {
				logger.warning( "(1)Deliver already in progress" );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}
			/* Rebase in progress */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*Unable to start a deliver operation while a rebase operation is in progress on.*$" ) ) {
				logger.warning( "(2)Rebase in progress" );
				throw new DeliverException( this, Type.REBASE_IN_PROGRESS, e );
			}
			/* Deliver requires rebase */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*requires child development.*streams to rebase to recommended baselines before performing deliver.*$" ) ) {
				logger.warning( "(3)Deliver requires rebase" );
				throw new DeliverException( this, Type.REQUIRES_REBASE, e );
			}
			/* No interproject deliveries */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*does not allow deliver operations from streams in other.*$" ) ) {
				logger.warning( "(4)Interproject deliver denied" );
				throw new DeliverException( this, Type.INTERPROJECT_DELIVER_DENIED, e );
			}
			/* Merge error */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*Unable to perform merge.*Unable to do integration.*Unable to deliver stream.*$" ) ) {
				logger.warning( "(5)Merge error" );
				throw new DeliverException( this, Type.MERGE_ERROR, e );
			}			
			/* If nothing applies.... */
			else {
				throw new DeliverException( this, Type.UNKNOWN, e );
			}
		}

		/* Test for baseline == true */
		if( baseline != null ) {
			Matcher m = rx_deliver_find_baseline.matcher( result );
			if( !m.find() ) {
				return false;
			}
		}

		if( baseline == null ) {
			Matcher m = rx_deliver_find_nobaseline.matcher( result );
			if( !m.find() ) {
				return false;
			}
		}

		return true;
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
}
