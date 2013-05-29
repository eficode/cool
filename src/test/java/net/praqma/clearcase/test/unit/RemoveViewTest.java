package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.api.RemoveView;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class RemoveViewTest {

    @Test
    public void testBasic() {
        RemoveView rv = new RemoveView().all().setTag( "tag1" );

        assertThat( rv.getCommandLine(), is( "rmview -force -all -tag tag1" ) );
    }

}
