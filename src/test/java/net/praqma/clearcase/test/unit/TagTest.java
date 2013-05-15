package net.praqma.clearcase.test.unit;

import static org.junit.Assert.*;

import java.util.ArrayList;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.TagException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.execute.CmdResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { Baseline.class, Cleartool.class } )
public class TagTest {
	
	private static Logger logger = Logger.getLogger();
	
	static {
		Appender appender = new ConsoleAppender();
		appender.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( appender );
	}

	@Test
	public void testConcreteTest() throws UnableToInitializeEntityException, ClearCaseException {
		Baseline baseline = Baseline.get( "bl1@\\pvob" );
		
		CmdResult result = new CmdResult();
		result.stdoutList = new ArrayList<String>();
		result.stdoutList.add( "" );
		result.stdoutList.add( "" );
		result.stdoutList.add( "   tag@205@\\ccucm22306711_PVOB ->  \"tagtype=ccucm-project-tagged-ccucm22306711&tagid=1&buildstatus=SUCCESS\"" );
		
		PowerMockito.mockStatic( Cleartool.class );
		PowerMockito.when( Cleartool.run( Mockito.any( String.class ) ) ).thenReturn( result );
		
		Tag tag = Tag.getTag( baseline, "ccucm-project-tagged-ccucm22306711", "1", false );
		
		logger.info( "TAG: " + tag );
		
		assertNotNull( tag );
		assertEquals( "SUCCESS", tag.getEntry( "buildstatus" ) );
	}
}
