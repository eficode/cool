package net.praqma.clearcase.ucm.utils.filters;

import java.util.Iterator;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

public class NoLabels extends BaselineFilter {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	@Override
	public int filter( BaselineList baselines ) {
		tracer.entering(NoLabels.class.getSimpleName(), "filter", new Object[]{baselines});
		int pruned = 0;
		Iterator<Baseline> it = baselines.iterator();
		while( it.hasNext() ) {
			Baseline baseline = it.next();
			if( baseline.getLabelStatus().equals( LabelStatus.UNLABLED ) ) {
				it.remove();
				pruned++;
				continue;
			}
		}

		tracer.exiting(NoLabels.class.getSimpleName(), "filter", pruned);
		return pruned;
	}

	@Override
	public String getName() {
		tracer.entering(NoLabels.class.getSimpleName(), "getName");
		tracer.exiting(NoLabels.class.getSimpleName(), "getName", "Skip unlabled baselines");
		return "Skip unlabled baselines";
	}

}
