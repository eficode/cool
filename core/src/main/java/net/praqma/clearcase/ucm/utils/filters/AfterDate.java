package net.praqma.clearcase.ucm.utils.filters;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

public class AfterDate extends BaselineFilter {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private static Logger logger = Logger.getLogger();

	private Date date;

	public AfterDate( Date date ) {
		tracer.entering(AfterDate.class.getSimpleName(), "AfterDate", new Object[]{date});
		this.date = date;
		tracer.exiting(AfterDate.class.getSimpleName(), "AfterDate");
	}

	@Override
	public int filter( BaselineList baselines ) {
		tracer.entering(AfterDate.class.getSimpleName(), "filter", new Object[]{baselines});
		logger.debug( "Date is " + date );

		/* Sort the baselines */
		Collections.sort( baselines, new BaselineList.AscendingDateSort() );

		int pruned = 0;
		Iterator<Baseline> itDate = baselines.iterator();
		while( itDate.hasNext() ) {
			Baseline baseline = itDate.next();
			if( baseline.getDate().before( date ) ) {
				itDate.remove();
				pruned++;
			} else {
				/* We must be after date, since the list is sorted, there's no more baselines before date */
				break;
			}
		}

		tracer.exiting(AfterDate.class.getSimpleName(), "filter", pruned);
		return pruned;
	}

	@Override
	public String getName() {
		tracer.entering(AfterDate.class.getSimpleName(), "getName");
		tracer.exiting(AfterDate.class.getSimpleName(), "getName", "Return baselines after " + date);
		return "Return baselines after " + date;
	}

}
