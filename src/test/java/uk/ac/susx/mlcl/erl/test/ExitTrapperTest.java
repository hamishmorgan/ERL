/*
 * Copyright (c) 2010-2013, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.erl.test;

import java.security.Permission;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ExitTrapper utility.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExitTrapperTest extends AbstractTest {

    @Before
    public void setUp() {
        System.setSecurityManager(null);
    }

    @After
    public void tearDown() {
        System.setSecurityManager(null);
    }

    @Test(expected = SecurityException.class)
    public void testEnable_Failure() {
        final AtomicBoolean block = new AtomicBoolean(true);
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (block.get() && perm.getName().equals("setSecurityManager")) {
                        throw new SecurityException();
                    }
                }
            });
            ExitTrapper.enable();
        } finally {
            block.set(false);
            System.setSecurityManager(null);
        }
    }

    @Test
    public void testIsUsable_True() {
        System.setSecurityManager(null);
        boolean expected = true;
        boolean actual = ExitTrapper.isUseable();
        assertEquals(expected, actual);
    }

    @Test
    public void testIsUsable_False() {
        final AtomicBoolean block = new AtomicBoolean(true);
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (block.get() && perm.getName().equals("setSecurityManager")) {
                        throw new SecurityException();
                    }
                }
            });
            boolean expected = false;
            boolean actual = ExitTrapper.isUseable();
            assertEquals(expected, actual);
        } finally {
            block.set(false);
            System.setSecurityManager(null);
        }

    }

    /**
     * Test of enableExistTrapping method, of class ExitTrapper.
     */
    @Test(expected = ExitTrapper.ExitException.class)
    public void testEnableExistTrapping() {
        ExitTrapper.enable();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isEnabled());
        System.exit(Integer.MIN_VALUE);
    }

    /**
     * Test of disableExitTrapping method, of class ExitTrapper.
     */
    @Test
    public void testDisableExitTrapping() {
        try {
            if (!ExitTrapper.isEnabled()) {
                ExitTrapper.enable();
            }

            assertTrue(ExitTrapper.isEnabled());
            ExitTrapper.disable();
            assertFalse(ExitTrapper.isEnabled());

            assertFalse("Exist trapping should be disabled.",
                        ExitTrapper.isEnabled());

        } finally {
            if (ExitTrapper.isEnabled()) {
                ExitTrapper.disable();
            }
        }
    }

    /**
     * Test of toggleExitTrapping method, of class ExitTrapper.
     */
    @Test(expected = ExitTrapper.ExitException.class)
    public void testToggleExitTrapping() {
        ExitTrapper.toggle();
        System.exit(1);
    }

    /**
     * Test of isExitTrappingEnabled method, of class ExitTrapper.
     */
    @Test
    public void testIsExitTrappingEnabled() {
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isEnabled());
        ExitTrapper.enable();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isEnabled());
        try {
            System.exit(0);
            fail("System.exit should have been trapped.");
        } catch (ExitTrapper.ExitException ex) {
            assertEquals(0, ex.getStatus());
            //pass
        }

        ExitTrapper.disable();
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isEnabled());
        ExitTrapper.toggle();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isEnabled());
        try {
            System.exit(Integer.MAX_VALUE);
            fail("System.exit should have been trapped.");
        } catch (ExitTrapper.ExitException ex) {
            assertEquals(Integer.MAX_VALUE, ex.getStatus());
            //pass
        }

        ExitTrapper.toggle();
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isEnabled());
    }

    /**
     * Test of ExitException subclass of ExitTrapper.
     */
    @Test
    public void testExitException() {
        ExitTrapper.enable();
        int[] codes = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1,
            -Integer.MAX_VALUE, Integer.MAX_VALUE - 1};
        for (int code : codes) {
            try {
                System.exit(code);
                fail("System.exit(" + code + ") should have been trapped.");
            } catch (ExitTrapper.ExitException ex) {
                assertEquals(code, ex.getStatus());
                //pass
            }
        }
    }
}
