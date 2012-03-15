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
import net.praqma.util.execute.CommandLineException;

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

	public Deliver( Baseline baseline, Stream stream, Stream target, File context, String viewtag ) {
		this.baseline = baseline;
		this.stream = stream;
		this.target = target;
		this.context = context;
		this.viewtag = viewtag;
	}

	public boolean deliverForced( Stream stream, Stream target, File viewcontext, String viewtag ) throws DeliverException {
		return deliver( true, true, true );
	}

	public boolean deliver( boolean force, boolean complete, boolean abort ) throws DeliverException {
		logger.debug( "Delivering " + baseline + ", " + stream + ", " + target + ", " + context + ", " + viewtag );

		String bl = baseline != null ? baseline.getFullyQualifiedName() : null;
		String st = stream != null ? stream.getFullyQualifiedName() : null;
		String ta = target != null ? target.getFullyQualifiedName() : null;

		String result = ""; // strategy.deliver( bl, st, ta, context, viewtag, force, complete, abort );

		String cmd = "deliver" + ( force ? " -force" : "" ) + ( complete ? " -complete" : "" ) + ( abort ? " -abort" : "" );
		cmd += ( baseline != null ? " -baseline " + baseline : "" );
		cmd += ( stream != null ? " -stream " + stream : "" );
		cmd += ( target != null ? " -target " + target : "" );
		cmd += ( viewtag != null ? " -to " + viewtag : "" );

		try {
			result = Cleartool.run( cmd, context, true ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			logger.warning( "Could not deliver to target " + target + ": " + e.getMessage() );
			logger.warning( e );
			logger.warning( "---- ENDS HERE ----" );

			/* Determine cause */
			if( e.getMessage().replace( System.getProperty( "line.separator" ), " " ).contains( "requires child development streams to rebase to recommended baselines before performing deliver operation" ) ) {
				logger.warning( "Deliver requires rebase" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_REQUIRES_REBASE );
				throw new DeliverException( this, Type.REQUIRES_REBASE, e );
			} else if( e.getMessage().replace( System.getProperty( "line.separator" ), " " ).contains( "cleartool: Error: Unable to perform merge" ) ) {
				logger.warning( "Merge error" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.MERGE_ERROR );
				throw new DeliverException( this, Type.MERGE_ERROR, e );
			} else if( e.getMessage().replace( System.getProperty( "line.separator" ), " " ).contains( "does not allow deliver operations from streams in other" ) ) {
				logger.warning( "Interproject deliver denied" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.INTERPROJECT_DELIVER_DENIED );
				throw new DeliverException( this, Type.INTERPROJECT_DELIVER_DENIED, e );
			} else if( e.getMessage().replace( System.getProperty( "line.separator" ), " " ).contains( "which is currently involved in an active deliver or rebase operation.  The set activity of this view may not be changed until the operation has completed." ) ) {
				logger.warning( "Deliver already in progress" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_IN_PROGRESS );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			} else if( e.getMessage().contains( "active deliver or rebase operation.  The set activity of this view may not be" ) ) {
				logger.warning( "Deliver already in progress" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_IN_PROGRESS );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}

			if( e.getMessage().matches( "(?s)active deliver or rebase operation.  The set activity of this view may not be" ) ) {
				logger.warning( "Deliver already in progress" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_IN_PROGRESS );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}

			Matcher m2 = rx_checkProgress.matcher( e.getMessage() );
			if( m2.find() ) {
				logger.warning( "Deliver already in progress" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_IN_PROGRESS );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}

			/**
			 * in case there is an deliver in progres on the target stream
			 */
			if( e.getMessage().contains( "Deliver operation" ) ) {
				logger.warning( "Deliver already in progress" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.DELIVER_IN_PROGRESS );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}

			/* Match for merge errors */
			Matcher m = rx_checkMergeError.matcher( e.getMessage() );
			if( m.find() ) {
				logger.warning( "Merge error" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.MERGE_ERROR );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
			}

			/* Match for denied deliveries */
			m = rx_checkDeliverDenied.matcher( e.getMessage() );
			if( m.find() ) {
				logger.warning( "Interproject deliver denied" );
				//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage(), UCMType.INTERPROJECT_DELIVER_DENIED );
				throw new DeliverException( this, Type.INTERPROJECT_DELIVER_DENIED, e );
			}

			/* If nothing applies.... */
			//throw new UCMException( "Could not deliver: " + e.getMessage(), e.getMessage() );
			throw new DeliverException( this, Type.UNKNOWN, e );
		}

		// System.out.println( "I GOT: \n\"" + result + "\"\n" );

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
			//throw new UCMException( "Could not cancel deliver: " + e.getMessage(), e.getMessage() );
			throw new CancelDeliverException( stream, e );
		}
	}
	
	public String getStatus() throws CleartoolException {
		return Deliver.getStatus( stream );
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
}
