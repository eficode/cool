package net.praqma.clearcase.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.clearcase.util.Utilities;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class VersionTest {

	public static void main( String[] args ) throws UCMException, IOException {
		
		if( args.length < 2 ) {
			System.err.println( "No path given" );
			System.exit( 1 );
		}
		
		File path = new File( args[0] );
		File devpath = new File( path, "dev" );
		File intpath = new File( path, "int" );
		path.mkdirs();
		
		String name = args[1];
		
		File path2 = new File( path, "dev/" + name + "/Test" );
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger);
        
        //File path = new File( System.getProperty("user.dir") );
		
		//File path = new File( "C:/Temp/views2/chw_test_mainline_16_dev/TEST16" );
		File file = new File( path2, "/Model/snade.txt" );
		PVob pvob = new PVob( "\\" + name + "_PVOB" );
		
		System.out.println("Created " + pvob);
		
		String append = System.currentTimeMillis()/1000 + "";
		
		/* Integration stream */
		Stream intStream = UCMEntity.getStream( "test_developement_int", pvob, true );
		System.out.println("Integration stream created");
		SnapshotView intView = SnapshotView.create( intStream, intpath, "wolles_" + append + "_int" );
		
		/**/
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		
		/**/
		Component component = UCMEntity.getComponent( "test", pvob, true );
		
		/* Development */
		SnapshotView devview = Utilities.CreateView( "wolles_test_" + append + "@" + pvob, intStream, baseline, devpath, "wolles_" + append + "dev" );
		System.out.println("Development stream + view created");
		devview.Update( true, true, true, false, COMP.MODIFIABLE, null );
		System.out.println("Development view updated");
		
		file.getParentFile().mkdirs();
		file.createNewFile();
		
		Activity activity = Activity.create( "wolles_" + append, pvob, true, null, devpath );
		
		Version.checkOut( path2, path2 );
		
		Version version = Version.create( file, false, devview );
		//version.checkOut();

		PrintStream ps;
		try {
			ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(file, true) ) );
			ps.println( append );
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//version.checkIn();
		
		Version.checkIn( path2, false, path2 );
		
		Baseline.create( "wolles_baseline_" + append, component, devpath, true, true );
		
	}

}
