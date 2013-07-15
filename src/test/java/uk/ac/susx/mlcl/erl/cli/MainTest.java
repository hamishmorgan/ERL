/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.erl.cli;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.erl.test.AbstractTest;
import uk.ac.susx.mlcl.erl.test.ExitTrapper;

/**
 *
 *
 *
 * @author hiam20
 */
public class MainTest extends AbstractTest {

    @Test
    public void testMain_NoArgs() throws Exception {
        final String[] args = {};

        try {
            ExitTrapper.enable();
            try {
                Main.main(args);
                Assert.fail("Expecting command line error because no arguments where provided.");
            } catch (ExitTrapper.ExitException ex) {
                Assert.assertEquals(-1, ex.getStatus());
                // success 
            }
        } finally {
            ExitTrapper.disable();
        }
    }

    @Test
    public void testMain_Simple_1() throws Exception {
        final String[] args = {
                "-o", "-",
                "example_data/point_samson.txt"
        };

        try {
            ExitTrapper.enable();
            Main.main(args);
        } finally {
            ExitTrapper.disable();
        }
    }

    @Test
    public void testMain_Simple_2() throws Exception {

        final String[] args = {
                "-o", "-",
                "example_data/panama.txt"
        };

        try {
            ExitTrapper.enable();
            Main.main(args);
        } finally {
            ExitTrapper.disable();
        }
    }

}
