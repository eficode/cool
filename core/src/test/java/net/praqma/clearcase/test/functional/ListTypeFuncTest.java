package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.command.ListType;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Test;

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

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" );

    @Test
    public void test() throws UnableToInitializeEntityException, CleartoolException {

        ListType ls = new ListType().setBranchType().setLocal();
        List<Branch> branches = ls.list();

        assertThat( branches.size(), is( 2 ) );
    }
}
