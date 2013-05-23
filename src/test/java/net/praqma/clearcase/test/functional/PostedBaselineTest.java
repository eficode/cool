package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToPromoteBaselineException;
import net.praqma.clearcase.test.BaseClearCaseTest;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
@PrepareForTest( { Baseline.class, Deliver.class } )
public class PostedBaselineTest /* extends BaseClearCaseTest */ {

    @Rule
    public PowerMockRule pmockRule = new PowerMockRule();

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

    @Rule
    public ClearCaseRule ccenv = new ClearCaseRule( "cool-posted-baseline" );

    @Test
    @ClearCaseUniqueVobName( name = "POSTED-INITIAL" )
    public void testPosted() throws UnableToInitializeEntityException, CleartoolException {
        System.out.println( "CONTEXT: " + ccenv.context );
        Baseline client3 = ccenv.context.baselines.get( "client-3" );

        Stream stream = ccenv.context.streams.get( "one_int" );

        /* Find the client-3 rooted baseline */
        List<Baseline> baselines = client3.getCompositeDependantBaselines();

        Baseline b = null;
        for( Baseline baseline : baselines ) {
            if( baseline.getFullyQualifiedName().contains( "client-3" ) && !baseline.getComponent().isRootLess() ) {
                b = baseline;
                break;
            }
        }

        PowerMockito.mockStatic( Deliver.class );
        PowerMockito.when( Deliver.getStatus( stream ) ).thenReturn( b.getFullyQualifiedName() );

        Component system = ccenv.context.components.get( "_System" );

        List<Baseline> posted = stream.getPostedBaselines( system, Project.PromotionLevel.INITIAL );

        assertNotNull( posted );
        assertThat( posted.size(), is( 1 ) );
        assertThat( posted.get( 0 ), is( client3 ) );
    }

    @Test
    @ClearCaseUniqueVobName( name = "POSTED-TESTED" )
    public void testPostedTested() throws UnableToInitializeEntityException, CleartoolException, UnableToPromoteBaselineException {
        Baseline client3 = ccenv.context.baselines.get( "client-3" );
        client3.setPromotionLevel( Project.PromotionLevel.TESTED );

        Stream stream = ccenv.context.streams.get( "one_int" );

        /* Find the client-3 rooted baseline */
        List<Baseline> baselines = client3.getCompositeDependantBaselines();

        Baseline b = null;
        for( Baseline baseline : baselines ) {
            if( baseline.getFullyQualifiedName().contains( "client-3" ) && !baseline.getComponent().isRootLess() ) {
                b = baseline;
                break;
            }
        }

        PowerMockito.mockStatic( Deliver.class );
        PowerMockito.when( Deliver.getStatus( stream ) ).thenReturn( b.getFullyQualifiedName() );

        Component system = ccenv.context.components.get( "_System" );

        List<Baseline> posted = stream.getPostedBaselines( system, Project.PromotionLevel.TESTED );

        assertNotNull( posted );
        assertThat( posted.size(), is( 1 ) );
        assertThat( posted.get( 0 ), is( client3 ) );
    }
}
