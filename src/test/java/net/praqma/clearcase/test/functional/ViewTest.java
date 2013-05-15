package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class ViewTest extends GetViewTestBase {
    private static Logger logger = Logger.getLogger( ViewTest.class.getName() );

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "cool-view-test" );

    @Test
    public void test01() {

    }
}
