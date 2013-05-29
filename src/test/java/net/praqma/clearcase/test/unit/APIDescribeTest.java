package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.api.Describe;
import net.praqma.clearcase.exceptions.CleartoolException;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class APIDescribeTest {

    @Test
    public void testMemberOfClosure() {
        Describe d = new Describe( "object" ).addModifier( Describe.memberOfClosure );

        assertThat( d.getCommandLine(), is( "describe -fmt %[member_of_closure]p object" ) );
    }

    @Test
    public void testDependsOn() {
        Describe d = new Describe( "object" ).addModifier( Describe.dependsOn );

        assertThat( d.getCommandLine(), is( "describe -fmt %[depends_on]p object" ) );
    }

    @Test
    public void testDependsOnWithC() {
        Describe d = new Describe( "object" ).addModifier( Describe.dependsOn.clone().commaSeparate() );

        assertThat( d.getCommandLine(), is( "describe -fmt %[depends_on]Cp object" ) );
    }

    @Test
    public void testInitialBaseline() {
        Describe d = new Describe( "object" ).addModifier( Describe.initialBaseline );

        assertThat( d.getCommandLine(), is( "describe -fmt %[initial_bl]Xp object" ) );
    }

    @Test
    public void testGetResults() throws CleartoolException {
        String s = "1 2 3";
        Describe d = new Describe( "" ).addModifier( Describe.dependsOn.clone() );
        List<String> r = d.getElements( s );
        Map<String, String[]> rs = d.getResults( r );

        assertThat( r.size(), is( 1 ) );
        assertThat( rs.get( "depends_on" ).length, is( 3 ) );
        assertThat( rs.get( "depends_on" ), is( new String[] { "1", "2", "3" } ) );
    }

    @Test
    public void testGetResultsComma() throws CleartoolException {
        String s = "1, 2, 3";
        Describe d = new Describe( "" ).addModifier( Describe.dependsOn.clone().commaSeparate() );
        List<String> r = d.getElements( s );
        Map<String, String[]> rs = d.getResults( r );

        assertThat( r.size(), is( 1 ) );
        assertThat( rs.get( "depends_on" ).length, is( 3 ) );
        assertThat( rs.get( "depends_on" ), is( new String[] { "1", "2", "3" } ) );
    }

    @Test
    public void testGetElements() {
        String s = "1\n2\n3";
        List<String> d = new Describe( "" ).getElements( s );

        assertThat( d.size(), is( 3 ) );
        assertThat( d.get( 0 ), is( "1" ) );
        assertThat( d.get( 1 ), is( "2" ) );
        assertThat( d.get( 2 ), is( "3" ) );
    }
}
