/* Copyright (c) 2008, Nathan Sweet
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package org.codejuicer.poxoserializer.io;

import java.io.ByteArrayInputStream;

public class POxOPrimitiveDecoder extends ByteArrayInputStream {

    protected char[] chars = new char[32];

    /**
     * Creates a new Input for reading from a byte array.
     * 
     * @param buffer An exception is thrown if more bytes than this are read.
     */
    public POxOPrimitiveDecoder(byte[] buffer) {
        super(buffer);
    }

    /**
     * Creates a new Input for reading from a byte array.
     * 
     * @param buffer An exception is thrown if more bytes than this are read.
     */
    public POxOPrimitiveDecoder(byte[] buffer, int offset, int count) {
        super(buffer, offset, count);
    }

    // byte

    /** Reads a single byte. */
    public byte readByte() {
        return (byte)read();
    }

    /**
     * Reads bytes.length bytes and writes them to the specified byte[], starting at index 0.
     */
    public void readBytes(byte[] bytes) {
        readBytes(bytes, 0, bytes.length);
    }

    /**
     * Reads count bytes and writes them to the specified byte[], starting at offset.
     */
    public void readBytes(byte[] bytes, int offset, int count) {
        read(bytes, offset, count);
    }

    // int

    /**
     * Reads a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is
     * not guaranteed that a variable length encoding will be really used. The stream may decide to use
     * native-sized integer representation for efficiency reasons.
     **/
    public int readInt(boolean optimizePositive) {
        return readVarInt(optimizePositive);
    }

    /**
     * Reads a 1-5 byte int. It is guaranteed that a varible length encoding will be used.
     */
    public int readVarInt(boolean optimizePositive) {
        int b = read();
        int result = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = read();
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = read();
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = read();
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = read();
                        result |= (b & 0x7F) << 28;
                    }
                }
            }
        }
        return optimizePositive ? result : ((result >>> 1) ^ -(result & 1));
    }

    // string

    /**
     * Reads the length and string of UTF8 characters, or null. This can read strings written by
     * {@link Output#writeString(String)} , {@link Output#writeString(CharSequence)}, and
     * {@link Output#writeAscii(String)}.
     * 
     * @return May be null.
     */
    public String readString() {
        int b = read();
        if ((b & 0x80) == 0)
            return readAscii(); // ASCII.
        // Null, empty, or UTF8.
        int charCount = readUtf8Length(b);
        switch (charCount) {
        case 0:
            return null;
        case 1:
            return "";
        }
        charCount--;
        if (chars.length < charCount)
            chars = new char[charCount];
        readUtf8(charCount);
        return new String(chars, 0, charCount);
    }

    private int readUtf8Length(int b) {
        int result = b & 0x3F; // Mask all but first 6 bits.
        if ((b & 0x40) != 0) { // Bit 7 means another byte, bit 8 means UTF8.
            b = read();
            result |= (b & 0x7F) << 6;
            if ((b & 0x80) != 0) {
                b = read();
                result |= (b & 0x7F) << 13;
                if ((b & 0x80) != 0) {
                    b = read();
                    result |= (b & 0x7F) << 20;
                    if ((b & 0x80) != 0) {
                        b = read();
                        result |= (b & 0x7F) << 27;
                    }
                }
            }
        }
        return result;
    }

    private void readUtf8(int charCount) {
        char[] chars = this.chars;
        // Try to read 7 bit ASCII chars.
        int charIndex = 0;
        int count = Math.min(this.count - this.pos, charCount);

        int b;
        while (charIndex < count) {
            b = (byte)read();
            if (b < 0) {
                pos--;
                break;
            }
            chars[charIndex++] = (char)b;
        }

        // If buffer didn't hold all chars or any were not ASCII, use slow path for
        // remainder.
        if (charIndex < charCount)
            readUtf8_slow(charCount, charIndex);
    }

    private void readUtf8_slow(int charCount, int charIndex) {
        char[] chars = this.chars;

        while (charIndex < charCount) {
            int b = read() & 0xFF;
            switch (b >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                chars[charIndex] = (char)b;
                break;
            case 12:
            case 13:
                chars[charIndex] = (char)((b & 0x1F) << 6 | read() & 0x3F);
                break;
            case 14:
                chars[charIndex] = (char)((b & 0x0F) << 12 | (read() & 0x3F) << 6 | read() & 0x3F);
                break;
            }
            charIndex++;
        }
    }

    private String readAscii() {
        byte[] buffer = new byte[count - (pos - 1)];
        int index = 0;
        int start = this.pos - 1;
        this.reset();
        this.skip(start);

        int b;
        do {
            b = read();
            buffer[index++] = (byte)b;
        } while ((b & 0x80) == 0);
        buffer[index - 1] &= 0x7F; // Mask end of ascii bit.
        String value = new String(buffer, 0, 0, index);
        buffer[index - 1] |= 0x80;

        return value;
    }

    // float

    /** Reads a 4 byte float. */
    public float readFloat() {
        return Float.intBitsToFloat(readInt(true));
    }

    // short

    /** Reads a 2 byte short. */
    public short readShort() {
        return (short)(((read() & 0xFF) << 8) | (read() & 0xFF));
    }

    // long

    /**
     * Reads a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It
     * is not guaranteed that a variable length encoding will be really used. The stream may decide to use
     * native-sized integer representation for efficiency reasons.
     */
    public long readLong(boolean optimizePositive) {
        return readVarLong(optimizePositive);
    }

    /**
     * Reads a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
     */
    public long readVarLong(boolean optimizePositive) {
        int b = read();
        long result = b & 0x7F;
        if ((b & 0x80) != 0) {
            b = read();
            result |= (b & 0x7F) << 7;
            if ((b & 0x80) != 0) {
                b = read();
                result |= (b & 0x7F) << 14;
                if ((b & 0x80) != 0) {
                    b = read();
                    result |= (b & 0x7F) << 21;
                    if ((b & 0x80) != 0) {
                        b = read();
                        result |= (long)(b & 0x7F) << 28;
                        if ((b & 0x80) != 0) {
                            b = read();
                            result |= (long)(b & 0x7F) << 35;
                            if ((b & 0x80) != 0) {
                                b = read();
                                result |= (long)(b & 0x7F) << 42;
                                if ((b & 0x80) != 0) {
                                    b = read();
                                    result |= (long)(b & 0x7F) << 49;
                                    if ((b & 0x80) != 0) {
                                        b = read();
                                        result |= (long)b << 56;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!optimizePositive)
            result = (result >>> 1) ^ -(result & 1);
        return result;
    }

    // boolean

    /** Reads a 1 byte boolean. */
    public boolean readBoolean() {
        return read() == 1;
    }

    // char

    /** Reads a 2 byte char. */
    public char readChar() {
        return (char)(((read() & 0xFF) << 8) | (read() & 0xFF));
    }

    // double

    /** Reads an 8 bytes double. */
    public double readDouble() {
        return Double.longBitsToDouble(readLong(true));
    }
}
