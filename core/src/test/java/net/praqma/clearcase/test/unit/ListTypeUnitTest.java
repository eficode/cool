package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.command.ListType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class ListTypeUnitTest {

    @Test
    public void test() {
        ListType ls = new ListType().setBranchType().setLocal();
        assertThat( ls.getCommandLine(), is( "lstype -short -kind brtype -local" ) );
    }

    @Test
    public void test2() {
        ListType ls = new ListType().setBranchType();
        List<String> lines = new ArrayList<String>(1);
        lines.add( "main" );

        List<Branch> branches = ls.getTypes( lines );

        assertThat( branches.size(), is( 1 ) );
        assertThat( branches.get( 0 ).getName(), is( "main" ) );
    }
}
