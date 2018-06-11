package net.praqma.clearcase.test.unit;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.VersionList;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VersionListTest {
	
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
	
	private static final String qfs = System.getProperty( "file.separator" );

	@Test
	public void test() throws ClearCaseException {
		List<Version> versions = new ArrayList<Version>();
		
		String v1 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "int" + qfs + "1";
		versions.add( Version.getVersion( v1 ) );
		
		String v2 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "int" + qfs + "2";
		versions.add( Version.getVersion( v2 ) );
		
		String v3 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "dev" + qfs + "5";
		versions.add( Version.getVersion( v3 ) );
		
		String v4 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "int" + qfs + "9";
		Version four = Version.getVersion( v4 );
		versions.add( four );
		
		String v5 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "int" + qfs + "6";
		versions.add( Version.getVersion( v5 ) );
		
		String v6 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "dev" + qfs + "3";
		versions.add( Version.getVersion( v6 ) );
		
		String v7 = "code" + qfs + "lib" + qfs + "common.h@@" + qfs + "main" + qfs + "dev" + qfs + "8";
		Version seven = Version.getVersion( v7 );
		versions.add( seven );
		
		VersionList vlist = new VersionList( versions );
		
		VersionList nvlist = vlist.getLatest();
		
		logger.debug( "LIST: " + nvlist );
        
		assertThat( nvlist.size(), is( 2 ) );
        assertThat ( nvlist, hasItems(seven, four) );
	}
}
