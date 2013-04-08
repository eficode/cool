package net.praqma.clearcase.test;

import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.filters.AfterBaseline;
import net.praqma.clearcase.ucm.utils.filters.AfterDate;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.junit.TestDescription;
import net.praqma.util.debug.Logger;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaselineListDynamicTest extends BaseClearCaseTest {
	
	private static Logger logger = Logger.getLogger();

	@Rule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-baselinelist-dynamic" );

	@Test
    @ClearCaseUniqueVobName( name = "basic-date" )
	public void basicDate() throws ClearCaseException {
		
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "_System" ).load();
		
		Date date = new Date();
		
		logger.debug( "Date " + net.praqma.clearcase.ucm.entities.UCMEntity.dateFormatter.format( date ) );
		
		/* We need to make sure the baseline is created at least one second after the date */
		try {
			Thread.sleep( 1000 );
		} catch( InterruptedException e1 ) {
			/* Try anyway */
		}
		
		String viewtag = ccenv.getUniqueName() + "_one_int";
		System.out.println( "VIEW: " + ccenv.context.views.get( viewtag ) );
		File path = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );
		
		System.out.println( "PATH: " + path );
		
		try {
			ccenv.addNewContent( ccenv.context.components.get( "Model" ), path, "test.txt" );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		
		Baseline.create( "new-baseline", ccenv.context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
		
		BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).addFilter( new AfterDate( date ) ).apply();
		
		logger.info( "List is: " + baselines );
		
		assertEquals( 1, baselines.size() );
	}

    @Test
    @ClearCaseUniqueVobName( name = "ensure" )
    @TestDescription( title = "Ensuring a Baseline", text = "If searching after a Baseline, that does not have the same Level as the requirements, no Baselines will be found" )
    public void testEnsure() throws ClearCaseException {
        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        model1.setPromotionLevel( PromotionLevel.BUILT );

        Stream stream = ccenv.context.streams.get( "one_int" ).load();
        Component component = ccenv.context.components.get( "_System" ).load();

        BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).addFilter( new AfterBaseline( model1 ) ).apply();

        logger.info( "List is: " + baselines );

        assertEquals( 5, baselines.size() );
    }
}
