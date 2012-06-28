package net.praqma.clearcase.ucm.utils.filters;

import java.util.Date;
import java.util.Iterator;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.Baselines2;
import net.praqma.util.debug.Logger;

public class AfterDate extends BaselineFilter {
	
	private static Logger logger = Logger.getLogger();

	private Date date;
	
	public AfterDate( Date date ) {
		this.date = date;
	}
	
	@Override
	public int filter( Baselines2 baselines ) {
		logger.debug( "Date is " + date );
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
		
		return pruned;
	}
	
	@Override
	public String getName() {
		return "Return baselines after " + date;
	}

}
