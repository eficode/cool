package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.execute.AbnormalProcessTerminationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.TestCase;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { Baseline.class, Cleartool.class } )
public class StreamUnitTest extends TestCase {
	
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
}
