/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.ucm.utils.filters;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.utils.BaselineFilter;
import net.praqma.clearcase.ucm.utils.BaselineList;

/**
 *
 * @author Praqma
 */
@SuppressFBWarnings("")
public class BeforeDate extends BaselineFilter {

	private static Logger logger = Logger.getLogger( BeforeDate.class.getName() );

	private Date date;
	
	public BeforeDate( Date date ) {
		this.date = date;
	}  
    
    @Override
    public int filter(BaselineList baselines) {
        logger.fine( "Date is " + date );
		
		/* Sort the baselines */
		Collections.sort( baselines, new BaselineList.DescendingDateSort() );
		
		int pruned = 0;
		Iterator<Baseline> itDate = baselines.iterator();
		while( itDate.hasNext() ) {
			Baseline baseline = itDate.next();
            logger.finest( String.format("%s==%s", baseline.getDate(), date) );
            if( date.before( baseline.getDate() ) || date.compareTo( baseline.getDate() ) == 0 ) {
                logger.finest( "REMOVED" );
				itDate.remove();
				pruned++;
			} else {
				/* We must be after date, since the list is sorted, there are no more baselines before date */
				break;
			}
		}
		
		return pruned;
    }

    @Override
    public String getName() {
        return "Getting baselines before: " + date;
    }
    
}
