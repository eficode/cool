package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.util.test.junit.LoggingRule;
import org.junit.ClassRule;
import org.junit.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author cwolfgang
 */
public class FB9017 {

    @Rule
    public static ClearCaseRule ccenv = new ClearCaseRule( "FB9017", "setup-no-baselines.xml" );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" );

    //@Test
    @ClearCaseUniqueVobName( name = "rødgrødmedfløde" )
    public void test01() throws ClearCaseException, IOException {
        Component model = ccenv.context.components.get( "Model" );
        Stream stream = ccenv.context.streams.get( "one_int" );
        File path = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );
        //ccenv.addNewContent( model, path, "thefile.txt" );
        String filename = "thefile.txt";
        createFile( filename, path, "yes", false );

        Activity.create( "sødebær", stream, ccenv.getPVob(), true, null, "Jæns æder øl og bær, ækelt", path );

        Version.addToSourceControl( new File( filename ), path, null, true );
        Baseline.create( "sødræv", model, path, Baseline.LabelBehaviour.INCREMENTAL, false );
    }

    protected File createFile( String filename, File path, String content, boolean append ) throws IOException {
        File newFile = new File( path, filename );
        if( !newFile.exists() ) {
            newFile.createNewFile();
        }

        if( content != null ) {
            FileWriter fw = null;
            try {
                fw = new FileWriter( newFile, append );
                fw.write( content );
            } finally {
                fw.close();
            }
        }

        return newFile;
    }
}
