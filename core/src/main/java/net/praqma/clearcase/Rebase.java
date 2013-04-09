package net.praqma.clearcase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.RebaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Rebase {
	
	private static final String rx_rebase_in_progress = "^Rebase operation in progress on stream";
	
	private static Logger logger = Logger.getLogger( Rebase.class.getName() );
	
	private Stream stream;
    private UCMView view;
    private String viewTag;
    private File viewPath;

	private List<Baseline> baselines = new ArrayList<Baseline>();
    private Set<Baseline> droppedBaselines = new HashSet<Baseline>();

    private boolean dropFromStream = false;


    /**
     * @deprecated since 0.6.13
     */
	public Rebase( Stream stream, UCMView view, Baseline baseline ) {
		this.stream = stream;
		this.baselines.add( baseline );
		this.view = view;
	}

    /**
     * @deprecated since 0.6.13
     */
	public Rebase( Stream stream, UCMView view, List<Baseline> baselines ) {
		this.stream = stream;
		this.baselines = baselines;
		this.view = view;
	}

    public Rebase( Stream stream ) {
        this.stream = stream;
    }

    public Rebase( UCMView view ) {
        this.view = view;
    }

    public Rebase( String viewTag ) {
        this.viewTag = viewTag;
    }

    public Rebase( File viewPath ) {
        this.viewPath = viewPath;
    }

    public Rebase addBaseline( Baseline baseline ) {
        this.baselines.add( baseline );

        return this;
    }

    public Rebase addBaselines( List<Baseline> baselines ) {
        this.baselines.addAll( baselines );

        return this;
    }

    public Rebase dropBaseline( Baseline baseline ) {
        droppedBaselines.add( baseline );

        return this;
    }

    public Rebase dropBaselines( List<Baseline> baselines ) {
        this.droppedBaselines.addAll( baselines );

        return this;
    }

    public Set<Baseline> getDroppedBaselines() {
        return droppedBaselines;
    }

    /**
     * Drop those foundation {@link Baseline}s not given as parameters from the {@link Stream}s configuration
     * @return
     */
    public Rebase dropFromStream() {
        dropFromStream = true;

        return this;
    }

	public boolean rebase( boolean complete ) throws RebaseException {
		logger.fine( "Rebasing" );

		String cmd = "rebase " + ( complete ? "-complete " : "" ) + " -force";

        if( view != null ) {
            cmd += " -view " + view.getViewtag();
        } else if( stream != null ) {
            cmd +=  " -stream " + stream;

            if( dropFromStream ) {
                List<Baseline> fbls = stream.getFoundationBaselines();
                logger.fine( "Dropping unselected foundation baselines. " + fbls );
                for( Baseline fbl : fbls ) {
                    if( !baselines.contains( fbl ) ) {
                        logger.finest( "Dropping " + fbl );
                        droppedBaselines.add( fbl );
                    }
                }
            }
        } else if( viewTag != null ) {
            cmd += " -view " + viewTag;
        } else {
            throw new IllegalStateException( "No valid parameters given for rebase" );
        }

		if( baselines != null && baselines.size() > 0 ) {
			cmd += " -baseline ";
			for( Baseline b : baselines ) {
				cmd += b.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}

        if( droppedBaselines != null && droppedBaselines.size() > 0 ) {
            cmd += " -dbaseline ";
            for( Baseline b : droppedBaselines ) {
                cmd += b.getNormalizedName() + ",";
            }
            cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
        }
		
		try {
			CmdResult res = Cleartool.run( cmd );

			if( res.stdoutBuffer.toString().matches( "^No rebase needed.*" ) ) {
				return false;
			} else {
				return true;
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new RebaseException( this, e );
		}
	}
	
	public boolean isInProgress() throws CleartoolException {
		return Rebase.isInProgress( stream );
	}
	
	public static boolean isInProgress( Stream stream ) throws CleartoolException {
		String cmd = "rebase -status -stream " + stream;
		try {
			String result = Cleartool.run( cmd ).stdoutBuffer.toString();
			if( result.matches( rx_rebase_in_progress ) ) {
				return true;
			} else {
				return false;
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to determine progress of " + stream, e );
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
			throw new CleartoolException( "Unable to cancel rebase of " + stream, e );
		}
	}
}
