package net.praqma.clearcase.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.structure.Printer;

public abstract class RegistryCheck {

    /* Matches stranded vobs */
    private static final Pattern rx_stranded = Pattern.compile( "rgy_check: Error: This VOB object is stranded(.*?)$$^", Pattern.MULTILINE | Pattern.DOTALL );
    /* Matches modifiers */
    private static final Pattern rx_modifiers = Pattern.compile( "^\\s*-(\\w+)\\s*=\\s*\"(.*?)\"\\s*$", Pattern.MULTILINE );
    
    public static void main( String[] args ) {

        List<Map<String, String>> vobInfo = checkVobs();
        
        Printer.listMapPrinter( vobInfo );
    }
    
    public static List<Map<String, String>> checkVobs() {
        String cmd = "rgy_check -vobs";
        
        List<Map<String, String>> vobInfo = new ArrayList<Map<String, String>>();

        CmdResult result = CommandLine.getInstance().run( cmd, null, true, true );
        Matcher m = rx_stranded.matcher( result.stdoutBuffer.toString() );
        
        while( m.find() ) {
            Matcher mi = rx_modifiers.matcher( m.group(1) );
            
            Map<String, String> r = new HashMap<String, String>();
            
            while( mi.find() ) {
                r.put( mi.group(1), mi.group(2) );
            }
            
            vobInfo.add( r );
        }
        
        return vobInfo;
    }
}


/*

rgy_check: Error: This VOB object is stranded (it has no tags
in any region):
        -hostname = "pumpkin"
        -local_path = "D:\vobs\JBR_PVOB.2.vbs"
        -vob_replica = "37c67daf.c16645be.b961.9d:d3:e2:50:65:ec"
        -vob_family = "4b4fbe43.27be42de.9c51.4d:5c:53:84:ce:f5"
        -attributes = "sumvob"
 */