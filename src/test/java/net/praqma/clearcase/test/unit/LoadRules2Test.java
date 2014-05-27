/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.praqma.clearcase.test.unit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import static junit.framework.Assert.assertNotNull;
import net.praqma.clearcase.ucm.view.SnapshotView;
import org.apache.commons.lang.SystemUtils;
import static org.junit.Assert.assertEquals;
import org.mockito.Mockito;

/**
 *
 * @author Mads
 */
public class LoadRules2Test {
    
    @org.junit.Test
    public void testLoadRules2() throws Exception {
        
        String windows = " -add_loadrules " + "2Cool\\Model "+"2Cool\\Trace "+"2Cool\\Gui "+ "2Cool\\ServerTest";
        String unix = " -add_loadrules " + "2Cool/ServerTest "+"2Cool/Gui "+"2Cool/Model "+ "2Cool/Trace";
        
        String loadModWindows = " -add_loadrules "+ "2Cool\\Gui";
        String loadModUnix = " -add_loadrules "+ "2Cool/Gui";
        
        String expectedLoadRuleMod = SystemUtils.IS_OS_UNIX ? loadModUnix : loadModWindows;
        String expectedLoadRuleString = SystemUtils.IS_OS_UNIX ? unix : windows;  
        //TODO: Implement a test
        SnapshotView.LoadRules2 lr = new SnapshotView.LoadRules2();        
        SnapshotView.LoadRules2 spy = Mockito.spy(lr);
        Mockito.doReturn(mockConsoleOut()).when(spy).getConsoleOutput(Mockito.any(SnapshotView.class));
        String sequence = spy.loadRuleSequence(new SnapshotView(), SnapshotView.Components.ALL);
        
        spy.apply(new SnapshotView(), SnapshotView.Components.ALL);
        assertEquals(expectedLoadRuleString, spy.getLoadRules() );
        
        spy.apply(new SnapshotView(), SnapshotView.Components.MODIFIABLE);        
        assertEquals(expectedLoadRuleMod, spy.getLoadRules());
    } 
    
    public List<String> mockConsoleOut() throws Exception {        
        InputStream is;
        
        if(SystemUtils.IS_OS_LINUX) {
            is = LoadRules2Test.class.getResourceAsStream("catcs_unix.txt");
        } else {
            is = LoadRules2Test.class.getResourceAsStream("catcs.txt");
        }
        
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
