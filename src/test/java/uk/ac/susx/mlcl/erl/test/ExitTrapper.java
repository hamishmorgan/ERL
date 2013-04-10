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

import com.google.common.base.Optional;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Signed;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Static utility class that allows calls to {@link System#exit(int) }
 * to be intercepted. This makes direct testing of <tt>main</tt> methods possible, since all calls
 * to {@link System#exit(int) } result in a runtime {@link ExitException}, rather than simply
 * terminating the forked VM (passing the simpleTest).
 *
 * <p>When exit trapping is enabled, the previously installed {@link SecurityManager} will be
 * temporarily encapsulate, over-riding {@link SecurityManager#checkExit(int)}. It will pass all
 * other security related checks to the encapsulated security manager (if there was one), or to the
 * default security manager (when there isn't). When trapping is disabled the previously installed
 * manager (if it exists) will be re-instated.</p>
 *
 * <p>Take care to insure ExitTrapper is disabled when not required, otherwise it's functionality
 * will bleed into other areas of runtime. To insure ExitTrapper is disabled enclosed it in a
 * <tt>try/finally</tt> block as follows:</p>
 * <pre>
 *  try {
 *      ExitTrapper.enable();
 *
 *      // Do trapped stuff here...
 *
 *  } catch(ExitException ex) {
 *      // System.exit() was called
 *      int status = ex.getStatus();
 *      // Handle exception here
 *  }finally {
 *      ExitTrapper.disable();
 *  }
 * </pre>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull()
@ThreadSafe()
public class ExitTrapper {

    /**
     * Logger used by the exit trapper.
     */
    private static final Logger LOG = Logger.getLogger(ExitTrapper.class.getName());

    /**
     * Static utility class can't be constructed.
     */
    private ExitTrapper() {
        throw new AssertionError("Exit trapper is a static utility so should not be instantiated.");
    }

    /**
     * Get whether or not the ExitTrapper can be used (enabled). It can only be used if a
     * SecurityManager has not been set, or if the set SecurityManager does not forbid the
     * "setSecurityManager" permission.
     *
     * @return true if ExitTrapper can be used (enabled), false otherwise
     */
    public static synchronized boolean isUseable() {
        try {
            if (System.getSecurityManager() != null) {
                System.getSecurityManager().checkPermission(
                        new RuntimePermission("setSecurityManager"));
            }
            return true;
        } catch (SecurityException ex) {
            return false;
        }
    }

    /**
     * Turn ON exit trapping if it's currently disabled. If exit trapping is already enabled then
     * nothing happens, but a warning is logged.
     *
     * @throws SecurityException if the security manager has already been set and * * *
     * its <code>checkPermission</code> method doesn't allow it to be replaced.
     */
    public static synchronized void enable() {
        if (!isEnabled()) {
            System.setSecurityManager(
                    new NoExitSecurityManager(System.getSecurityManager()));
        } else {
            LOG.warning("Ignoring failed attempt to enable ExitTrapper when it is already active.");
        }
    }

    /**
     * Turn OFF exit trapping if it's currently enabled. If exit trapping is already disabled then
     * nothing happens, but a warning is logged.
     */
    public static synchronized void disable() {
        if (isEnabled()) {
            System.setSecurityManager(
                    ((NoExitSecurityManager) System.getSecurityManager()).getInner());
        } else {
            LOG.warning("Ignoring failed attempt to disable ExitTrapper when it is not active.");
        }
    }

    /**
     * Turn OFF exit trapping if it's currently on, otherwise turn it ON.
     *
     * @throws SecurityException if EXitTrapper is disabled, the security manager has already been
     * set, and its <code>checkPermission</code> method doesn't allow it to be replaced.
     */
    public static synchronized void toggle() {
        if (isEnabled()) {
            disable();
        } else {
            enable();
        }
    }

    /**
     * Return whether or not exit trapping is enabled.
     *
     * @return true if exit trapping is enabled, false otherwise
     */
    @CheckReturnValue
    public static synchronized boolean isEnabled() {
        final SecurityManager installedSM = System.getSecurityManager();
        return installedSM != null && installedSM.getClass().equals(NoExitSecurityManager.class);
    }

    /**
     * <tt>ExitException</tt> is a {@link SecurityException } that will be thrown when
     * {@link System#exit(int)} is called and trapping is enabled.
     */
    @NotThreadSafe
    public static final class ExitException extends SecurityException {

        private static final long serialVersionUID = 1L;
        /**
         *
         */
        private static final String MESSAGE_PATTERN =
                "Call to System.exit({0,number,integer}) trapped.";
        /**
         * Exit status code that was trapped.
         */
        @Signed
        private final int status;

        /**
         * Construct a new ExitException with the given exit status code.
         *
         * @param status Code passed to {@link System#exit(int)}
         */
        public ExitException(@Signed int status) {
            super(MessageFormat.format(MESSAGE_PATTERN, status));
            this.status = status;
        }

        /**
         * The status code that was passed to {@link System#exit(int)}
         *
         * @return The status code of trapped exit.
         */
        public final @Signed
        int getStatus() {
            return status;
        }
    }

    /**
     * Security manager instance that will throw exceptions when System.exit is called.
     */
    @Immutable
    private static final class NoExitSecurityManager
            extends SecurityManagerDecoratorAdapter {

        private NoExitSecurityManager(@Nullable final SecurityManager inner) {
            super(inner);
        }

        private NoExitSecurityManager() {
            super();
        }

        @Override
        public void checkExit(@Signed final int status) throws ExitException {
            super.checkExit(status);
            throw new ExitException(status);
        }

        @Override
        public void checkPermission(final Permission perm) {
            if (isInnerPresent()) {
                getInner().checkPermission(perm);
            }
        }

        @Override
        public void checkPermission(final Permission perm, final Object context) {
            if (isInnerPresent()) {
                getInner().checkPermission(perm, context);
            }
        }
    }

    /**
     * SecurityManagerDecoratorAdapter wraps a given security manager, delegating all calls to the
     * inner class. This class is intended to be extended, with only a subset of the method
     * overridden.
     */
    @Immutable
    private static abstract class SecurityManagerDecoratorAdapter
            extends SecurityManager {

        private final Optional<SecurityManager> inner;

        private SecurityManagerDecoratorAdapter(@Nullable SecurityManager inner) {
            this.inner = Optional.fromNullable(inner);
        }

        private SecurityManagerDecoratorAdapter() {
            this.inner = Optional.absent();
        }

        public final @Nullable
        SecurityManager getInner() {
            return inner.orNull();
        }

        public final boolean isInnerPresent() {
            return inner.isPresent();
        }

        @Override
        public void checkExit(int status) {
            if (inner.isPresent()) {
                inner.get().checkExit(status);
            } else {
                super.checkExit(status);
            }
        }

        @Override
        public void checkPermission(Permission perm) {
            if (inner.isPresent()) {
                inner.get().checkPermission(perm);
            } else {
                super.checkPermission(perm);
            }
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            if (inner.isPresent()) {
                inner.get().checkPermission(perm, context);
            } else {
                super.checkPermission(perm, context);
            }
        }

        @Override
        public ThreadGroup getThreadGroup() {
            return !inner.isPresent()
                    ? super.getThreadGroup()
                    : inner.get().getThreadGroup();
        }

        @Override
        public Object getSecurityContext() {
            return !inner.isPresent()
                    ? super.getSecurityContext()
                    : inner.get().getSecurityContext();
        }

        @Override
        @Deprecated
        public boolean getInCheck() {
            return !inner.isPresent()
                    ? super.getInCheck()
                    : inner.get().getInCheck();
        }

        @Override
        public void checkWrite(String file) {
            if (inner.isPresent()) {
                inner.get().checkWrite(file);
            } else {
                super.checkWrite(file);
            }
        }

        @Override
        public void checkWrite(FileDescriptor fd) {
            if (inner.isPresent()) {
                inner.get().checkWrite(fd);
            } else {
                super.checkWrite(fd);
            }
        }

        @Override
        public boolean checkTopLevelWindow(Object window) {
            return !inner.isPresent()
                    ? super.checkTopLevelWindow(window)
                    : inner.get().checkTopLevelWindow(window);
        }

        @Override
        public void checkSystemClipboardAccess() {
            if (inner.isPresent()) {
                inner.get().checkSystemClipboardAccess();
            } else {
                super.checkSystemClipboardAccess();
            }
        }

        @Override
        public void checkSetFactory() {
            if (inner.isPresent()) {
                inner.get().checkSetFactory();
            } else {
                super.checkSetFactory();
            }
        }

        @Override
        public void checkSecurityAccess(String target) {
            if (inner.isPresent()) {
                inner.get().checkSecurityAccess(target);
            } else {
                super.checkSecurityAccess(target);
            }
        }

        @Override
        public void checkRead(String file, Object context) {
            if (inner.isPresent()) {
                inner.get().checkRead(file, context);
            } else {
                super.checkRead(file, context);
            }
        }

        @Override
        public void checkRead(String file) {
            if (inner.isPresent()) {
                inner.get().checkRead(file);
            } else {
                super.checkRead(file);
            }
        }

        @Override
        public void checkRead(FileDescriptor fd) {
            if (inner.isPresent()) {
                inner.get().checkRead(fd);
            } else {
                super.checkRead(fd);
            }
        }

        @Override
        public void checkPropertyAccess(String key) {
            if (inner.isPresent()) {
                inner.get().checkPropertyAccess(key);
            } else {
                super.checkPropertyAccess(key);
            }
        }

        @Override
        public void checkPropertiesAccess() {
            if (inner.isPresent()) {
                inner.get().checkPropertiesAccess();
            } else {
                super.checkPropertiesAccess();
            }
        }

        @Override
        public void checkPrintJobAccess() {
            if (inner.isPresent()) {
                inner.get().checkPrintJobAccess();
            } else {
                super.checkPrintJobAccess();
            }
        }

        @Override
        public void checkPackageDefinition(String pkg) {
            if (inner.isPresent()) {
                inner.get().checkPackageDefinition(pkg);
            } else {
                super.checkPackageDefinition(pkg);
            }
        }

        @Override
        public void checkPackageAccess(String pkg) {
            if (inner.isPresent()) {
                inner.get().checkPackageAccess(pkg);
            } else {
                super.checkPackageAccess(pkg);
            }
        }

        @Override
        @Deprecated
        public void checkMulticast(InetAddress maddr, byte ttl) {
            if (inner.isPresent()) {
                inner.get().checkMulticast(maddr, ttl);
            } else {
                super.checkMulticast(maddr, ttl);
            }
        }

        @Override
        public void checkMulticast(InetAddress maddr) {
            if (inner.isPresent()) {
                inner.get().checkMulticast(maddr);
            } else {
                super.checkMulticast(maddr);
            }
        }

        @Override
        public void checkMemberAccess(Class<?> clazz, int which) {
            if (inner.isPresent()) {
                inner.get().checkMemberAccess(clazz, which);
            } else {
                super.checkMemberAccess(clazz, which);
            }
        }

        @Override
        public void checkListen(int port) {
            if (inner.isPresent()) {
                inner.get().checkListen(port);
            } else {
                super.checkListen(port);
            }
        }

        @Override
        public void checkLink(String lib) {
            if (inner.isPresent()) {
                inner.get().checkLink(lib);
            } else {
                super.checkLink(lib);
            }
        }

        @Override
        public void checkExec(String cmd) {
            if (inner.isPresent()) {
                inner.get().checkExec(cmd);
            } else {
                super.checkExec(cmd);
            }
        }

        @Override
        public void checkDelete(String file) {
            if (inner.isPresent()) {
                inner.get().checkDelete(file);
            } else {
                super.checkDelete(file);
            }
        }

        @Override
        public void checkCreateClassLoader() {
            if (inner.isPresent()) {
                inner.get().checkCreateClassLoader();
            } else {
                super.checkCreateClassLoader();
            }
        }

        @Override
        public void checkConnect(String host, int port, Object context) {
            if (inner.isPresent()) {
                inner.get().checkConnect(host, port, context);
            } else {
                super.checkConnect(host, port, context);
            }
        }

        @Override
        public void checkConnect(String host, int port) {
            if (inner.isPresent()) {
                inner.get().checkConnect(host, port);
            } else {
                super.checkConnect(host, port);
            }
        }

        @Override
        public void checkAwtEventQueueAccess() {
            if (inner.isPresent()) {
                inner.get().checkAwtEventQueueAccess();
            } else {
                super.checkAwtEventQueueAccess();
            }
        }

        @Override
        public void checkAccess(ThreadGroup g) {
            if (inner.isPresent()) {
                inner.get().checkAccess(g);
            } else {
                super.checkAccess(g);
            }
        }

        @Override
        public void checkAccess(Thread t) {
            if (inner.isPresent()) {
                inner.get().checkAccess(t);
            } else {
                super.checkAccess(t);
            }
        }

        @Override
        public void checkAccept(String host, int port) {
            if (inner.isPresent()) {
                inner.get().checkAccept(host, port);
            } else {
                super.checkAccept(host, port);
            }
        }
    }
}
