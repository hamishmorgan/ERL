/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.test;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

/**
 * @author hamish
 */
public class AbstractTest {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final File TEST_DATA_PATH = new File("src/test/data");
    @Rule()
    public final TestName testName = new TestName();

    @After
    public void flushOutput() {
        System.out.flush();
        System.err.flush();
    }

    /**
     * TODO: Probably useful enough to move to a general purpose library
     * <p/>
     *
     * @param obj
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Nonnull
    protected static <T> T cloneWithSerialization(final T obj) {

        ObjectOutputStream objectsOut = null;
        ObjectInputStream ois = null;
        try {
            try {
                final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                objectsOut = new ObjectOutputStream(bytesOut);

                objectsOut.writeObject(obj);
                objectsOut.flush();

                final byte[] bytes = bytesOut.toByteArray();

                ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

                @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
                final T result = (T) ois.readObject();
                return result;
            } finally {
                if (objectsOut != null) {
                    objectsOut.close();
                }
                if (ois != null) {
                    ois.close();
                }
            }
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    @Nonnull
    protected static <T> T clone(T obj) {
        try {

            assertTrue("doesn't implement Cloneable", obj instanceof Cloneable);

            final Method cloneMethod = obj.getClass().getMethod("clone");

            assertTrue("clone() is not public", Modifier.isPublic(cloneMethod.getModifiers()));
            assertFalse("clone() is abstract", Modifier.isAbstract(cloneMethod.getModifiers()));
            assertFalse("clone() is static", Modifier.isStatic(cloneMethod.getModifiers()));

            final Object result = cloneMethod.invoke(obj);

            assertEquals("cloned instance class different", result.getClass(), obj.getClass());
            assertEquals("cloned object not equal to original", obj, result);

            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            final T castResult = (T) result;

            return castResult;

        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }

    }

    protected static void assertExhaustedIterator(@Nonnull Iterator<?> it) {
        try {
            it.next();
            fail("Expected iterator to be exhausted by next() succeeded.");
        } catch (NoSuchElementException e) {
            // this is supposed to happen
            assertNotNull(e);
        }
    }

    @Nonnull
    protected static Random newRandom() {
        Random rand = new Random();
        final int seed = rand.nextInt();
        System.out.println(" > random seed = " + seed);
        rand = new Random(seed);
        return rand;
    }

    public static String readTestData(String path) {
        return readTestData(path, DEFAULT_CHARSET);
    }

    public static String readTestData(String path, Charset charset) {
        try {
            return Files.toString(new File(TEST_DATA_PATH, path), charset);
        } catch (IOException ex) {
            // Should throw an assumption exception
            Assume.assumeNoException(ex);
            // ...so this never happens
            throw new AssertionError(ex);
        }

    }

    @Before()
    public final void _printTestMethod() throws SecurityException {
        System.out.println(MessageFormat.format(
                "Running test: {0}#{1}",
                this.getClass().getName(), testName.getMethodName()));
    }

    /**
     * Returns a {@code File} pointing to {@code resourceName} that is relative to
     * the current class context, if the resource is found in the class path.
     *
     * @return the resource File
     * @throws IllegalArgumentException if resource is not found or cannot be accessed as a file.
     */
    @Nonnull
    protected File getResourceAsFile(String name) {
        final URL resource = getResource(name);
        if (resource.getProtocol().equalsIgnoreCase("file"))
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                // occurs when the resource URL is not strictly RFC2396 compliant (which it should be.)
                throw new AssertionError(e);
            }
        else
            throw new IllegalArgumentException(format("Unsupported protocol in url '{1}'", resource));
    }

    /**
     * Returns a {@code URL} pointing to {@code resourceName} that is relative to
     * the current class context, if the resource is found in the class path.
     *
     * @return the resource URL
     * @throws IllegalArgumentException if resource is not found
     */
    protected URL getResource(String name) {
        return Resources.getResource(this.getClass(), name);
    }

    public File newTempFile() throws IOException {
        final File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    public URL newTempUrl() throws IOException {
        return newTempFile().toURI().toURL();
    }


}
