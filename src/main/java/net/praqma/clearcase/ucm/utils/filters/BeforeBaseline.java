package net.praqma.clearcase.ucm.utils.filters;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

public class BeforeBaseline extends BaselineFilter {

	private static Logger logger = Logger.getLogger( BeforeBaseline.class.getName() );

	private Baseline before;

	public BeforeBaseline( Baseline before ) {
		this.before = before;
	}

    @Override
    public void preFilter( BaselineList list ) {
        logger.fine( "Ensuring " + before.getNormalizedName() );
        list.ensureBaseline( before );
    }

    @Override
	public int filter( BaselineList baselines ) {
		
		/* Sort the baselines */
		Collections.sort( baselines, new BaselineList.DescendingDateSort() );
		
		int pruned = 0;
		Iterator<Baseline> itBefore = baselines.iterator();
		while( itBefore.hasNext() ) {
			Baseline baseline = itBefore.next();
			logger.fine( "Removing " + baseline.getNormalizedName() );
			if( baseline.equals( before ) ) {
				/* We found the baseline we were looking for */
				/* Let's remove this too */
				itBefore.remove();
				pruned++;
				break;
			} else {
				itBefore.remove();
				pruned++;
			}
		}
		
		return pruned;
	}

	@Override
	public String getName() {
		return "Return baselines before " + before.getNormalizedName();
	}

}
