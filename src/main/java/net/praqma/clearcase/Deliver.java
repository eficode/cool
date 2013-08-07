package net.praqma.clearcase;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.exceptions.DeliverException.Type;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import org.apache.commons.io.FileUtils;

public class Deliver {
	private static Logger logger = Logger.getLogger( Deliver.class.getName() );

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

    public Deliver( Stream stream, Stream target ) {
        this.stream = stream;
        this.target = target;
    }

    /**
     * @deprecated since 0.6.10
     */
	public boolean deliverForced( Stream stream, Stream target, File viewcontext, String viewtag ) throws DeliverException, CleartoolException {
		return deliver( true, true, true, false );
	}

    /**
     * @deprecated since 0.6.13 - Should it be?
     */
	public boolean deliver( boolean force, boolean complete, boolean abort, boolean resume ) throws DeliverException, CleartoolException {
		logger.fine( "Delivering " + baseline + " from " + stream + " to " + target + " in " + context + " with tag " + viewtag );
		try {
			return _deliver( force, complete, abort, resume );
		} catch( DeliverException e ) {
			if( e.getType().equals( Type.DELIVER_IN_PROGRESS ) && !resume ) { //could be a posted delivery
				String status = getStatus( stream );
				if( status.replace( System.getProperty( "line.separator" ), " " ).contains( "Operation posted from" ) ) {
					logger.fine( "Posted delivery" );
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
		cmd += ( stream != null ? " -stream " + stream : "" );
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
			logger.log( Level.WARNING, "Could not deliver to target " + target, e);
			
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
			/* Deliver in progress */
			else if( e.getMessage().matches( "(?i)(?m)(?s)^.*which is currently involved in an.*active deliver or rebase operation.*The set activity of this view may not be.*changed until the operation has completed.*$" ) ) {
				logger.warning( "(6)Deliver already in progress" );
				throw new DeliverException( this, Type.DELIVER_IN_PROGRESS, e );
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
	
	public static void rollBack( String oldViewTag, Stream oldSourceStream, File context ) throws CleartoolException, IOException {

		//String cmd = "deliver -cancel -force -stream " + oldSourceStream;
        String cmd = "deliver -cancel -stream " + oldSourceStream;

		try {
			if( context.exists() ) {
				Cleartool.run( cmd, context );
			} else {
				SnapshotView.regenerateViewDotDat( context, oldViewTag );
				Cleartool.run( cmd, context );
			}
		} catch( Exception e ) {
			throw new CleartoolException( "Could not regenerate view to force deliver: " + oldViewTag, e );
		} finally {
			if( context.exists() && oldViewTag != null ) {
                FileUtils.deleteDirectory( context );
			}
		}
	}

	public void cancel() throws CancelDeliverException {
		cancel( stream, context );
	}

	public static void cancel( Stream stream, File context ) throws CancelDeliverException {
        logger.fine( "Cancel deliver" + ( stream != null ? " from " + stream.getNormalizedName() : "" ) + " in " + context );

		try {
			String cmd = "deliver -cancel -force" + ( stream != null ? " -stream " + stream : "" );
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CancelDeliverException( stream, e );
		}
	}

    public static void cancel( Stream targetStream ) throws ClearCaseException {
        logger.fine( "Cancelling deliver to " + targetStream.getNormalizedName() );

        Stream source = targetStream.getDeliveringStream( false );
        if( source != null ) {
            logger.fine( "Source stream is " + source.getNormalizedName() );
            try {
                String cmd = "deliver -cancel -force -stream " + source;
                Cleartool.run( cmd );
            } catch( AbnormalProcessTerminationException e ) {
                throw new CancelDeliverException( targetStream, e );
            }
        } else {
            throw new CancelDeliverException( targetStream, "Could not find source stream for deliver" );
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

    public static final Pattern rxFindStream   = Pattern.compile( "^Deliver operation in progress on stream \"(.*?)\"$", Pattern.MULTILINE );
    public static final Pattern rxFindActivity = Pattern.compile( "^\\s*Using integration activity \"(.*?)\"\\.$", Pattern.MULTILINE );
    public static final Pattern rxFindViewTag  = Pattern.compile( "^\\s*Using view \"(.*?)\"\\.$", Pattern.MULTILINE );

    public static class Status {
        private DeliverStatus status;
        private Stream sourceStream;
        private Activity activity;
        private String viewTag;

        private Status() {}

        public Status( DeliverStatus status, Stream sourceStream, Activity activity, String viewTag ) {
            this.status = status;
            this.sourceStream = sourceStream;
            this.activity = activity;
            this.viewTag = viewTag;
        }

        public static Status getStatus( Stream stream ) throws ClearCaseException {
            return Status.getStatus( Deliver.getStatus( stream ) );
        }

        public static Status getStatus( String statusString ) throws ClearCaseException {
            Status s = new Status();

            s.status = stringToStatus( statusString );
            if( s.status.equals( DeliverStatus.DELIVER_IN_PROGRESS ) ) {

                Matcher streamName   = rxFindStream.matcher( statusString );
                Matcher activityName = rxFindActivity.matcher( statusString );
                Matcher viewTag      = rxFindViewTag.matcher( statusString );

                if( streamName.find() && activityName.find() && viewTag.find() ) {
                    s.sourceStream = Stream.get( streamName.group( 1 ) );
                    s.activity = Activity.get( activityName.group( 1 ), s.sourceStream.getPVob() );
                    s.viewTag = viewTag.group( 1 );
                } else {
                    throw new CleartoolException( "Unable to find deliver elements" );
                }
            }

            return s;
        }

        public Activity getActivity() {
            return activity;
        }

        public String getViewTag() {
            return viewTag;
        }

        public Stream getSourceStream() {
            return sourceStream;
        }

        public boolean isInProgress() {
            return status.equals( DeliverStatus.DELIVER_IN_PROGRESS );
        }

        @Override
        public String toString() {
            if( isInProgress() ) {
                return "DeliverStatus[" + sourceStream.getShortname() + ", in progress]";
            } else {
                return "DeliverStatus[No deliver in progress]";
            }
        }
    }
	
	public File getViewContext() {
		return this.context;
	}
	
	public String getViewtag() {
		return this.viewtag;
	}
}
