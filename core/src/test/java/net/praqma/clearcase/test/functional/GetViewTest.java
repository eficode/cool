package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.Rebase;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.GetView;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UpdateView;
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
public class GetViewTest extends GetViewTestBase {

    private static Logger logger = Logger.getLogger( GetViewTest.class.getName() );

    @ClassRule
    public static ClearCaseRule ccenv = new ClearCaseRule( "cool-GetView" );

    @Test
    public void basicCreate() throws IOException, ClearCaseException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG1";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        Stream container = Stream.create( oneInt, "container1", true, model1 );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        listFiles( view.getViewRoot() );

        verifyView( gv, ccenv.getUniqueName() + "/Model/model.h", "#1" );
    }

    @Test
    public void basicGet() throws IOException, ClearCaseException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG2a";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        Stream container = Stream.create( oneInt, "container2a", true, model1 );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        /* Verify first */
        listFiles( view.getViewRoot() );
        verifyView( gv, ccenv.getUniqueName() + "/Model/model.h", "#1" );

        /* Verify second */
        GetView gv2 = new GetView( path, viewTag );
        verifyView( gv2, ccenv.getUniqueName() + "/Model/model.h", "#1" );
    }

    @Test
    public void advancedGet() throws IOException, ClearCaseException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG2b";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );
        Baseline model2 = ccenv.context.baselines.get( "model-2" );

        Stream container = Stream.create( oneInt, "container2b", true, model1 );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        /* Verify first */
        listFiles( view.getViewRoot() );
        verifyView( gv, ccenv.getUniqueName() + "/Model/model.h", "#1" );

        new Rebase( container ).addBaseline( model2 ).rebase( true );

        /* Verify second */
        GetView gv2 = new GetView( path, viewTag );
        verifyView( gv2, ccenv.getUniqueName() + "/Model/model.h", "#1#2" );
    }

    @Test( expected = IllegalStateException.class )
    public void testNonExistent() throws IOException, ClearCaseException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG3";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        Stream container = Stream.create( oneInt, "container3", true, model1 );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        /* Verify first */
        listFiles( view.getViewRoot() );
        verifyView( gv, ccenv.getUniqueName() + "/Model/model.h", "#1" );

        /* Verify second */
        GetView gv2 = new GetView( new File( path.getParent(), "98u2n918u2n9831u2n3981nu23981u2398/hahahaha" ), viewTag );
        gv2.get();
    }

    @Test
    public void change() throws IOException, ClearCaseException {
        File path = createTempPath();
        String viewTag = ccenv.getUniqueName() + "_TAG4";
        String viewTag2 = ccenv.getUniqueName() + "_TAG4_2";

        Stream oneInt = ccenv.context.streams.get( "one_int" );
        Baseline model1 = ccenv.context.baselines.get( "model-1" );

        Stream container = Stream.create( oneInt, "container4", true, model1 );

        GetView gv = new GetView( path, viewTag ).createIfAbsent().setStream( container );
        SnapshotView view = gv.get();

        SnapshotView.LoadRules lr = new SnapshotView.LoadRules( view, SnapshotView.Components.ALL );
        new UpdateView( view ).setLoadRules( lr ).update();

        /* Verify first */
        listFiles( view.getViewRoot() );
        verifyView( gv, ccenv.getUniqueName() + "/Model/model.h", "#1" );

        /* Verify second */
        GetView gv2 = new GetView( path, viewTag2 );
        gv2.get();
        //verifyView( gv2, ccenv.getUniqueName() + "/Model/model.h", "#1" );
    }
}
