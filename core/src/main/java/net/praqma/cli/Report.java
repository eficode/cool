package net.praqma.cli;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Find;
import net.praqma.clearcase.command.ListType;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.io.File;
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

    private String sep = ", ";

    @Override
    public void perform( String[] arguments ) throws Exception {
        Options o = new Options( "1.0.0" );

        Option opath = new Option( "path", "p", true, 1, "The path to report" );
        Option osep = new Option( "separator", "s", false, 1, "The separator to use, default is \",\"" );

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

        File path = new File( opath.getString() );

        ListType ls = new ListType().setLocal().setBranchType().setViewRoot( path );
        List<Branch> branches = ls.list();

        Map<File, Entry> map = new HashMap<File, Entry>();

        for( Branch branch : branches ) {
            findBranch( path, branch, map );
        }

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

            sb.append( "\"" + v.getFile().getAbsolutePath() + "\"" ).append( sep ); // Name
            long age = secs - v.getDate().getTime();
            sb.append( age / ( 1000 * 60 * 60 ) ).append( sep ); // Age
            sb.append( v.isDirectory() ? "directory" : "file" ).append( sep ); // Absolute file
            sb.append( v.getUser() ).append( sep ); // The user
            sb.append( branch.getName() ).append( sep ); // The branch
            sb.append( v.getDate() ); // The creation date?

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
