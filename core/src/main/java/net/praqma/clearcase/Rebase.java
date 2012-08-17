package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.RebaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Rebase {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static final String rx_rebase_in_progress = "^Rebase operation in progress on stream";
	
	private static Logger logger = Logger.getLogger();
	
	private Stream stream;
	private List<Baseline> baselines = new ArrayList<Baseline>();
	private UCMView view;
	
	public Rebase( Stream stream, UCMView view, Baseline baseline ) {
		tracer.entering(Rebase.class.getSimpleName(), "Rebase", new Object[]{stream, view, baseline});
		this.stream = stream;
		this.baselines.add( baseline );
		this.view = view;
		tracer.exiting(Rebase.class.getSimpleName(), "Rebase");
	}
	
	public Rebase( Stream stream, UCMView view, List<Baseline> baselines ) {
		tracer.entering(Rebase.class.getSimpleName(), "Rebase", new Object[]{stream, view, baselines});
		this.stream = stream;
		this.baselines = baselines;
		this.view = view;
		tracer.exiting(Rebase.class.getSimpleName(), "Rebase");
	}

	public boolean rebase( boolean complete ) throws RebaseException {
		tracer.entering(Rebase.class.getSimpleName(), "rebase", complete);
		
		logger.debug( "Rebasing " + view.getViewtag() );

		String cmd = "rebase " + ( complete ? "-complete " : "" ) + " -force -view " + view.getViewtag() + " -stream " + stream;
		
		tracer.finest(String.format("Checking if there are any baselines: %s", baselines));
		
		if( baselines != null && baselines.size() > 0 ) {
			tracer.finest("There are some baselines.");
			
			cmd += " -baseline ";
			
			tracer.finest("Adding baselines to cmd.");
			for( Baseline b : baselines ) {
				cmd += b.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		CmdResult res;
		try {
			res = Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			RebaseException exception = new RebaseException( this, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		
		boolean result;
		
		tracer.finest("Checking Cleartool output to see if a rebase was needed.");
		
		if( res.stdoutBuffer.toString().matches( "^No rebase needed.*" ) ) {
			tracer.finest("Rebase was not needed.");
			result = false;
		} else {
			tracer.finest("Rebase was needed.");
			result = true;
		}
		tracer.exiting(Rebase.class.getSimpleName(), "rebase", result);
		return result;
	}
	
	public boolean isInProgress() throws CleartoolException {
		tracer.entering(Rebase.class.getSimpleName(), "isInProgress");
		boolean result = Rebase.isInProgress( stream );
		tracer.exiting(Rebase.class.getSimpleName(), "isInProgress", result);
		return result;
	}
	
	public static boolean isInProgress( Stream stream ) throws CleartoolException {
		tracer.entering(Rebase.class.getSimpleName(), "isInProgress", stream);
		
		String cmd = "rebase -status -stream " + stream;
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		String result;
		
		try {
			 result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to determine progress of " + stream, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Checking Cleartool output to see if a rebase is in progress.");
		
		boolean res;
		if( result.matches( rx_rebase_in_progress ) ) {
			res = true;
		} else {
			res = false;
		}
		tracer.exiting(Rebase.class.getSimpleName(), "isInProgress", res);
		return res;
	}
	
	public void cancel() throws CleartoolException {
		tracer.entering(Rebase.class.getSimpleName(), "cancel");
		Rebase.cancelRebase( stream );
		tracer.exiting(Rebase.class.getSimpleName(), "cancel");
	}

	public static void cancelRebase( Stream stream ) throws CleartoolException {
		tracer.entering(Rebase.class.getSimpleName(), "cancelRebase", stream);
		
		String cmd = "rebase -cancel -force -stream " + stream;
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to cancel rebase of " + stream, e );
			
			new CleartoolException( "Unable to cancel rebase of " + stream, e );
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Rebase.class.getSimpleName(), "cancelRebase");
	}
}
