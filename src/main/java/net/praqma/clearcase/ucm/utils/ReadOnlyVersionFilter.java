/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.praqma.clearcase.ucm.utils;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ucm.entities.Version;

/**
 *
 * @author Mads
 */
@SuppressFBWarnings("")
public class ReadOnlyVersionFilter extends VersionFilter implements Serializable {
    
    private static final Logger log = Logger.getLogger(ReadOnlyVersionFilter.class.getName());
    
    public final File view;
    public final List<String> readOnlyLoadLines;
 
    /**
     * A load line in unix looks like this: /vobs/myCompA 
     * A load line in windows looks like this: \vobfolder\myCompA
     * This filter will discard changes to files where the path to the changed file matches [viewroot][path seperator][readOnlyLoadLine]
     * @param view The view root ({@link File}
     * @param readOnlyLoadLines The load paths, they are relative to the view you're standing in.
     */
    public ReadOnlyVersionFilter(File view, List<String> readOnlyLoadLines) {
        this.view = view;
        this.readOnlyLoadLines = readOnlyLoadLines;
    }
    
    @Override
    public int filter(VersionList versions) {
        log.fine(String.format( "In ReadOnlyVersionFilter, View = %s, ReadOnlyComponents = %s", view, readOnlyLoadLines) );
        log.fine(String.format( "%s versions before ReadOnlyVersionFilter", versions.size()) );
        
        int filterCount = 0;
        Iterator<Version> vers = versions.iterator();
        while(vers.hasNext()) {
            
            Version curVer = vers.next();
            log.fine("Checking version " + curVer.getFullyQualifiedName());
            for(String readOnlyComponent : readOnlyLoadLines) {
                File filePathToTest = new File(view, readOnlyComponent);                
                log.fine( String.format( "Matching %s to see if it begins with %s", curVer.getFile().getAbsolutePath(), filePathToTest.getAbsolutePath()) );
                
                if(curVer.getFile().getAbsolutePath().startsWith(filePathToTest.getAbsolutePath())) {
                    log.fine( String.format( "Removing version %s as it is read only", curVer.getFullyQualifiedName() ) ) ;
                    vers.remove();
                    filterCount++;
                }
            }
        }
        log.fine(String.format( "%s versions after ReadOnlyVersionFilter", versions.size()) );
        return filterCount;
    }
    
}
