package net.praqma.clearcase.test.unit;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class VersionTest {
	
	private static Logger logger = Logger.getLogger();
	
	private static Appender app;

	@BeforeClass
	public static void logger() {
		app = new ConsoleAppender();
		app.setMinimumLevel( LogLevel.DEBUG );
		Logger.addAppender( app );
	}
	
	@AfterClass
	public static void loggerAfter() {
		Logger.removeAppender( app );
	}
	
	@Test
	public void getVersion() throws ClearCaseException {
		String version = "c:\\code\\lib\\common.h@@\\main\\int\\1";
		
		Version v = Version.getVersion( version );
		
		assertThat( v.getRevision(), is( 1 ) );
		assertThat( v.getVersion(), is( "\\main\\int\\1" ) );
		assertThat( v.getBranch(), is( "\\main\\int" ) );
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			assertThat( v.getFile(), is( new File( "c:\\code\\lib\\common.h" ) ) );
		}
		
		String version2 = "/code/lib/common.h@@/main/int/1";
		
		Version v2 = Version.getVersion( version2 );
		
		assertThat( v2.getRevision(), is( 1 ) );
		assertThat( v2.getVersion(), is( "/main/int/1" ) );
		assertThat( v2.getBranch(), is( "/main/int" ) );
		if( Cool.getOS().equals( OperatingSystem.UNIX ) ) {
			assertThat( v2.getFile(), is( new File( "/code/lib/common.h" ) ) );
		}
	}
	
	@Test
	public void getVersionNew() throws ClearCaseException {
		String version = "c:\\code\\lib\\common.h@@\\main\\int\\1";
		
		Version v = Version.getVersion( version );
		
		assertThat( v.getRevision(), is( 1 ) );
		assertThat( v.getVersion(), is( "\\main\\int\\1" ) );
		assertThat( v.getBranch(), is( "\\main\\int" ) );
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			assertThat( v.getFile(), is( new File( "c:\\code\\lib\\common.h" ) ) );
		}
		
		String version2 = "/code/lib/common.h@@/main/int/1";
		
		Version v2 = Version.getVersion( version2 );
		
		assertThat( v2.getRevision(), is( 1 ) );
		assertThat( v2.getVersion(), is( "/main/int/1" ) );
		assertThat( v2.getBranch(), is( "/main/int" ) );
		if( Cool.getOS().equals( OperatingSystem.UNIX ) ) {
			assertThat( v2.getFile(), is( new File( "/code/lib/common.h" ) ) );
		}
	}
}
