package net.praqma.jenkins.utils.remoting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.Baselines;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.FileAppender;
import net.praqma.util.debug.appenders.StreamAppender;

import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * List baselines
 * 
 * @author wolfgang
 *
 */
public class GetBaselines implements FileCallable<List<Baseline>> {

	private TaskListener listener;
	private Component component;
	private Stream stream;
	private PromotionLevel plevel;
	
	private int max;

	private Date date;
	private Baseline after;
	
	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;
		
		this.max = max;
	}

	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max, Date date ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.date = date;
		
		this.max = max;
	}

	/**
	 * Retrieve a list of {@link Baseline}s after a given {@link Baseline} in chronological order
	 * @param listener
	 * @param component
	 * @param stream
	 * @param plevel
	 * @param max
	 * @param after
	 */
	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max, Baseline after ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.after = after;
		this.max = max;
	}

	private class AscendingDateSort implements Comparator<Baseline> {

		@Override
		public int compare( Baseline bl1, Baseline bl2 ) {
			if( bl2.getDate() == null ) {
				return 1;
			}
			if( bl1.getDate() == null ) {
				return -1;
			}
			return (int) ( ( bl1.getDate().getTime() / 1000 ) - ( bl2.getDate().getTime() / 1000 ) );
		}
	}

	@Override
	public List<Baseline> invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		//Appender app = new FileAppender( new File( workspace, "out.log" ) );
		//Appender app = new StreamAppender( out );
		//app.setMinimumLevel( LogLevel.DEBUG );
		//Logger.addAppender( app );
		
		out.println( "Getting baselines for " + component.getNormalizedName() + " and " + stream.getNormalizedName() );

		List<Baseline> baselines = null;

		try {
			baselines = Baselines.get( stream, component, plevel );
		} catch( ClearCaseException e ) {
			e.print( out );
			throw new IOException( "Unable to get baselines", e );
		}

		/* Prune */
		int pruned = 0;
		Iterator<Baseline> it = baselines.iterator();
		while( it.hasNext() ) {
			Baseline baseline = it.next();
			if( baseline.getShortname().startsWith( "deliverbl." ) || baseline.getLabelStatus().equals( LabelStatus.UNLABLED ) ) {
				it.remove();
				pruned++;
				continue;
			}
			
			/* Load the baseline */
			try {
				baseline.load();
			} catch( Exception e ) {
				out.println( "[ClearCase] Unable to load " + baseline.getNormalizedName() + ": " + e.getMessage() );
				it.remove();
				pruned++;
				continue;
				/* Just continue */
			}
		}
		if( pruned > 0 ) {
			out.println( "[ClearCase] Pruned " + pruned + " baselines" );
		}

		/* Sort by date - first is oldest, last is newest */
		Collections.sort( baselines, new AscendingDateSort() );

		/* Get from a specific baseline */
		if( after != null ) {
			Iterator<Baseline> itAfter = baselines.iterator();
			while( itAfter.hasNext() ) {
				Baseline baseline = itAfter.next();
				out.println( "Removing " + baseline.getNormalizedName() );
				if( baseline.equals( after ) ) {
					/* We found the baseline we were looking for */
					/* Let's remove this too */
					itAfter.remove();
					break;
				} else {
					itAfter.remove();
				}
			}
		} else if( date != null ) {
			out.println( "Date is " + date );
			Iterator<Baseline> itDate = baselines.iterator();
			while( itDate.hasNext() ) {
				Baseline baseline = itDate.next();
				if( baseline.getDate().before( date ) ) {
					itDate.remove();
				} else {
					/* We must be after date, since the list is sorted, there's no more baselines before date */
					break;
				}
			}
		} else {
			/* No modifier */
		}
		
		out.println( "Done: " + baselines.subList( 0, max ) );
		
		/* Max? 0 = unlimited */
		if( max > 0 ) {
			ArrayList<Baseline> n = new ArrayList<Baseline>();
			n.addAll( baselines.subList( 0, max ) );
			return n;
		} else {
			return baselines;
		}

		
	}

}
