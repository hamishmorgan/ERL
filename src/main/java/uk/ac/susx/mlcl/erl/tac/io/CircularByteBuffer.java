package uk.ac.susx.mlcl.erl.tac.io;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.*;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 25/07/2013
* Time: 15:09
* To change this template use File | Settings | File Templates.
*/
class CircularByteBuffer  {
    private final int size;
    @Nonnull
    private final byte[] buf;
    private int length;
    private int nextGet;
    private int nextPut;

    public CircularByteBuffer(final int size) {
        checkArgument(size >= 0);
        this.size = size;
        buf = new byte[size];
    }

    public int capacity() {
        return size;
    }

    public int length() {
        return length;
    }

    public void clear() {
        length = 0;
        nextGet = 0;
        nextPut = 0;
    }

    public boolean equals(@Nonnull byte[] arr) {
        if(arr.length != length())
            return false;
        for (int i = 0; i < length(); i++) {
            if (arr[i] != buf[(nextGet + i) % size])
                return false;
        }
        return true;
    }

    public byte get() throws BufferUnderflowException {
        if (isEmpty())
            throw new BufferUnderflowException();

        length--;
        byte b = buf[nextGet++];
        if (nextGet >= size)
            nextGet = 0;
        return b;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public void put(byte b) throws BufferOverflowException {
        if (isFull())
            throw new BufferOverflowException();

        length++;
        buf[nextPut++] = b;
        if (nextPut >= size)
            nextPut = 0;
    }

    public void put(@Nonnull byte[] bytes) {
        for (byte b : bytes)
            put(b);
    }

    public boolean isFull() {
        return length == size;
    }
}
