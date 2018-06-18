package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Find;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Version;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class FindTest {

    private static Logger logger = Logger.getLogger( FindTest.class.getName() );

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "find", "setup.xml" );

    @Test
    public void test() throws UnableToInitializeEntityException, CleartoolException {

        File path = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );

        Find find = new Find().addPathName( "." ).useUnExtendedNames().setFindAll().print().setViewRoot( path );
        List<Version> versions = find.find();

        logger.fine( versions.toString() );
    }
}
