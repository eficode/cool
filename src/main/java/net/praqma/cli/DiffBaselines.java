/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.cli;

import java.util.List;
import net.praqma.clearcase.api.DiffBl;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.interfaces.Diffable;

/**
 *
 * @author Mads
 */
public class DiffBaselines {
    
	public static void main( String[] args ) throws ClearCaseException {
        //stream:XXXX
        StringDiffable d1 = new StringDiffable(args[0]);
        
        //baseline:XXXX
        StringDiffable d2 = new StringDiffable(args[1]);
        
        DiffBl diffbl = new DiffBl(d1, d2).setVersions(true).setActivities(true);
        List<String> consoleOut = diffbl.execute();
        for(String s : consoleOut) {
            System.out.println(s);
        }
        
	}
    
    public static class StringDiffable implements Diffable {

        public String fqn;
        
        public StringDiffable(String fqn) {
            this.fqn = fqn;
        }
        
        @Override
        public String getFullyQualifiedName() {
            return fqn;
        }
        
    }
}
