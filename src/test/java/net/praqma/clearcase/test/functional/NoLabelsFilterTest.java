/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.filters.NoLabels;
import net.praqma.util.debug.Logger;
import static org.hamcrest.CoreMatchers.not;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

/**
 *
 * @author Mads
 */
public class NoLabelsFilterTest {
    
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-blist-nolabel", "setup_noLabel.xml");

    @Test
    public void basicLoadTestFilter() throws Exception {
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "Clientapp" ).load();
        Baseline baseline = ccenv.context.baselines.get( "client-3-nolabel" );        
        
        BaselineList baselines = new BaselineList( stream, component, Project.PromotionLevel.INITIAL ).addFilter(new NoLabels()).apply();
        assertEquals("Baseline list must contain 3 elements", 3, baselines.size());
        
        //We must make sure that the baseline is NOT in the list.
        Assert.assertThat(baselines, not(hasItem(baseline)));
    }
}
