package net.praqma.clearcase.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class Utilities {
	
	public static void main( String[] args ) throws UCMException {
		
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		/* Setup the logger */
        Logger logger = PraqmaLogger.getLogger(false);
        logger.subscribeAll();
        logger.setLocalLog( new File( "versiontest.log") );
        Cool.setLogger(logger);
		
		PVob pvob = new PVob( "\\" + args[1] + "_PVOB" );
		Stream intStream = UCMEntity.getStream( "Development_int", pvob, true );
		Baseline baseline = UCMEntity.getBaseline( "Structure_1_0", pvob, true );
		CreateView("wolles_new_view", intStream, baseline, new File( args[0] ), "supercooltag" );
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
		SnapshotView sv = SnapshotView.Create( devStream, viewroot, viewtag );
		sv.Update( true, true, true, false, COMP.MODIFIABLE, null );
		
		return sv;
	}
	
	public static void addToFile( Version version, String baselineName, Component component, String text ) throws UCMException {
		version.checkOut();

		PrintStream ps;
		try {
			ps = new PrintStream( new BufferedOutputStream(new FileOutputStream(version.getVersion(), true) ) );
			ps.println( text );
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		version.checkIn();
		
		Baseline.create( baselineName, component, version.getVersion().getParentFile(), true, true );
	}
}
