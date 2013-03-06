package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 06-03-13
 *         Time: 13:34
 */
public class FB8671 {

    @Rule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB8671", "setup-8671.xml" );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" );

    @Test
    public void test01() throws ClearCaseException {
        Stream child = ccenv.context.streams.get( "one_dev" );
        Stream target = ccenv.context.streams.get( "one_int" );
        Deliver deliver = new Deliver( child, target );
        deliver.deliver( true, false, true, false );

        Stream delivering = target.getDeliveringStream( false );

        assertThat( delivering, is( child ) );
    }

    @Test
    @ClearCaseUniqueVobName( name = "childchild" )
    public void test02() throws ClearCaseException {
        Stream child = ccenv.context.streams.get( "one_dev3" );
        Stream target = ccenv.context.streams.get( "one_int" );
        Deliver deliver = new Deliver( child, target );
        deliver.deliver( true, false, true, false );

        Stream delivering = target.getDeliveringStream( false );

        assertThat( delivering, is( child ) );
    }
}
