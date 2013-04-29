package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * @author cwolfgang
 */
public class FB9017 {

    @Rule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB9017", "setup-no-baselines.xml" );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" );

    @Test
    @ClearCaseUniqueVobName( name = "rødgrødmedfløde" )
    public void test01() throws ClearCaseException {
        Component model = ccenv.context.components.get( "Model" );
        File path = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );
        ccenv.addNewContent( model, path, "thefile.txt" );
        Baseline.create( "sødræv", model, path, Baseline.LabelBehaviour.INCREMENTAL, false );
    }
}
