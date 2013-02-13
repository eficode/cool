package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListProjectsException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Stream;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 13-02-13
 *         Time: 16:34
 */
public class StreamTest {
    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB8603", "setup-siblings.xml" );

    @Test
    public void findSiblings() throws UnableToInitializeEntityException, UnableToListProjectsException {
        Stream one = ccenv.context.streams.get( "one_int" );

        List<Stream> siblings = one.getSiblingStreams();
        assertThat( siblings.size(), is( 2 ) );
    }
}
