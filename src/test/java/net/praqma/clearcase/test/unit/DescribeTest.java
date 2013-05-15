package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.Describe;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class DescribeTest {

    @Test( expected = IllegalStateException.class )
    public void simpleFail() throws UnableToInitializeEntityException {
        Baseline b = Baseline.get( "bl@\\pvob" );
        Describe d = new Describe( b );
        d.getCommandLine();
    }

    @Test
    public void simpleDependsOn() throws UnableToInitializeEntityException {
        Baseline b = Baseline.get( "bl@\\pvob" );
        Describe d = new Describe( b ).dependentsOn();
        String cmd = d.getCommandLine();

        assertThat( cmd, is( "describe -fmt {%[depends_on]Cp} baseline:bl@\\pvob" ) );
    }

    @Test
    public void getElementsSimple() throws UnableToInitializeEntityException {
        Baseline b = Baseline.get( "bl@\\pvob" );
        Describe d = new Describe( b );
        List<String> r = d.getElements( "{1}{2}{3}" );

        assertThat( r.size(), is( 3 ) );
        assertThat( r.get( 0 ), is( "1" ) );
        assertThat( r.get( 1 ), is( "2" ) );
        assertThat( r.get( 2 ), is( "3" ) );
    }

    @Test
    public void getElementsSimpleLineMultiple() throws UnableToInitializeEntityException {
        Baseline b = Baseline.get( "bl@\\pvob" );
        Describe d = new Describe( b ).dependentsOn();
        List<String> r = d.getElements( "{1,2,3}" );
        Map<String, String[]> rs = d.getResults( r );

        assertThat( r.size(), is( 1 ) );
        assertThat( rs.get( "depends_on" ).length, is( 3 ) );
        assertThat( rs.get( "depends_on" ), is( new String[] { "1", "2", "3" } ) );
    }
}
