/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Mads
 */
public class ClearCaseRuleTest {
    
    @Rule
    public ClearCaseRule rule = new ClearCaseRule("content-creation");
    
    @Test
    public void testMultipeCreations() throws Exception {
        Baseline bl1 = rule.createNewDevStreamContents("one_dev");
        Baseline bl2 = rule.createNewDevStreamContents("one_dev");
    }
}
