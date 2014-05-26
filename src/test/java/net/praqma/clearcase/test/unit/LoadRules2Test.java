/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.praqma.clearcase.test.unit;

import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static junit.framework.Assert.assertNotNull;
import net.praqma.clearcase.ucm.view.SnapshotView;
import static org.junit.Assert.assertTrue;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

/**
 *
 * @author Mads
 */
public class LoadRules2Test {
    
    @org.junit.Test
    public void testLoadRules2() throws Exception {
        //TODO: Implement a test
        SnapshotView.LoadRules2 lr = new SnapshotView.LoadRules2();        
        SnapshotView.LoadRules2 spy = Mockito.spy(lr);
        Mockito.doReturn(mockConsoleOut()).when(spy).getConsoleOutput(Mockito.any(SnapshotView.class));
        String sequence = spy.loadRuleSequence(new SnapshotView(), SnapshotView.Components.ALL);
        
        assertNotNull(String.format( "The current config spec is: %s", sequence), sequence);        
        
    } 
    
    public List<String> mockConsoleOut() throws Exception {
        InputStream is = LoadRules2Test.class.getResourceAsStream("catcs.txt");
        List<String> lines = new ArrayList<String>();
        try {                
            Scanner scan = new Scanner(is);
            System.out.printf("We're here");
            while(scan.hasNextLine()) {
                System.out.println("In line");
                lines.add(scan.nextLine());
            }

        } finally {
            is.close();
        }
        
        return lines;
    }
}
