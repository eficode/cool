package net.praqma.clearcase.api;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class DiffBlUnitTest {

    @Test
    public void test01() throws UnableToInitializeEntityException {
        Baseline bl1 = Baseline.get( "bl1@\\pvob" );
        Baseline bl2 = Baseline.get( "bl2@\\pvob" );

        DiffBl diffBl = new DiffBl( bl1, bl2 ).setVersions( true ).setActivities( true );

        assertThat( diffBl.getCommandLine(), is( "diffbl -versions -activities baseline:bl2@\\pvob baseline:bl1@\\pvob" ) );
    }

    @Test
    public void testPred() throws UnableToInitializeEntityException {
        Baseline bl1 = Baseline.get( "bl1@\\pvob" );

        DiffBl diffBl = new DiffBl( bl1, null ).setVersions( true ).setActivities( true );

        assertThat( diffBl.getCommandLine(), is( "diffbl -versions -activities -predecessor baseline:bl1@\\pvob" ) );
    }
}
