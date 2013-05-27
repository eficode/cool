package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.api.ListVob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class ListVobTest {

    @Test
    public void testBasic() {
        ListVob lv = new ListVob();

        assertThat( lv.getCommandLine(), is( "ls" ) );
    }

    @Test
    public void testBasicShort() {
        ListVob lv = new ListVob().shortReportLength();

        assertThat( lv.getCommandLine(), is( "ls -short" ) );
    }

    @Test
    public void testBasicRecurse() {
        ListVob lv = new ListVob().recurse();

        assertThat( lv.getCommandLine(), is( "ls -recurse" ) );
    }

    @Test
    public void testBasicViewOnly() {
        ListVob lv = new ListVob().restrictToViewOnly();

        assertThat( lv.getCommandLine(), is( "ls -view_only" ) );
    }

    @Test
    public void testBasicPname() {
        ListVob lv = new ListVob().addPathName( "path1" );

        assertThat( lv.getCommandLine(), is( "ls \"path1\"" ) );
    }

    @Test
    public void testBasicPnames() {
        ListVob lv = new ListVob().addPathName( "path1" ).addPathName( "path2" );

        assertThat( lv.getCommandLine(), is( "ls \"path1\" \"path2\"" ) );
    }

    @Test
    public void testBasicMultiple() {
        ListVob lv = new ListVob().addPathName( "path1" ).addPathName( "path2" ).shortReportLength().recurse().restrictToViewOnly();

        assertThat( lv.getCommandLine(), is( "ls -short -recurse -view_only \"path1\" \"path2\"" ) );
    }
}
