package net.praqma.clearcase.ucm.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Baselines {
	private static Logger logger = Logger.getLogger();
	
	public static List<Baseline> get( Stream stream, Component component, PromotionLevel plevel ) throws UnableToListBaselinesException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.debug( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel );
		List<String> bls_str = null;
		
		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( plevel != null ? " -level " + plevel.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to get baselines: " + e.getMessage() );
			throw new UnableToListBaselinesException( stream, component, plevel, e );
		}

		logger.debug( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		int c = 0;
		for( String bl : bls_str ) {
			bls.add( Baseline.get( bl, stream.getPVob(), true ) );
			c++;
		}

		return bls;
	}
	
	private static class AscendingDateSort implements Comparator<Baseline> {

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
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max ) throws UnableToListBaselinesException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return get( component, stream, plevel, max, null, null );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date ) throws UnableToListBaselinesException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return get( component, stream, plevel, max, date, null );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Baseline after) throws UnableToListBaselinesException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return get( component, stream, plevel, max, null, after );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date, Baseline after ) throws UnableToListBaselinesException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		List<Baseline> baselines = null;

		baselines = Baselines.get( stream, component, plevel );

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
				logger.warning( "[ClearCase] Unable to load " + baseline.getNormalizedName() + ": " + e.getMessage() );
				it.remove();
				pruned++;
				continue;
				/* Just continue */
			}
		}
		if( pruned > 0 ) {
			logger.verbose( "[ClearCase] Pruned " + pruned + " baselines" );
		}

		/* Sort by date - first is oldest, last is newest */
		Collections.sort( baselines, new AscendingDateSort() );

		/* Get from a specific baseline */
		if( after != null ) {
			Iterator<Baseline> itAfter = baselines.iterator();
			while( itAfter.hasNext() ) {
				Baseline baseline = itAfter.next();
				logger.debug( "Removing " + baseline.getNormalizedName() );
				if( baseline.equals( after ) ) {
					/* We found the baseline we were looking for */
					/* Let's remove this too */
					itAfter.remove();
					break;
				} else {
					itAfter.remove();
				}
			}
		} 
		
		/* Prune from a specified date */
		if( date != null ) {
			logger.debug( "Date is " + date );
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
		
		logger.verbose( "Done" );
		
		/* Max? 0 = unlimited */
		if( max > 0 && baselines.size() > 0 ) {
			ArrayList<Baseline> n = new ArrayList<Baseline>();
			n.addAll( baselines.subList( 0, max ) );
			return n;
		} else {
			return baselines;
		}

	}
}
