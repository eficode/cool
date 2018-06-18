package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author cwolfgang
 */
public class FB9184 {

    private static Logger logger = Logger.getLogger( FB9184.class.getName() );

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB9184", "setup.xml" );

    @Test
    public void test() throws CleartoolException {
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        assertThat( model1.getComponent().getRootDir(), is( "" ) );
    }

    @Test
    public void test2() throws CleartoolException, UnableToInitializeEntityException {
        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        List<Baseline> baselines = model1.getDependent();

        logger.severe( baselines.toString() );

        assertThat( baselines.size(), is( 2 ) );
    }

    @Test
    public void test3() throws CleartoolException, UnableToInitializeEntityException {
        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        List<Baseline> baselines = model1.getDependent();

        assertThat( baselines.size(), is( 2 ) );
        assertThat( baselines.get( 0 ).getDependent().size(), is( 2 ) );
        assertThat( baselines.get( 1 ).getDependent().size(), is( 2 ) );
    }

    @Test
    public void test4() throws CleartoolException, UnableToInitializeEntityException {
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        List<Baseline> baselines_1 = model1.getDependent();
        assertThat( baselines_1.size(), is( 2 ) );

        assertTrue( baselines_1.get( 0 ).getComponent().isRootLess() );
        assertTrue( baselines_1.get( 1 ).getComponent().isRootLess() );

        List<Baseline> baselines_2_1 = baselines_1.get( 0 ).getDependent();
        List<Baseline> baselines_2_2 = baselines_1.get( 1 ).getDependent();

        assertFalse( baselines_2_1.get( 0 ).getComponent().isRootLess() );
        assertFalse( baselines_2_1.get( 1 ).getComponent().isRootLess() );
        assertFalse( baselines_2_2.get( 0 ).getComponent().isRootLess() );
        assertFalse( baselines_2_2.get( 1 ).getComponent().isRootLess() );
    }
}
