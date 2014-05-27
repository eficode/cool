package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.filters.AfterBaseline;
import net.praqma.clearcase.ucm.utils.filters.NoDeliver;
import net.praqma.clearcase.ucm.utils.filters.NoLabels;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class FB8789 {

    @Rule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB8789", "setup.xml" );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" );

    @Test
    @ClearCaseUniqueVobName( name = "fb8789" )
    public void test01() throws ClearCaseException {

        Stream stream = ccenv.context.streams.get( "one_int" );
        Component component = ccenv.context.components.get( "_System" );
        Project.PromotionLevel level = Project.PromotionLevel.INITIAL;

        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        model1.setPromotionLevel( Project.PromotionLevel.BUILT );

        BaselineList list = new BaselineList( stream, component, level, true ).
                addFilter( new AfterBaseline( model1 ) ).
                addFilter( new NoDeliver() ).
                addFilter( new NoLabels() ).
                setSorting( new BaselineList.AscendingDateSort() ).
                load();

        list.apply();

        Baseline model2 = ccenv.context.baselines.get( "model-2" );
        Baseline model3 = ccenv.context.baselines.get( "model-3" );

        assertThat( list.size(), is( 5 ) );
        assertEquals( model2, list.get( 1 ) );
        assertEquals( model3, list.get( 2 ) );
    }
}
