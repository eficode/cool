/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.praqma.clearcase.test.unit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.praqma.clearcase.ucm.view.SnapshotView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Mads
 */
public class LoadRules2Test {
    
    @Test
    public void testLoadRules2() throws Exception {
        
        String[] windowsLinesArrayAll = new String[]{
            "2Cool\\Model",
            "2Cool\\Trace",
            "2Cool\\Gui",
            "2Cool\\ServerTest"
        };
        
        String[] windowsLinesArrayMod = new String[]{
            "2Cool\\Gui",
        };
        
        
        String windows = " -add_loadrules " + "2Cool\\Model "+"2Cool\\Trace "+"2Cool\\Gui "+ "2Cool\\ServerTest";
        String unix = " -add_loadrules " + "vobs/2Cool/ServerTest "+"vobs/2Cool/Gui "+"vobs/2Cool/Model "+ "vobs/2Cool/Trace";
        
        String loadModWindows = " -add_loadrules "+ "2Cool\\Gui";
        String loadModUnix = " -add_loadrules "+ "/vobs/2Cool/Gui";
        
        String linuxWindowsSwitch = SystemUtils.IS_OS_UNIX ? "catcs_unix.txt" : "catcs.txt";
        
        String expectedLoadRuleMod = SystemUtils.IS_OS_UNIX ? loadModUnix : loadModWindows;
        String expectedLoadRuleString = SystemUtils.IS_OS_UNIX ? unix : windows;  
        //TODO: Implement a test
        SnapshotView.LoadRules2 lr = new SnapshotView.LoadRules2();        
        SnapshotView.LoadRules2 spy = Mockito.spy(lr);
        Mockito.doReturn(mockConsoleOut(linuxWindowsSwitch)).when(spy).getConsoleOutput(Mockito.any(SnapshotView.class));
        String sequence = spy.loadRuleSequence(new SnapshotView());
        if(!SystemUtils.IS_OS_UNIX) {
            spy.apply(new SnapshotView());
            String mylr = spy.getLoadRules();
            
            for(String line : windowsLinesArrayAll) {
                assertTrue(String.format("Load rule string must contain %s", line), mylr.contains(line)); 
            }

            SnapshotView.LoadRules2 lr2 = new SnapshotView.LoadRules2(SnapshotView.Components.MODIFIABLE);        
            SnapshotView.LoadRules2 spy2 = Mockito.spy(lr2);
            
            Mockito.doReturn(mockConsoleOut(linuxWindowsSwitch)).when(spy2).getConsoleOutput(Mockito.any(SnapshotView.class));
            
            spy2.apply(new SnapshotView());
            String mylrModifiable = spy2.getLoadRules();
            assertEquals(expectedLoadRuleMod, mylrModifiable);
        } else {
            //TODO FIX Unit tests for unix, the ordering is different on unix
            assertTrue(true);
        }
    }
    
    
    @Test
    public void FB11710() throws Exception {        
        
        String[] arrayOfLinesAll = new String[] {
            "appTemplate_RelDocs\\appTemplate_RelDocs",
            "bbRTE_RelDocs\\RTE_RelDocs",
            "bbRTE_Source\\RTE_Source",
            "appTemplate_Tools\\appTemplate_Tools",
            "appTemplate_Dev\\appTemplate_Dev",
            "appTemplate_Release\\appTemplate_Release",
            "rwScript_Tools\\Script_Release",
            "bbRTE_Release\\RTE_Release",
            "rwScript_Tools\\Script_RelDocs", 
            "appTemplate_Source\\appTemplate_Source"            
        };
        String expectedAll = StringUtils.join(new String[] { 
            " -add_loadrules", 
            "appTemplate_RelDocs\\appTemplate_RelDocs",
            "bbRTE_RelDocs\\RTE_RelDocs",
            "bbRTE_Source\\RTE_Source",
            "appTemplate_Tools\\appTemplate_Tools",
            "appTemplate_Dev\\appTemplate_Dev",
            "appTemplate_Release\\appTemplate_Release",
            "rwScript_Tools\\Script_Release",
            "bbRTE_Release\\RTE_Release",
            "rwScript_Tools\\Script_RelDocs", 
            "appTemplate_Source\\appTemplate_Source"
        }, " ");
 
        String[] arrayOfLinesModifiable = new String[] {
            "appTemplate_RelDocs\\appTemplate_RelDocs",
            "appTemplate_Tools\\appTemplate_Tools",            
            "appTemplate_Dev\\appTemplate_Dev",
            "appTemplate_Release\\appTemplate_Release",
            "appTemplate_Source\\appTemplate_Source",          
        
        };       
        String modifiableLoadLines = StringUtils.join(new String[] { 
            " -add_loadrules", 
            "appTemplate_RelDocs\\appTemplate_RelDocs",
            "appTemplate_Tools\\appTemplate_Tools",            
            "appTemplate_Dev\\appTemplate_Dev",
            "appTemplate_Release\\appTemplate_Release",
            "appTemplate_Source\\appTemplate_Source",            
        }, " ");
        
        SnapshotView.LoadRules2 lr = new SnapshotView.LoadRules2();
        SnapshotView.LoadRules2 lr2 = new SnapshotView.LoadRules2(SnapshotView.Components.MODIFIABLE);
        SnapshotView.LoadRules2 spy = Mockito.spy(lr);
        SnapshotView.LoadRules2 spy2 = Mockito.spy(lr2);
        Mockito.doReturn(mockConsoleOut("ucm-config-spec-with-readonly.txt")).when(spy).getConsoleOutput(Mockito.any(SnapshotView.class));
        Mockito.doReturn(mockConsoleOut("ucm-config-spec-with-readonly.txt")).when(spy2).getConsoleOutput(Mockito.any(SnapshotView.class));
        
        if(SystemUtils.IS_OS_UNIX) {
            assertTrue(true);
        } else {
            SnapshotView view = new SnapshotView();
            spy.apply(view);
            
            String allLr = spy.apply(view).getLoadRules();
            
            for(String sAll : arrayOfLinesAll) {
                assertTrue( String.format( "Load rule must contain %s", sAll), allLr.contains(sAll));            
            }
            
            spy2.apply(view);
            String loads = spy2.getLoadRules();
            for(String s : arrayOfLinesModifiable) {
                assertTrue( String.format( "Load rule must contain %s", s), loads.contains(s));
            }
        }
    }
    
    
    public List<String> mockConsoleOut(String filename) throws Exception {        
        InputStream is;
        is = LoadRules2Test.class.getResourceAsStream(filename);
        
        List<String> lines = new ArrayList<String>();
        try {                
            Scanner scan = new Scanner(is);
            while(scan.hasNextLine()) {
                lines.add(scan.nextLine());
            }

        } finally {
            is.close();
        }
        
        return lines;
    }
}
