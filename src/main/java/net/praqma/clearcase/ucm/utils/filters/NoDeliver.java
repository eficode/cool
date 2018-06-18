package net.praqma.clearcase.ucm.utils.filters;

import java.util.Iterator;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

@SuppressFBWarnings("")
public class NoDeliver extends BaselineFilter {
	
	@Override
	public int filter( BaselineList baselines ) {
		int pruned = 0;
		Iterator<Baseline> it = baselines.iterator();
		while( it.hasNext() ) {
			Baseline baseline = it.next();
			if( baseline.getShortname().startsWith( "deliverbl." ) ) {
				it.remove();
				pruned++;
				continue;
			}
		}
		
		return pruned;
	}
	
	@Override
	public String getName() {
		return "Skip deliver baselines";
	}

}
