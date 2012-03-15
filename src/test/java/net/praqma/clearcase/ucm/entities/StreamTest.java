package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class StreamTest {
    private static Appender app;
	
	@BeforeClass
	public static void startup() {
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
        app = new ConsoleAppender();
        app.setMinimumLevel( LogLevel.DEBUG );
        Logger.addAppender( app );
	}
	
    @AfterClass
    public static void end() {
        Logger.removeAppender( app ); 
       }

}
