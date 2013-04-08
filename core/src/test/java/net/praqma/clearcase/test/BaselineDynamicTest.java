package net.praqma.clearcase.test;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.util.ExceptionUtils;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaselineDynamicTest extends BaseClearCaseTest {

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-baseline-dynamic" );

	@Test
	public void testSetPromotionLevel() throws Exception {
		Baseline bl = ccenv.context.baselines.get( "model-1" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
		bl.setPromotionLevel( PromotionLevel.RELEASED );
		assertEquals( PromotionLevel.RELEASED, bl.getPromotionLevel( false ) );
	}
}
