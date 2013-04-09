package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.BaseClearCaseTest;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.GetView;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UpdateView;
import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 */
public class GetViewTestBase extends BaseClearCaseTest {

    private static Logger logger = Logger.getLogger( GetViewTestBase.class.getName() );

    public void listFiles( File path ) {
        logger.info( "Listing " + path.getAbsolutePath() );
        String[] files = path.list();
        for( String f : files ) {
            logger.info( " * " + f );
        }
    }

    public void verifyView( GetView gv, String file, String content ) throws IOException, ClearCaseException {
        logger.info( "Verifying " + file );

        gv.validateViewRoot();
        SnapshotView view = gv.get();
        File ccfile = new File( view.getViewRoot(), file );

        String c = FileUtils.readFileToString( ccfile );

        logger.info( "Verifying content" );
        assertThat( c, is( content ) );
    }

    public File createTempPath() throws IOException {
        File path = path = File.createTempFile( "snapshot", "view" );

        if( !path.delete() ) {
            throw new IOException( "Unable to delete dir " + path );
        }

        if( !path.mkdirs() ) {
            throw new IOException( "Unable to make dir " + path );
        }
        System.out.println( "Path: " + path );

        return path;
    }
}
