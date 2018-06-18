package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Stream;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class StreamTest {
    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB8603", "setup-siblings.xml" );

    @Test
    public void testLoad() throws CleartoolException, UnableToInitializeEntityException {
        Stream one = ccenv.context.streams.get( "one_int" );
        Stream two = ccenv.context.streams.get( "two_int" );
        one.setDefaultTarget( two );

        Stream stream = Stream.get( "one_int", one.getPVob() );
        assertThat( stream.getDefaultTarget(), is( two ) );
    }

    @Test
    public void findSiblings() throws UnableToInitializeEntityException, UnableToListProjectsException, CleartoolException, UCMEntityNotFoundException, UnableToLoadEntityException {
        Stream one = ccenv.context.streams.get( "one_int" );
        Stream two = ccenv.context.streams.get( "two_int" );
        Stream three = ccenv.context.streams.get( "three_int" );
        one.setDefaultTarget( two );
        two.setDefaultTarget( three );

        List<Stream> siblings = one.getSiblingStreams();
        assertThat( siblings.size(), is( 0 ) );

        List<Stream> siblings2 = two.getSiblingStreams();
        assertThat( siblings2.size(), is( 1 ) );
    }
}
