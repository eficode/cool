package net.praqma.cli;

import net.praqma.clearcase.Find;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Report extends CLI {
    private static Logger logger = Logger.getLogger( Report.class.getName() );

    public static void main( String[] args ) throws Exception {
        Report s = new Report();
        s.perform( args );
    }

    @Override
    public void perform( String[] arguments ) throws Exception {
        Options o = new Options( "1.0.0" );

        Option opath = new Option( "path", "p", true, 1, "The path to report" );

        o.setOption( opath );

        o.setDefaultOptions();

        o.parse( arguments );

        try {
            o.checkOptions();
        } catch( Exception e ) {
            logger.severe( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        File path = new File( opath.getString() );
        Find find = new Find().addPathName( "." ).useUnExtendedNames().setFindAll().print().setViewRoot( path );
        List<Version> versions = find.find();

        List<String> rows = new ArrayList<String>( versions.size() );

        for( Version v : versions ) {
            StringBuilder sb = new StringBuilder();
            File file = v.getFile();

            Date now = new Date();
            long secs = now.getTime();

            sb.append( v.getFile().getAbsolutePath() ).append( ", " ); // Name
            sb.append( secs - v.getDate().getTime() ).append( ", " ); // Age
            sb.append( file.isDirectory() ).append( ", " ); // Absolute file
            sb.append( v.getUser() ).append( ", " ); // The user
            sb.append( v.getDate() ); // The creation date?

            rows.add( sb.toString() );
        }

        logger.info( "Rows: " + rows );
    }
}
