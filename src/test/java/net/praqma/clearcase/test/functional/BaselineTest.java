package net.praqma.clearcase.test.functional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.test.BaseClearCaseTest;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.util.ExceptionUtils;

import org.junit.ClassRule;
import org.junit.Test;

public class BaselineTest extends BaseClearCaseTest {

    private static final Logger logger = Logger.getLogger( BaselineTest.class.getName() );

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-baseline" );

	@Test
	public void testLoadAndPromotionLevel() throws Exception {
		Baseline bl = ccenv.context.baselines.get( "model-3" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel(  ) );
	}
	
	@Test
	public void testCreateBaseline() throws Exception {
		String viewtag = ccenv.getUniqueName() + "_one_int";
		System.out.println( "VIEW: " + ccenv.context.views.get( viewtag ) );
		//File path = new File( context.views.get( viewtag ).getPath() );
		File path = new File( ccenv.context.mvfs + "/" + viewtag + "/" + ccenv.getVobName() );
		
		System.out.println( "PATH: " + path );
		
		try {
			ccenv.addNewContent( ccenv.context.components.get( "Model" ), path, "test.txt" );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		
		Baseline.create( "new-baseline", ccenv.context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
	}
	
	@Test
	public void testGetStream() throws Exception {
		Baseline bl = ccenv.context.baselines.get( "model-1" ).load();
		
		assertEquals( ccenv.context.integrationStreams.get( "one_int" ), bl.getStream() );
	}
	
	@Test
	public void testGetComponent() throws Exception {
		Baseline bl = ccenv.context.baselines.get( "client-1" ).load();
		
		assertEquals( ccenv.context.components.get( "_System" ), bl.getComponent() );
	}
	
	@Test
	public void testGet() throws Exception {
		Baseline bl = Baseline.get( "_System_1.0@" + ccenv.getPVob() );
		
		assertNotNull( bl );
	}
	
	@Test
	public void testGetPvob() throws Exception {
		Baseline bl = Baseline.get( "_System_1.0", ccenv.getPVob() );
		
		assertNotNull( bl );
	}

    @Test
    public void testCompositeDescendantBaselines() throws UnableToInitializeEntityException, CleartoolException {
        Baseline b = ccenv.context.baselines.get( "model-1" );

        List<Baseline> baselines = b.getCompositeDependantBaselines();

        assertThat( baselines.size(), is( 5 ) );
    }

    @Test
    public void testCompositeMemberBaselines() throws UnableToInitializeEntityException, CleartoolException {
        Baseline model = ccenv.context.baselines.get( "model-1" );
        Baseline b = model.getRootedBaseline();

        logger.info( "I found " + b );

        List<Baseline> members = b.getCompositeMemberBaselines();

        Component componentModel = ccenv.context.components.get( "Model" );
        Component componentServer = ccenv.context.components.get( "_Server" );
        Component componentClient = ccenv.context.components.get( "_Client" );
        Component componentSystem = ccenv.context.components.get( "_System" );

        assertThat( members.size(), is( 7 ) );
        assertThat( members.get( 0 ).getComponent(), is( componentServer ) );
        assertThat( members.get( 1 ).getComponent(), is( componentSystem ) );
        assertThat( members.get( 2 ).getComponent(), is( componentSystem ) );
        assertThat( members.get( 3 ).getComponent(), is( componentClient ) );
        assertThat( members.get( 4 ).getComponent(), is( componentSystem ) );
        assertThat( members.get( 5 ).getComponent(), is( componentClient ) );
        assertThat( members.get( 6 ).getComponent(), is( componentSystem ) );
    }

    @Test
    public void testGetPostedBaselinesFor() throws UnableToInitializeEntityException, CleartoolException {
        Baseline model = ccenv.context.baselines.get( "model-1" );
        Baseline client = ccenv.context.baselines.get( "client-1" );

        /* Find a rooted baseline */
        Baseline b = model.getRootedBaseline();

        Component system = ccenv.context.components.get( "_System" );

        List<Baseline> posted = b.getPostedBaselinesFor( system );

        assertNotNull( posted );
        assertThat( posted.size(), is( 4 ) );
        assertThat( posted.get( 0 ), is( model ) );
        assertThat( posted.get( 1 ), is( client ) );
        assertThat( posted.get( 2 ), is( model ) );
        assertThat( posted.get( 3 ), is( client ) );
    }

    @Test
    public void testGetPostedBaselinesFor2() throws UnableToInitializeEntityException, CleartoolException {
        Baseline client = ccenv.context.baselines.get( "client-3" );

        /* Find a rooted baseline */
        Baseline b = client.getRootedBaseline();

        Component system = ccenv.context.components.get( "_System" );

        List<Baseline> posted = b.getPostedBaselinesFor( system );

        assertNotNull( posted );
        assertThat( posted.size(), is( 1 ) );
        assertThat( posted.get( 0 ), is( client ) );
    }

    @Test
    public void testPromote() throws UnableToPromoteBaselineException {
        Baseline b = ccenv.context.baselines.get( "client-3" );
        b.promote();
        assertThat(b.getPromotionLevel(), is(PromotionLevel.BUILT));
    }
    
    @Test
    public void testForcedLoad() throws UnableToPromoteBaselineException, UnableToLoadEntityException, UnableToInitializeEntityException {
        Baseline b1 = ccenv.context.baselines.get( "client-3" ).load();
        Baseline b2 = ccenv.context.baselines.get( "client-3" ).load(true);
        assertThat(b1, is(b2));
    }
}
