package net.praqma.clearcase.util;

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
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class Utilities {
	
	public static void main( String[] args ) throws UCMException, IOException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger);
        
        File view = new File( args[0] );
        String name = args[1];
		String viewtag = "supercooltag";
		PVob pvob = new PVob( "\\" + name + "_PVOB" );

		Stream intStream = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		Component component = UCMEntity.getComponent( "Model", pvob, true );
		
		SnapshotView sview = null;
		
		if( !UCMView.viewExists( viewtag ) ) {
			sview = CreateView("stream:wolles_dev" + "@" + pvob, intStream, baseline, view, viewtag );
		} else {
			sview = UCMView.getSnapshotView(view);
		}		
		
		File devpath = new File( view, name + "/" + component.getShortname() );
		
		Activity activity = Activity.create( null, pvob, true, null, devpath );
		Version.checkOut( devpath, devpath );
		
		File f1 = new File( devpath, "myfile1.txt" );
		f1.createNewFile();
		Version version1 = Version.create( f1, false, sview );
		
		File f2 = new File( devpath, "myfile2.txt" );
		f1.createNewFile();
		Version version2 = Version.create( f2, false, sview );
		
		addToFiles(new Version[]{version1, version2}, "my_super_cool_baseline", component, new String[]{"my1", "my2"}, devpath );
		
		Version.checkIn( devpath, false, devpath );
	}
	
	/**
	 * Creates a development {@link Stream} based of parent and an updated {@link SnapshotView}.
	 * @param name Name of the view
	 * @param parent Parent Stream
	 * @param baseline Baseline offset
	 * @param viewroot The root of the view
	 * @param viewtag The tag of the view
	 * @return Returns the SnapshotView
	 * @throws UCMException
	 */
	public static SnapshotView CreateView( String name, Stream parent, Baseline baseline, File viewroot, String viewtag ) throws UCMException {
		
		Stream devStream = Stream.create( parent, name, false, baseline );
		SnapshotView sv = SnapshotView.create( devStream, viewroot, viewtag );
		sv.Update( true, true, true, false, COMP.MODIFIABLE, null );
		
		return sv;
	}
	
	public static void addToFiles( Version[] version, String baselineName, Component component, String[] text, File view ) throws UCMException {
		
		for( int i = 0 ; i < version.length ; i++ ) {
			//version[i].checkOut();
	
			PrintStream ps;
			try {
				ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(version[i].getVersion(), true) ) );
				ps.println( text[i] );
				ps.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			//version[i].checkIn();
		}
		
		
		
		Baseline.create( baselineName, component, view, true, true );
	}
}
