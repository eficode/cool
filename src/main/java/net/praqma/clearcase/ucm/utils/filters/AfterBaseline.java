package net.praqma.clearcase.ucm.utils.filters;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

@SuppressFBWarnings("")
public class AfterBaseline extends BaselineFilter {
	
	private static Logger logger = Logger.getLogger( AfterBaseline.class.getName() );

	private Baseline after;
	
	public AfterBaseline( Baseline after ) {
		this.after = after;
	}

    @Override
    public void preFilter( BaselineList list ) {
        logger.fine( "Ensuring " + after.getNormalizedName() );
        list.ensureBaseline( after );
    }

	@Override
	public int filter(BaselineList baselines) {
		
		/* Sort the baselines */
		Collections.sort(baselines, new BaselineList.AscendingDateSort());

		int pruned = 0;
		Iterator<Baseline> itAfter = baselines.iterator();
		while (itAfter.hasNext()) {
			Baseline baseline = itAfter.next();
			logger.fine("Removing " + baseline.getNormalizedName());
			itAfter.remove();
			pruned++;
			if (baseline.equals(after)) {
				/* Stop pruning once we've found our baseline */
				break;
			}
		}
		return pruned;
	}

	@Override
	public String getName() {
		return "Return baselines after " + after.getNormalizedName();
	}

}
