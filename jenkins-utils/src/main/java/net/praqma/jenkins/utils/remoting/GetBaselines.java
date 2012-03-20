package net.praqma.jenkins.utils.remoting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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

	private Date date;
	private Baseline first;
	
	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;
	}

	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, Date date ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.date = date;
	}

	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, Baseline first ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.first = first;
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
	public List<Baseline> invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();

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
			} catch( ClearCaseException e ) {
				out.println( "[ClearCase] Unable to load " + baseline.getNormalizedName() );
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
		if( first != null ) {
			Iterator<Baseline> itFirst = baselines.iterator();
			while( itFirst.hasNext() ) {
				Baseline baseline = itFirst.next();
				if( baseline.equals( first ) ) {
					/* We found the baseline we were looking for */
					break;
				} else {
					itFirst.remove();
				}
			}
		} else if( date != null ) {
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

		return baselines;
	}

}
