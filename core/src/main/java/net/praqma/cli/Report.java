package net.praqma.cli;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Find;
import net.praqma.clearcase.command.ListType;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private String sep = "; ";
    private File path;
    private int lengthOfPath = 0;
    private boolean showFullPath = false;

    private DateFormat dateFormatter;

    @Override
    public void perform( String[] arguments ) throws Exception {
        Options o = new Options( "1.0.0" );

        Option opath = new Option( "path", "p", false, 1, "The path to report" );
        Option osep = new Option( "separator", "s", false, 1, "The separator to use, default is \",\"" );
        Option oshowfull = new Option( "showFull", "f", false, 0, "Show full view path" );
        Option odateFormat = new Option( "dateFormat", "d", false, 1, "Date format, default is \"yyyy.MM.dd\"" );


        o.setOption( opath );
        o.setOption( osep );

        o.setDefaultOptions();

        o.parse( arguments );

        try {
            o.checkOptions();
        } catch( Exception e ) {
            logger.severe( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( osep.isUsed() ) {
            sep = osep.getString() + " ";
        }

        if( oshowfull.isUsed() ) {
            showFullPath = true;
        }

        if( opath.isUsed() ) {
            path = new File( opath.getString() );
        } else {
            path = new File( System.getProperty( "user.dir" ) );
        }
        lengthOfPath = path.getAbsolutePath().length();
        logger.fine( "Path is " + path.getAbsolutePath() + ", " + lengthOfPath );

        if( odateFormat.isUsed()) {
            dateFormatter = new SimpleDateFormat( odateFormat.getString() );
        } else {
            dateFormatter = new SimpleDateFormat( "yyyy.MM.dd" );
        }

        ListType ls = new ListType().setLocal().setBranchType().setViewRoot( path );
        List<Branch> branches = ls.list();

        Map<File, Entry> map = new HashMap<File, Entry>();

        for( Branch branch : branches ) {
            findBranch( path, branch, map );
        }

        System.out.println( "File" + sep + "Age" + sep + "Type" + sep + "Last user" + sep + "Branch name" + sep + "Date" );

        for( File key : map.keySet() ) {
            System.out.println( map.get( key ).string );
        }
    }

    private void findBranch( File path, Branch branch, Map<File, Entry> map ) throws Exception {
        logger.info( "Processing " + branch );

        Find find = new Find().addPathName( "." ).setFindAll().print().setViewRoot( path ).setVersionQuery( "version(.../" + branch.getName() + "/LATEST)" );

        List<Version> versions = null;
        try {
            versions = find.find();
        } catch( Exception e ) {
            File[] fs = path.listFiles();
            for( File f : fs ) {
                logger.info( f.toString() );
            }

            throw e;
        }

        //List<Entry> rows = new ArrayList<Entry>( versions.size() );

        for( Version v : versions ) {
            StringBuilder sb = new StringBuilder();
            File file = v.getFile();

            Date now = new Date();
            long secs = now.getTime();

            logger.fine( "Version: " + v );
            logger.fine( "Version: " + v.getDate() );
            logger.fine( "Version: " + v.getRevision() );

            /* Get file */
            if( showFullPath ) {
                sb.append( "\"" + v.getFile().getAbsolutePath() + "\"" ).append( sep ); // Name
            } else {
                sb.append( "\"" + v.getFile().getAbsolutePath().substring( lengthOfPath ) + "\"" ).append( sep ); // Name
            }

            /* Get age */
            long age = secs - v.getDate().getTime();
            sb.append( age / ( 1000 * 60 * 60 ) ).append( sep ); // Age

            /* Get type */
            sb.append( v.isDirectory() ? "directory" : "file" ).append( sep ); // Absolute file

            /* Get user */
            sb.append( v.getUser() ).append( sep ); // The user

            /* Get branch name */
            sb.append( branch.getName() ).append( sep ); // The branch

            /* Get date */
            sb.append( dateFormatter.format( v.getDate() ) );

            /* Put to map */
            if( map.containsKey( file ) ) {
                if( map.get( file ).age < age ) {
                    map.put( file, new Entry( sb.toString(), age ) );
                }
            } else {
                map.put( file, new Entry( sb.toString(), age ) );
            }
        }
    }

    public class Entry {
        String string;
        long age;
        public Entry( String string, long age ) {
            this.string = string;
            this.age = age;
        }
    }
}
