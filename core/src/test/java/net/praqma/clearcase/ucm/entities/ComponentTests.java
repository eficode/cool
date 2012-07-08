package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

public class ComponentTests {

    private static Logger logger = Logger.getLogger();
    private static Appender myLogAppender = new ConsoleAppender();
    
    Component model1, model2, client1, emptyComponent, nullComponent;
    Component emptyComponentSpy, nullComponentSpy;
    int model1HashCode;
    
    @AfterClass
    public static void clean() {
        logger.debug("AfterClass called");
        Logger.removeAppender(myLogAppender);
    }
    
    @BeforeClass
    public static void setup() {
        myLogAppender.setMinimumLevel(Logger.LogLevel.DEBUG);
        // Enable logger to get debug output from each test to console
        Logger.addAppender(myLogAppender); 
        
        logger.debug("BeforeClass done.");
    }
    
    @Before
    public void initialize() {
        try {
            model1 = Component.get("component:Model@\\Test_PVOB");
            model2 = Component.get("component:Model@\\Test_PVOB");
            client1 = Component.get("component:Client@\\Test_PVOB");
            emptyComponent = Component.get("component:empty@\\Test_PVOB");

            model1HashCode = model1.hashCode();

            
            emptyComponentSpy = PowerMockito.spy(emptyComponent);
            
            PowerMockito.doReturn("").when(emptyComponentSpy).getFullyQualifiedName();

        } catch (UnableToInitializeEntityException e) {
            assertTrue("Initialization of test objects failed", false);
        }
        catch (Throwable e)
        {
            logger.debug(e);
        }
        logger.debug("Initialize done");
    }

    @Test
    public void HashCode_equal1() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model1.hashCode()=" + model1.hashCode());
        assertTrue(
                "model1.hashCode()=" + model1.hashCode()
                + ", model1.hashCode()=" + model1.hashCode(),
                model1.hashCode() == model1.hashCode());
    }

    @Test
    public void HashCode_equal2() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model1HashCode (the saved one)=" + model1HashCode);
        assertTrue(
                "model1.hashCode()=" + model1.hashCode()
                + ", model1HashCode (the saved one)=" + model1HashCode, 
                model1.hashCode() == model1HashCode);
    }

    @Test
    public void HashCode_equal3() {
        logger.debug("model2.hashCode()=" + model2.hashCode()
                + ", model1HashCode (the saved one):" + model1HashCode);
        assertTrue(model2.hashCode() == model1HashCode);
    }

    @Test
    public void HashCode_equal4() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model2.hashCode()=" + model2.hashCode());
        assertTrue(
                "model1.hashCode()=" + model1.hashCode()
                + ", model2.hashCode()=" + model2.hashCode(), 
                model1.hashCode() == model2.hashCode());
    }

    @Test
    public void HashCode_notEqual1() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model2.hashCode()=" + model2.hashCode());
        assertTrue("x43", model1.hashCode() == model2.hashCode());
    }

    @Test
    public void HashCode_notEqual2() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", client1.hashCode()=" + client1.hashCode());
        assertFalse(
                "model1.hashCode()=" + model1.hashCode()
                + ", client1.hashCode()=" + client1.hashCode(), 
                model1.hashCode() == client1.hashCode());
    }

    @Test
    public void HashCode_notEqualEmptyFDQNname() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode());
        assertFalse(
                "model1.hashCode()=" + model1.hashCode()
                + ", emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode(), 
                model1.hashCode() == emptyComponentSpy.hashCode());
    }
    
    //@Test
    public void HashCode_EmptyFDQNname() {

        logger.debug("emptyComponent.getFullyQualifiedName()=" + emptyComponent.getFullyQualifiedName());
        logger.debug("emptyComponent.getFullyQualifiedName().hashCode()=" + emptyComponent.getFullyQualifiedName().hashCode());
        logger.debug("emptyComponentSpy.getFullyQualifiedName()=" + emptyComponentSpy.getFullyQualifiedName());
        logger.debug("emptyComponentSpy.getFullyQualifiedName().hashCode()=" + emptyComponentSpy.getFullyQualifiedName().hashCode());
        
        
        logger.debug("emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode()
                + ", \"\".hashCode()=" + "".hashCode());
        assertTrue(
                "emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode()
                + ", \"\".hashCode()=" + "".hashCode(), 
                emptyComponentSpy.hashCode() == "".hashCode());
    }
}
