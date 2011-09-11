package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.changeset.ChangeSet;
import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class Difference {
	private static Logger logger = Logger.getLogger();
	
	public static void main(String[] args) throws IOException, UCMException {
		
        Options o = new Options( "1.2.3" );

        Option osource = new Option( "source", "s", true, 1, "Source entity" );
        Option otarget = new Option( "target", "t", false, 1, "Target entity" );
        Option oview = new Option( "view", "w", false, 1, "View context" );
        
        o.setOption( osource );
        o.setOption( otarget );
        o.setOption( oview );
        
        o.setDefaultOptions();
        
        o.parse( args );
        
        logger.toStdOut( true );
        
        try {
            o.checkOptions();
        } catch( Exception e ) {
        	logger.error( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( o.isVerbose() ) {
        	logger.setMinLogLevel( LogLevel.DEBUG );
        } else {
        	logger.setMinLogLevel( LogLevel.INFO );
        }
        
		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		net.praqma.util.debug.PraqmaLogger.Logger plogger = net.praqma.util.debug.PraqmaLogger.getLogger();
		plogger.subscribeAll();
		plogger.setLocalLog(new File("diff.log"));
	       

        UCMEntity e1 = UCMEntity.getEntity( osource.getString() );
        
        if( o.isVerbose() ) {
        	logger.debug( "Entity 1: " + e1.getFullyQualifiedName() );
        }
        
        UCMEntity e2 = null;
        if( otarget.isUsed() ) {
        	e2 = UCMEntity.getEntity( otarget.getString() );
        }
        
        if( o.isVerbose() ) {
        	logger.debug( "Entity 2: " + ( e2 != null ? e2.getFullyQualifiedName() : "NULL" ) );
        }
        
        File view = null;
        if( oview.isUsed()) {
        	view = new File( oview.getString() );
        } else {
        	view = new File( System.getProperty("user.dir") );
        }
        
        if( o.isVerbose() ) {
        	logger.debug( "View context: " + view.getAbsolutePath() );
        }
        
        ChangeSet2 changeset = ChangeSet2.getChangeSet( (Diffable)e1, (Diffable)e2, view );

        System.out.println( changeset.toString() );
	}
}
