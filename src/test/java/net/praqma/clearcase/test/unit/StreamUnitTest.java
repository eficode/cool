package net.praqma.clearcase.test.unit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.util.Collections;
import java.util.List;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { Baseline.class, Cleartool.class, Deliver.class } )
public class StreamUnitTest {
	
	static {
		Appender appender = new ConsoleAppender();
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}
	
	@Test
	public void blaStreamExists() throws UnableToInitializeEntityException {
		Stream stream = Stream.get( "stream@\\pvob" );
		
		PowerMockito.mockStatic( Cleartool.class );
		PowerMockito.when( Cleartool.run( Mockito.any( String.class ) ) ).thenReturn( null );
		
		assertTrue( stream.exists() );
	}
	
	@Test
	public void blaStreamNotExists() throws UnableToInitializeEntityException {
		Stream stream = Stream.get( "stream@\\pvob" );
		
		PowerMockito.mockStatic( Cleartool.class );
		PowerMockito.when( Cleartool.run( Mockito.any( String.class ) ) ).thenThrow( new AbnormalProcessTerminationException( "DOESNT", "EXIST" ) );
		
		assertFalse( stream.exists() );
	}

    @Test
    public void getPostedBaselinesEqualityInitial() throws UnableToInitializeEntityException, CleartoolException, UnableToLoadEntityException, UnableToListBaselinesException {

        List<Baseline> baselines = getPostedBaselinesHelper( Project.PromotionLevel.INITIAL );
        assertThat( baselines.size(), is( 1 ) );
    }

    @Test
    public void getPostedBaselinesEqualityTested() throws UnableToInitializeEntityException, CleartoolException, UnableToLoadEntityException, UnableToListBaselinesException {

        List<Baseline> baselines = getPostedBaselinesHelper( Project.PromotionLevel.TESTED );
        assertThat( baselines.size(), is( 1 ) );
    }

    protected List<Baseline> getPostedBaselinesHelper( Project.PromotionLevel level ) throws UnableToInitializeEntityException, CleartoolException, UnableToLoadEntityException, UnableToListBaselinesException {
        Stream stream = Stream.get( "stream@\\pvob" );
        Component component = Component.get( "component@\\pvob" );
        Baseline baseline = Baseline.get( "bl@\\pvob" );
        Baseline spy = Mockito.spy( baseline );

        Mockito.doReturn( level ).when( spy ).getPromotionLevel();
        Mockito.doReturn( Collections.singletonList( spy ) ).when( spy ).getPostedBaselinesFor( component );

        PowerMockito.mockStatic( Deliver.class );
        PowerMockito.when( Deliver.getStatus( stream ) ).thenReturn( "baseline:bl@\\pvob" );

        PowerMockito.mockStatic( Baseline.class );
        PowerMockito.when( Baseline.get( Mockito.anyString() ) ).thenReturn( spy );

        return stream.getPostedBaselines( component, level );

    }
}
