package net.praqma.clearcase.ucm.utils.filters;

import java.util.Iterator;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.Baselines2;
import net.praqma.util.debug.Logger;

public class AfterBaseline extends BaselineFilter {
	
	private static Logger logger = Logger.getLogger();

	private Baseline after;
	
	public AfterBaseline( Baseline after ) {
		this.after = after;
	}
	
	@Override
	public int filter( Baselines2 baselines ) {
		int pruned = 0;
		Iterator<Baseline> itAfter = baselines.iterator();
		while( itAfter.hasNext() ) {
			Baseline baseline = itAfter.next();
			logger.debug( "Removing " + baseline.getNormalizedName() );
			if( baseline.equals( after ) ) {
				/* We found the baseline we were looking for */
				/* Let's remove this too */
				itAfter.remove();
				pruned++;
				break;
			} else {
				itAfter.remove();
				pruned++;
			}
		}
		
		return pruned;
	}

	@Override
	public String getName() {
		return "Return baselines after " + after.getNormalizedName();
	}

}
