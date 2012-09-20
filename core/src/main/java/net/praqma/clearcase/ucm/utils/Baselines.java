package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.execute.AbnormalProcessTerminationException;

/**
 * @deprecated since 0.6.0
 */
public class Baselines {
	private static Logger logger = Logger.getLogger( Baselines.class.getName()  );
	
	public static List<Baseline> get( Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		logger.fine( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel + " and multisitepolling " +  multisitePolling);
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
		logger.fine( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel );
		List<String> bls_str = null;
		
		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( plevel != null ? " -level " + plevel.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to get baselines: " + e.getMessage() );
			throw new UnableToListBaselinesException( stream, component, plevel, e );
		}

		logger.fine( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		int c = 0;
		for( String bl : bls_str ) {
			bls.add( Baseline.get( bl, stream.getPVob() ) );
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
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, null, null );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, date, null );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Baseline after) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return get( component, stream, plevel, max, null, after );
	}
	
	public static List<Baseline> get( Component component, Stream stream, PromotionLevel plevel, int max, Date date, Baseline after ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		List<Baseline> baselines = null;
		
		/* Printing info for debug */
		logger.fine( " --- Get baselines information --- " );
		logger.fine( "Component: " + component.getNormalizedName() );
		logger.fine( "Stream   : " + stream.getNormalizedName() );
		logger.fine( "Level    : " + plevel );
		logger.fine( "Max      : " + max );
		logger.fine( "Date     : " + date );
		logger.fine( "Baseline : " + ( after != null ? after.getNormalizedName() : "N/A" ) );

		baselines = Baselines.get( stream, component, plevel );
		logger.fine( " --- Bare retrieval --- " );
		logger.fine( "Baselines: " + baselines );

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
			logger.config( "[ClearCase] Pruned " + pruned + " baselines" );
		}
		
		logger.fine( " --- Pruned for delivers + loads --- " );
		logger.fine( "Baselines: " + baselines );

		/* Sort by date - first is oldest, last is newest */
		Collections.sort( baselines, new AscendingDateSort() );
        for (Baseline b : baselines)
            logger.fine( "Baselines: " + b.stringify() );
        
		/* Get from a specific baseline */
		if( after != null ) {
			Iterator<Baseline> itAfter = baselines.iterator();
			while( itAfter.hasNext() ) {
				Baseline baseline = itAfter.next();
				logger.fine( "Removing " + baseline.getNormalizedName() );
				if( baseline.equals( after ) ) {
					/* We found the baseline we were looking for */
					/* Let's remove this too */
					itAfter.remove();
					break;
				} else {
					itAfter.remove();
				}
			}
			
			logger.fine( " --- Pruned before baseline --- " );
			logger.fine( "Baselines: " + baselines );
		}
		
		/* Prune from a specified date */
		if( date != null ) {
			logger.fine( "Date is " + date );
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
			logger.fine( " --- Pruned before date --- " );
			logger.fine( "Baselines: " + baselines );
		} else {
			/* No modifier */
		}
		
		logger.config( "Done" );
		
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
