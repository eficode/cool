package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.command.ListType;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class ListTypeFuncTest {

    private static Logger logger = Logger.getLogger( ListTypeFuncTest.class.getName() );

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "list-type", "setup.xml" );

    @Test
    public void test() throws UnableToInitializeEntityException, CleartoolException {

        File path = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );

        ListType ls = new ListType().setBranchType().setLocal().setViewRoot( path );
        List<Branch> branches = ls.list();

        assertThat( branches.size(), is( 2 ) );
    }
}
