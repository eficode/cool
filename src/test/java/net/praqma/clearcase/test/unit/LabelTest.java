package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author cwolfgang
 */
public class LabelTest {

    @Test
    public void testNoLabel() {
        String t = "M:\\vobadm_view\\Kerne\\00 General@@\\main";

        Matcher m = Label.rx.matcher( t );

        assertThat( m.find(), is( false ) );
    }

    @Test
    public void testLabeled() {
        String t = "M:\\vobadm_view\\Kerne\\00 General@@\\main\\ker_work\\4 (PD2_START)";

        Matcher m = Label.rx.matcher( t );

        if( m.find() ) {
            assertThat( m.group( 1 ), is( "M:\\vobadm_view\\Kerne\\00 General@@\\main\\ker_work\\4" ) );
            assertThat( m.group( 2 ), is( "PD2_START" ) );
        } else {
            fail( "Was not true" );
        }
    }
}
