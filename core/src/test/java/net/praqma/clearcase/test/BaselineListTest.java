package net.praqma.clearcase.test;

import java.io.File;
import java.util.Date;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.test.unit.UCMEntity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.filters.AfterBaseline;
import net.praqma.clearcase.ucm.utils.filters.AfterDate;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.debug.Logger;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaselineListTest {
	
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-baselinelist" );
	
	@Test
	public void basic() throws ClearCaseException {
		
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "_System" ).load();
		BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).apply();
		
		logger.info( "List is: " + baselines );
		
		assertEquals( 6, baselines.size() );
	}
	
	@Test
	public void basicLimit() throws ClearCaseException {
		
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "_System" ).load();
		BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).setLimit( 5 ).apply();
		
		logger.info( "List is: " + baselines );
		
		assertEquals( 5, baselines.size() );
	}
	
	@Test
	public void basicAfterBaseline() throws ClearCaseException {
		
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "_System" ).load();
		Baseline baseline = ccenv.context.baselines.get( "model-3" ).load();
		
		BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).addFilter( new AfterBaseline( baseline ) ).apply();
		
		logger.info( "List is: " + baselines );
		
		assertEquals( 2, baselines.size() );
	}
	
	@Test
	public void basicLoad() throws ClearCaseException {
		
		Stream stream = ccenv.context.streams.get( "one_int" ).load();
		Component component = ccenv.context.components.get( "_System" ).load();
		
		BaselineList baselines = new BaselineList( stream, component, PromotionLevel.INITIAL ).load().apply();
		
		logger.info( "List is: " + baselines );
		
		assertEquals( 6, baselines.size() );
		
		assertNotNull( baselines.get( 0 ) );
	}
	
	@Test
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
	
}
