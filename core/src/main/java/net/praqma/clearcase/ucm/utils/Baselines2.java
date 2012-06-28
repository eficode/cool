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
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Baselines2 extends ArrayList<Baseline> {
	private static Logger logger = Logger.getLogger();

	private List<BaselineFilter> filters = new ArrayList<BaselineFilter>();
	private Comparator<Baseline> sorter;
	private boolean load = false;
	
	private Stream stream;
	private Component component;
	private PromotionLevel level;
	private boolean multisitePolling;
	private int limit = 0;
	
	protected Baselines2() {
		
	}
	
	public Baselines2( Stream stream, Component component, PromotionLevel plevel ) {
		this( stream, component, plevel, false );
	}

	public Baselines2( Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling ) {
		this.stream = stream;
		this.component = component;
		this.level = plevel;
		this.multisitePolling = multisitePolling;
	}
	
	public Baselines2 get() throws UnableToInitializeEntityException, UnableToListBaselinesException {

		/* Printing info for debug */
		logger.debug( " --- Get baselines information --- " );
		logger.debug( "Component: " + component.getNormalizedName() );
		logger.debug( "Stream   : " + stream.getNormalizedName() );
		logger.debug( "Level    : " + level );
		logger.debug( "Limit    : " + limit );
		logger.debug( "# filters: " + filters.size() );

		if( stream.hasPostedDelivery() ) {
			if( multisitePolling ) {
				this.addAll( stream.getPostedBaselines( component, level ) );
			}
		} else {
			this.addAll( _get() );
		}

		logger.debug( " --- Bare retrieval --- " );
		logger.debug( "Baselines: " + this );

		
		/* Sort the baselines */
		if( sorter != null ) {
			Collections.sort( this, sorter );
		}
		
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
		
		
		/* Limit? 0 = unlimited */
		if( limit > 0 && this.size() > 0 ) {
			Baselines2 n = new Baselines2();
			n.addAll( this.subList( 0, limit ) );
			logger.debug( "Final list of baselines: " + n );
			return n;
		} else {
			logger.debug( "Final list of baselines: " + this );
			return this;
		}
	}
	
	public Baselines2 setLimit( int limit ) {
		this.limit = limit;
		
		return this;
	}
	
	public Baselines2 load() {
		this.load = true;
		
		return this;
	}
	
	public Baselines2 setSorter( Comparator<Baseline> sorter ) {
		this.sorter = sorter;
		
		return this;
	}

	public Baselines2 addFilter( BaselineFilter filter ) {
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
