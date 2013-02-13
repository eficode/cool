package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Stream;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author cwolfgang
 *         Date: 13-02-13
 *         Time: 16:34
 */
public class StreamTest {
    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB6497", "setup-siblings.xml" );

    @Test
    public void findSiblings() {
        Stream one = ccenv.context.streams.get( "one_int" );
    }
}
