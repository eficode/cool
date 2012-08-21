package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class BaselineList extends ArrayList<Baseline> {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private static Logger logger = Logger.getLogger();

	private List<BaselineFilter> filters = new ArrayList<BaselineFilter>();
	private Comparator<Baseline> sorter;
	private boolean load = false;

	private Stream stream;
	private Component component;
	private PromotionLevel level;
	private boolean multisitePolling;
	private int limit = 0;

	public BaselineList() {
		tracer.entering(BaselineList.class.getSimpleName(), "BaselineList");
		tracer.exiting(BaselineList.class.getSimpleName(), "BaselineList");
	}


	public BaselineList( Stream stream, Component component, PromotionLevel plevel ) {
		this( stream, component, plevel, false );
		tracer.entering(AscendingDateSort.class.getSimpleName(), "BaselineList", new Object[]{stream, component, plevel});
		tracer.exiting(AscendingDateSort.class.getSimpleName(), "BaselineList");
	}

	public BaselineList( Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling ) {
		tracer.entering(BaselineList.class.getSimpleName(), "BaselineList", new Object[]{stream, component, plevel, multisitePolling});
		this.stream = stream;

		this.component = component;
		this.level = plevel;
		this.multisitePolling = multisitePolling;
		tracer.exiting(BaselineList.class.getSimpleName(), "BaselineList");
	}

	/**
	 * Create a {@link BaselineList} object from a list of {@link Baseline}s
	 * @param baselines - A list of {@link Baseline}s
	 * @return
	 */
	public BaselineList( List<Baseline> baselines ) {
		tracer.entering(BaselineList.class.getSimpleName(), "BaselineList", new Object[]{baselines});
		this.addAll( baselines );
		tracer.exiting(BaselineList.class.getSimpleName(), "BaselineList");
	}


	/**
	 * Apply all the given filters and rules to this
	 * @return
	 * @throws UnableToInitializeEntityException
	 * @throws UnableToListBaselinesException
	 */
	public BaselineList apply() throws UnableToInitializeEntityException, UnableToListBaselinesException {
		tracer.entering(BaselineList.class.getSimpleName(), "apply");
		/* Printing info for debug */
		logger.debug( " --- Get baselines information --- " );
		logger.debug( "Component: " + component.getNormalizedName() );
		logger.debug( "Stream   : " + stream.getNormalizedName() );
		logger.debug( "Level    : " + level );
		logger.debug( "Limit    : " + limit );
		logger.debug( "# filters: " + filters.size() );
		logger.debug( "Multisite: " + multisitePolling );

		if( multisitePolling ) {
			if( stream.hasPostedDelivery() ) {
				this.addAll( stream.getPostedBaselines( component, level ) );
			} else {
				tracer.exiting(BaselineList.class.getSimpleName(), "apply", this);
				return this;
			}
		} else {
			this.addAll( _get() );
		}

		logger.debug( " --- Bare retrieval --- " );
		logger.debug( "Baselines: " + this );

		/* Do the filtering */
		int pruned = 0;
		for( BaselineFilter filter : filters ) {
			logger.debug( "Filter: " + filter.getName() );
			pruned += filter.filter( this );
			logger.debug( "Baselines: " + this );
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
			logger.verbose( "[ClearCase] Pruned " + pruned + " baselines" );
		}

		/* Sort the baselines */
		if( sorter != null ) {
			Collections.sort( this, sorter );
		}		

		/* Limit? 0 = unlimited */
		if( limit > 0 && this.size() > 0 ) {
			BaselineList n = new BaselineList();
			n.addAll( this.subList( 0, limit ) );
			logger.debug( "Final list of baselines: " + n );
			tracer.exiting(BaselineList.class.getSimpleName(), "apply", n);
			return n;
		} else {
			logger.debug( "Final list of baselines: " + this );
			tracer.exiting(BaselineList.class.getSimpleName(), "apply", this);
			return this;
		}
	}

	/**
	 * Apply a single filter to the {@link BaselineList}
	 * @param filter
	 * @return
	 */
	public BaselineList applyFilter( BaselineFilter filter ) {
		tracer.entering(BaselineList.class.getSimpleName(), "applyFilter", new Object[]{filter});
		logger.debug( "Filter: " + filter.getName() );
		filter.filter( this );
		tracer.exiting(BaselineList.class.getSimpleName(), "applyFilter", this);
		return this;
	}

	/**
	 * Set a limit of how many {@link Baseline}s apply should return
	 * @param limit
	 * @return
	 */
	public BaselineList setLimit( int limit ) {
		tracer.entering(BaselineList.class.getSimpleName(), "setLimit", new Object[]{limit});
		this.limit = limit;
		tracer.exiting(BaselineList.class.getSimpleName(), "setLimit", this);
		return this;
	}

	/**
	 * Load the {@link Baseline}s
	 * @return
	 */
	public BaselineList load() {
		tracer.entering(BaselineList.class.getSimpleName(), "load");
		this.load = true;
		tracer.exiting(BaselineList.class.getSimpleName(), "load", this);
		return this;
	}

	/**
	 * Set the sorting of the {@link BaselineList}
	 * @param sorter - A {@link Comparator} of {@link Baseline}s
	 * @return
	 */
	public BaselineList setSorting( Comparator<Baseline> sorter ) {
		tracer.entering(BaselineList.class.getSimpleName(), "setSorting", new Object[]{sorter});
		this.sorter = sorter;
		tracer.exiting(BaselineList.class.getSimpleName(), "setSorting", this);
		return this;
	}

	/**
	 * Add a filter to apply
	 * @param filter
	 * @return
	 */
	public BaselineList addFilter( BaselineFilter filter ) {
		tracer.entering(BaselineList.class.getSimpleName(), "addFilter", new Object[]{filter});
		this.filters.add( filter );
		tracer.exiting(BaselineList.class.getSimpleName(), "addFilter", this);
		return this;

	}

	private List<Baseline> _get() throws UnableToInitializeEntityException, UnableToListBaselinesException {
		tracer.entering(BaselineList.class.getSimpleName(), "_get");
		List<String> bls_str = null;

		String cmd = "lsbl -s -component " + component + " -stream " + stream + ( level != null ? " -level " + level.toString() : "" );
		try {
			bls_str = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToListBaselinesException( stream, component, level, e );
		}

		logger.debug( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		int c = 0;
		for( String bl : bls_str ) {
			bls.add( Baseline.get( bl, stream.getPVob() ) );
			c++;
		}
		tracer.exiting(BaselineList.class.getSimpleName(), "_get", bls);
		return bls;
	}

	public static class AscendingDateSort implements Comparator<Baseline> {

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

}
