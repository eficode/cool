package net.praqma.clearcase.ucm.utils.filters;

import java.util.Collections;
import java.util.Iterator;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.util.debug.Logger;

public class AfterBaseline extends BaselineFilter {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
	private static Logger logger = Logger.getLogger();

	private Baseline after;
	
	public AfterBaseline( Baseline after ) {
tracer.entering(AfterBaseline.class.getSimpleName(), "AfterBaseline", new Object[]{after});
		this.after = after;
tracer.exiting(AfterBaseline.class.getSimpleName(), "AfterBaseline");
	}
	
	@Override
	public int filter( BaselineList baselines ) {
tracer.entering(AfterBaseline.class.getSimpleName(), "filter", new Object[]{baselines});
		
		/* Sort the baselines */
		Collections.sort( baselines, new BaselineList.AscendingDateSort() );
		
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
		
tracer.exiting(AfterBaseline.class.getSimpleName(), "filter", pruned);
		return pruned;
	}

	@Override
	public String getName() {
tracer.entering(AfterBaseline.class.getSimpleName(), "getName");
tracer.exiting(AfterBaseline.class.getSimpleName(), "getName", "Return baselines after " + after.getNormalizedName());
		return "Return baselines after " + after.getNormalizedName();
	}

}
