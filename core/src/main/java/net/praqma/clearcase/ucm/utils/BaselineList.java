package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class BaselineList extends ArrayList<Baseline> {
	private static Logger logger = Logger.getLogger( BaselineList.class.getName() );

	private List<BaselineFilter> filters = new ArrayList<BaselineFilter>();
	private Comparator<Baseline> sorter;
	private boolean load = false;
	
	private Stream stream;
	private Component component;
	private PromotionLevel level;
	private boolean multisitePolling;
	private int limit = 0;
	
	public BaselineList() {
		
	}
	
	public BaselineList( Stream stream, Component component, PromotionLevel plevel ) {
		this( stream, component, plevel, false );
	}

	public BaselineList( Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling ) {
		this.stream = stream;
		this.component = component;
		this.level = plevel;
		this.multisitePolling = multisitePolling;
	}
	
	/**
	 * Create a {@link BaselineList} object from a list of {@link Baseline}s
	 * @param baselines - A list of {@link Baseline}s
	 * @return
	 */
	public BaselineList( List<Baseline> baselines ) {
		this.addAll( baselines );
	}
	
	/**
	 * Apply all the given filters and rules to this
	 * @return
	 * @throws UnableToInitializeEntityException
	 * @throws UnableToListBaselinesException
	 */
	public BaselineList apply() throws UnableToInitializeEntityException, UnableToListBaselinesException {

		/* Printing info for debug */
		logger.fine( " --- Get baselines information --- " );
		logger.fine( "Component: " + component.getNormalizedName() );
		logger.fine( "Stream   : " + stream.getNormalizedName() );
		logger.fine( "Level    : " + level );
		logger.fine( "Limit    : " + limit );
		logger.fine( "# filters: " + filters.size() );
		logger.fine( "Multisite: " + multisitePolling );

		if( multisitePolling ) {
			if( stream.hasPostedDelivery() ) {
				this.addAll( stream.getPostedBaselines( component, level ) );
			} else {
				return this;
			}
		} else {
			this.addAll( _get() );
		}

		logger.fine( " --- Bare retrieval --- " );
		logger.fine( "Baselines: " + this );

		/* Do the filtering */
		int pruned = 0;
		for( BaselineFilter filter : filters ) {
			logger.fine( "Filter: " + filter.getName() );
			pruned += filter.filter( this );
			logger.fine( "Baselines: " + this );
		}
		
		/* Load em? */
		if( load ) {
			Iterator<Baseline> it = this.iterator();
			while( it.hasNext() ) {
				Baseline baseline = it.next();
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
		}
		
		if( pruned > 0 ) {
			logger.config( "[ClearCase] Pruned " + pruned + " baselines" );
		}
		
		/* Sort the baselines */
		if( sorter != null ) {
			Collections.sort( this, sorter );
		}		
		
		/* Limit? 0 = unlimited */
		if( limit > 0 && this.size() > 0 ) {
			BaselineList n = new BaselineList();
			n.addAll( this.subList( 0, limit ) );
			logger.fine( "Final list of baselines: " + n );
			return n;
		} else {
			logger.fine( "Final list of baselines: " + this );
			return this;
		}
	}
	
	/**
	 * Apply a single filter to the {@link BaselineList}
	 * @param filter
	 * @return
	 */
	public BaselineList applyFilter( BaselineFilter filter ) {
		logger.fine( "Filter: " + filter.getName() );
		filter.filter( this );
		
		return this;
	}
	
	/**
	 * Set a limit of how many {@link Baseline}s apply should return
	 * @param limit
	 * @return
	 */
	public BaselineList setLimit( int limit ) {
		this.limit = limit;
		
		return this;
	}
	
	/**
	 * Load the {@link Baseline}s
	 * @return
	 */
	public BaselineList load() {
		this.load = true;
		
		return this;
	}
	
	/**
	 * Set the sorting of the {@link BaselineList}
	 * @param sorter - A {@link Comparator} of {@link Baseline}s
	 * @return
	 */
	public BaselineList setSorting( Comparator<Baseline> sorter ) {
		this.sorter = sorter;
		
		return this;
	}

	/**
	 * Add a filter to apply
	 * @param filter
	 * @return
	 */
	public BaselineList addFilter( BaselineFilter filter ) {
		this.filters.add( filter );
		
		return this;
	}

	private List<Baseline> _get() throws UnableToInitializeEntityException, UnableToListBaselinesException {
		List<String> bls_str = null;
		
		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( level != null ? " -level " + level.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToListBaselinesException( stream, component, level, e );
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
	
	public static class AscendingDateSort implements Comparator<Baseline> {

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
	
}
