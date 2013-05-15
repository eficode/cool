/**
 * Test package for testing the package with same name.
 */
package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

/**
 * Tests for methods in Component class
 * Use naming like this for each test: methodname_thingsYouTest
 * 
 * Guidelines:
 * - there is logger that can be used to debug, comment in/comment out in 
 * beforeClass to enable/disable
 * - Please print error information in asserts if it fails, not only debug that
 * often will be disabled later.
 * 
 * NOTE: if other tests elsewhere creates a logger and adds an appender, but do
 * not clean it, you will see output (maybee more than once) though you disable it
 * 
 * @author Bue Petersen
 */
public class ComponentTests {

    // Our own logger is used, as this has nice features
    private static Logger logger = Logger.getLogger();
    private static Appender myLogAppender = new ConsoleAppender();
    
    // These objects are used in the tests
    Component model1, model2, client1, emptyComponent, nullComponent;
    Component emptyComponentSpy, nullComponentSpy;
    int model1HashCode;
    
    /**
     * Method called after this class is done, removed the log appender
     */
    @AfterClass
    public static void clean() {
        logger.debug("AfterClass called");
        Logger.removeAppender(myLogAppender);
    }
    
    /**
     * Method called before this class with test - creates a logger 
     * we will use.
     */
    @BeforeClass
    public static void setup() {
        myLogAppender.setMinimumLevel(Logger.LogLevel.DEBUG);
        
        // Enable logger to get debug output from each test to console
        //Logger.addAppender(myLogAppender); 
        
        logger.debug("BeforeClass done.");
    }
    
    /**
     * Called before each @Test annotated method below.
     * Sets up the union of objects we use in the tests
     */
    @Before
    public void initialize() {
        try {
            // initialize real objects
            model1 = Component.get("component:Model@\\Test_PVOB");
            model2 = Component.get("component:Model@\\Test_PVOB");
            client1 = Component.get("component:Client@\\Test_PVOB");
            emptyComponent = Component.get("component:empty@\\Test_PVOB");

            // used for comparison in a test 
            model1HashCode = model1.hashCode();

            
            // The real object above can not be initialized with an empty string
            // therefore we must use the spy from PowerMochito, which is a kind of
            // middle layer on the real object so that we call the getFullyQualifiedName
            // on the spy object we force the method to return something else.
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

    /**
     * Hash should be the same for the same component
     */
    @Test
    public void HashCode_equal1() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model1.hashCode()=" + model1.hashCode());
        assertTrue(
                "model1.hashCode()=" + model1.hashCode()
                + ", model1.hashCode()=" + model1.hashCode(),
                model1.hashCode() == model1.hashCode());
    }

    /**
     * Hash should be the same each time we ash, regardless of context
     */
    @Test
    public void HashCode_equal2() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", model1HashCode (the saved one)=" + model1HashCode);
        assertTrue(
                "model1.hashCode()=" + model1.hashCode()
                + ", model1HashCode (the saved one)=" + model1HashCode, 
                model1.hashCode() == model1HashCode);
    }

    /**
     * Saved hash for component 1 should be the same, as the one from another
     * component 2 if they have same FQDN
     */
    @Test
    public void HashCode_equal3() {
        logger.debug("model2.hashCode()=" + model2.hashCode()
                + ", model1HashCode (the saved one):" + model1HashCode);
        assertTrue(model2.hashCode() == model1HashCode);
    }

    /**
     * Two components with same FQDN should have the same hash?
     */
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
    public void HashCode_notEqual2() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", client1.hashCode()=" + client1.hashCode());
        assertFalse(
                "model1.hashCode()=" + model1.hashCode()
                + ", client1.hashCode()=" + client1.hashCode(), 
                model1.hashCode() == client1.hashCode());
    }

    /**
     * Hashcode of component should not match an component with an empty
     * FQDN.
     * FIXME Test not working - powermochito problem, it does not use the mocked method
     * in the component.hashcode
     */ 
    //@Test
    public void HashCode_notEqualEmptyFDQNname() {
        logger.debug("model1.hashCode()=" + model1.hashCode()
                + ", emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode());
        assertFalse(
                "model1.hashCode()=" + model1.hashCode()
                + ", emptyComponentSpy.hashCode()=" + emptyComponentSpy.hashCode(), 
                model1.hashCode() == emptyComponentSpy.hashCode());
    }
    
    /**
     * String.hashCode return 0 for emtpy string, this means component.hashCode 
     * also does.
     * FIXME Test not working - powermochito problem, it does not use the mocked method
     * in the component.hashcode
     */
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
