package net.praqma.clearcase.ucm.utils.filters;

import java.util.Iterator;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

@SuppressFBWarnings("")
public class NoLabels extends BaselineFilter {
	
	@Override
	public int filter( BaselineList baselines ) {
		int pruned = 0;
		Iterator<Baseline> it = baselines.iterator();
		while( it.hasNext() ) {
			Baseline baseline = it.next();
			if( baseline.getLabelStatus().equals( LabelStatus.UNLABLED ) ) {
				it.remove();
				pruned++;
			}
		}
		
		return pruned;
	}
	
	@Override
	public String getName() {
		return "Skip unlabled baselines";
	}

}
