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
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
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
	
	public static List<Baseline> get( Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		logger.debug( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel + " and multisitepolling " +  multisitePolling);
		List<Baseline> baselines = null;
		
		if(stream.hasPostedDelivery()) {
			if(multisitePolling)
				baselines = stream.getPostedBaselines(component, plevel);
			else
				baselines = new ArrayList<Baseline>();
		}
		else
			baselines = Baselines.get( stream, component, plevel );

		return baselines;
	}

	public static List<Baseline> get( Stream stream, Component component, PromotionLevel plevel) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		logger.debug( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel );
		List<String> bls_str = null;
		
		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( plevel != null ? " -level " + plevel.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to get baselines: " + e.getMessage() );
			throw new UnableToListBaselinesException( stream, component, plevel, e );
tracer.exiting(AscendingDateSort.class.getSimpleName(), "UCMException");
tracer.entering(AscendingDateSort.class.getSimpleName(), "UCMException", new Object[]{to, baselines:, +});
		}
tracer.exiting(AscendingDateSort.class.getSimpleName(), "UnableToListBaselinesException");
tracer.entering(AscendingDateSort.class.getSimpleName(), "UnableToListBaselinesException", new Object[]{component, e});

		logger.debug( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		int c = 0;
		for( String bl : bls_str ) {
			bls.add( Baseline.get( bl, stream.getPVob() ) );
			c++;
		}

		return bls;
	}
	
	private static class AscendingDateSort implements Comparator<Baseline> {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

		@Override
		public int compare( Baseline bl1, Baseline bl2 ) {
tracer.entering(AscendingDateSort.class.getSimpleName(), "compare", new Object[]{bl1, bl2});
			if( bl2.getDate() == null ) {
tracer.exiting(AscendingDateSort.class.getSimpleName(), "compare", 1);
				return 1;
			}
			if( bl1.getDate() == null ) {
tracer.exiting(AscendingDateSort.class.getSimpleName(), "compare", -1);
				return -1;
			}
tracer.exiting(AscendingDateSort.class.getSimpleName(), "compare", (int) ( ( bl1.getDate().getTime() / 1000 ) - ( bl2.getDate().getTime() / 1000 ) ));
			return (int) ( ( bl1.getDate().getTime() / 1000 ) - ( bl2.getDate().getTime() / 1000 ) );
		}
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, null, null );
tracer.exiting(AscendingDateSort.class.getSimpleName(), "get", get( component, stream, plevel, max, null, null ));
tracer.entering(AscendingDateSort.class.getSimpleName(), "get", new Object[]{stream, max, null});
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, date, null );
tracer.exiting(AscendingDateSort.class.getSimpleName(), "get", get( component, stream, plevel, max, date, null ));
tracer.entering(AscendingDateSort.class.getSimpleName(), "get", new Object[]{stream, max, null});
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Baseline after) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, null, after );
tracer.exiting(AscendingDateSort.class.getSimpleName(), "get", get( component, stream, plevel, max, null, after ));
tracer.entering(AscendingDateSort.class.getSimpleName(), "get", new Object[]{stream, max, after});
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date, Baseline after ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		List<Baseline> baselines = null;
		
		/* Printing info for debug */
		logger.debug( " --- Get baselines information --- " );
		logger.debug( "Component: " + component.getNormalizedName() );
		logger.debug( "Stream   : " + stream.getNormalizedName() );
		logger.debug( "Level    : " + plevel );
		logger.debug( "Max      : " + max );
		logger.debug( "Date     : " + date );
		logger.debug( "Baseline : " + ( after != null ? after.getNormalizedName() : "N/A" ) );

		baselines = Baselines.get( stream, component, plevel );
		logger.debug( " --- Bare retrieval --- " );
		logger.debug( "Baselines: " + baselines );

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
		
		logger.debug( " --- Pruned for delivers + loads --- " );
		logger.debug( "Baselines: " + baselines );

		/* Sort by date - first is oldest, last is newest */
		Collections.sort( baselines, new AscendingDateSort() );
tracer.exiting(AscendingDateSort.class.getSimpleName(), "AscendingDateSort");
tracer.entering(AscendingDateSort.class.getSimpleName(), "AscendingDateSort");
        for (Baseline b : baselines)
            logger.debug( "Baselines: " + b.stringify() );
        
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
			
			logger.debug( " --- Pruned before baseline --- " );
			logger.debug( "Baselines: " + baselines );
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
			logger.debug( " --- Pruned before date --- " );
			logger.debug( "Baselines: " + baselines );
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
