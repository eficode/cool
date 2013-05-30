package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.test.BaseClearCaseTest;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class StreamBasicTest extends BaseClearCaseTest {

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "stream-basic-test", "setup.xml" );

    @Test
    public void getViews() throws ViewException, CleartoolException {
        Stream intStream = ccenv.context.streams.get( "one_int" );

        List<UCMView> views = intStream.getViews();

        String tagName = ccenv.getUniqueName() + "_one_int";

        assertThat( views.size(), is( 1 ) );
        assertThat( views.get( 0 ).getViewtag(), is( tagName ) );
    }
}
