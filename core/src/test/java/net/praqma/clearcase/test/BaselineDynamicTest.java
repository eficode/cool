package net.praqma.clearcase.test;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.util.ExceptionUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaselineDynamicTest extends BaseClearCaseTest {

	@Rule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-baseline-dynamic" );

	@Test
    @ClearCaseUniqueVobName( name = "bl-dyn-test-set" )
	public void testSetPromotionLevel() throws Exception {
		Baseline bl = ccenv.context.baselines.get( "model-1" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
		bl.setPromotionLevel( PromotionLevel.RELEASED );
		assertEquals( PromotionLevel.RELEASED, bl.getPromotionLevel( false ) );
	}

    @Test
    @ClearCaseUniqueVobName( name = "bl-dyn-test-promote" )
    public void testPromote() throws Exception {
        Baseline bl = ccenv.context.baselines.get( "model-3" ).load();

        assertNotNull( bl );
        assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
        bl.promote();
        assertEquals( PromotionLevel.BUILT, bl.getPromotionLevel( false ) );
    }

    @Test
    @ClearCaseUniqueVobName( name = "bl-dyn-test-demote" )
    public void testDemote() throws Exception {
        Baseline bl = ccenv.context.baselines.get( "model-2" ).load();

        assertNotNull( bl );
        assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
        bl.demote();
        assertEquals( PromotionLevel.REJECTED, bl.getPromotionLevel( false ) );
    }


}
