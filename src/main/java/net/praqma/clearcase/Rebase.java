package net.praqma.clearcase;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.RebaseException;
import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Rebase {
	
	private static final String rx_rebase_in_progress = "^Rebase operation in progress on stream";
	
	private static Logger logger = Logger.getLogger();
	
	private Stream stream;
	private Baseline baseline;
	private SnapshotView view;
	
	public Rebase( Stream stream, Baseline baseline, SnapshotView view ) {
		this.stream = stream;
		this.baseline = baseline;
		this.view = view;
	}

	public boolean rebase( boolean complete ) throws RebaseException {
		//context.rebaseStream( view, this, baseline, complete );
		logger.debug( "Rebasing " + view.getViewtag() );

		String cmd = "rebase " + ( complete ? "-complete " : "" ) + " -force -view " + view.getViewtag() + " -stream " + this + " -baseline " + baseline;
		try {
			CmdResult res = Cleartool.run( cmd );

			if( res.stdoutBuffer.toString().matches( "^No rebase needed.*" ) ) {
				return false;
			} else {
				return true;
			}
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( e.getMessage() );
			throw new RebaseException( this, e );
		}
	}
	
	public boolean isInProgress() throws CleartoolException {
		return Rebase.isInProgress( stream );
	}
	
	public static boolean isInProgress( Stream stream ) throws CleartoolException {
		//return context.isRebasing( this );
		String cmd = "rebase -status -stream " + stream;
		try {
			String result = Cleartool.run( cmd ).stdoutBuffer.toString();
			if( result.matches( rx_rebase_in_progress ) ) {
				return true;
			} else {
				return false;
			}
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to determine rebasing: " + e.getMessage() );
			throw new CleartoolException( e );
		}
	}
	
	public void cancel() throws CleartoolException {
		Rebase.cancelRebase( stream );
	}

	public static void cancelRebase( Stream stream ) throws CleartoolException {
		String cmd = "rebase -cancel -force -stream " + stream;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( e );
		}
	}
}
