package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.Environment;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.util.io.FileUtilities;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 06-03-13
 *         Time: 11:41
 */
public class DeliverTest {

    @Test
    public void testStatus() throws FileNotFoundException, ClearCaseException, UnsupportedEncodingException {
        String output = FileUtilities.getContent( new File( URLDecoder.decode( Environment.class.getClassLoader().getResource( "output/deliverStatus01.txt" ).getFile(), "UTF-8" ) ) );

        Deliver.Status status = Deliver.Status.getStatus( output );

        assertThat( status.isInProgress(), is( true ) );
        assertThat( status.getSourceStream().getShortname(), is( "one_dev" ) );
        assertThat( status.getActivity().getShortname(), is( "deliver.one_dev.20130305.162827" ) );
        assertThat( status.getViewTag(), is( "ccucm_one_int" ) );
    }

    @Test
    public void testStatus2() throws FileNotFoundException, ClearCaseException, UnsupportedEncodingException {
        String output = FileUtilities.getContent( new File( URLDecoder.decode( Environment.class.getClassLoader().getResource( "output/deliverStatus02.txt" ).getFile(), "UTF-8" ) ) );

        Deliver.Status status = Deliver.Status.getStatus( output );

        assertThat( status.isInProgress(), is( false ) );
        assertNull( status.getSourceStream() );
        assertNull( status.getActivity() );
        assertNull( status.getViewTag() );
    }
}
