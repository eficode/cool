package net.praqma.clearcase.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.structure.Printer;

public abstract class RegistryCheck {

    //private static final Pattern rx_stranded = Pattern.compile( "rgy_check: Error: This VOB object is stranded(.*?)\\n\\s*\\n", Pattern.MULTILINE );
    //private static final Pattern rx_stranded = Pattern.compile( "rgy_check: Error: This VOB object is stranded(.*?)$\\s$", Pattern.MULTILINE | Pattern.DOTALL );
    private static final Pattern rx_stranded = Pattern.compile( "rgy_check: Error: This VOB object is stranded(.*?)$$^", Pattern.MULTILINE | Pattern.DOTALL );
    
    //private static final Pattern rx_modifiers = Pattern.compile( "^\\s*(\\w+)\\s*=\\s*\"(.*?)\"\\s*$" );
    private static final Pattern rx_modifiers = Pattern.compile( "^\\s*-(\\w+)\\s*=\\s*\"(.*?)\"\\s*$", Pattern.MULTILINE );
    
    public static void main( String[] args ) {
        String s = "rgy_check: Error: This VOB object is stranded (it has no tags\nin any region):\n" +
        		   "        -hostname = \"pumpkin\"\n" +
        		   "        -snade = \"noget med spaces i\"\n";
        s += "\n";
        s += "rgy_check: Error: This VOB object is stranded (it has no tags\nin any region):\n        -hostname = \"pumpkin2\"\n";
        
        
        s = "rgy_check: Error: This VOB object is stranded (it has no tags\n"+
"in any region):\n" + 
"    -hostname = \"pumpkin\"\n" + 
"    -local_path = \"D:\\vobs\\JBR_PVOB.1.vbs\"\n" + 
"    -vob_replica = \"b43ae333.901a428e.a199.e4:2c:2d:aa:d0:56\"\n" + 
"    -vob_family = \"fe97e5c0.e70f4209.b632.05:1b:95:25:27:4d\"\n" + 
"    -attributes = \"sumvob\"\n" + 
"\n" + 
"rgy_check: Error: This VOB object is stranded (it has no tags\n" + 
"in any region):\n" + 
"    -hostname = \"pumpkin\"\n" + 
"    -local_path = \"D:\\vobs\\Cool_PVOB.1.vbs\"\n" + 
"    -vob_replica = \"036bfccb.42bb4223.959c.26:57:08:31:10:ee\"\n" + 
"    -vob_family = \"0891295d.05364911.ae5d.78:ff:d5:21:70:c9\"\n" + 
"    -attributes = \"sumvob\"\n" + 
"\n" + 
"rgy_check: Error: This VOB object is stranded (it has no tags\n" + 
"in any region):\n" + 
"    -hostname = \"pumpkin\"\n" + 
"    -local_path = \"D:\\vobs\\jbr_vob.1.vbs\"\n" + 
"    -vob_replica = \"018a2735.0e534922.9f90.94:a3:01:2f:cd:f4\"\n" + 
"    -vob_family = \"9204346e.67d34478.8cbb.0c:e1:2d:c7:e9:52\"\n" + 
"\n" + 
"rgy_check: Error: This VOB object is stranded (it has no tags\n" + 
"in any region):\n" + 
"    -hostname = \"pumpkin\"\n" + 
"    -local_path = \"D:\\vobs\\JBR_PVOB.2.vbs\"\n" + 
"    -vob_replica = \"37c67daf.c16645be.b961.9d:d3:e2:50:65:ec\"\n" + 
"    -vob_family = \"4b4fbe43.27be42de.9c51.4d:5c:53:84:ce:f5\"\n" + 
"    -attributes = \"sumvob\"\n" + 
"\n" + 
"rgy_check: Error: 4 total registry errors/inconsistencies detected.";
        
        
        List<Map<String, String>> vobInfo = checkVobs(s);
        
        Printer.listMapPrinter( vobInfo );
    }
    
    public static List<Map<String, String>> checkVobs(String s) {
        String cmd = "rgy_check -vobs";
        
        List<Map<String, String>> vobInfo = new ArrayList<Map<String, String>>();

        //CmdResult result = CommandLine.getInstance().run( cmd, null, true, true );
        //Matcher m = rx_stranded.matcher( result.stdoutBuffer.toString() );
        Matcher m = rx_stranded.matcher( s );
        
        //System.out.println("RESULT: "  + result.stdoutBuffer.toString());
        //System.out.println("RESULT: "  + s);

        
        while( m.find() ) {
            System.out.println("--->" + m.groupCount());
            System.out.println(m.group( 1 ));
            
            Matcher mi = rx_modifiers.matcher( m.group(1) );
            
            Map<String, String> r = new HashMap<String, String>();
            
            while( mi.find() ) {
                //System.out.println("::::" + mi.groupCount());
                
                //System.out.println(mi.group(1) + " = " + mi.group(2));
                
                r.put( mi.group(1), mi.group(2) );
            }
            
            vobInfo.add( r );
        }
        
        System.out.println("done");
        
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