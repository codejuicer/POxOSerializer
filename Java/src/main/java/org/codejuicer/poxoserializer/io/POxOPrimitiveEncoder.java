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

import java.io.ByteArrayOutputStream;

public class POxOPrimitiveEncoder extends ByteArrayOutputStream {

    /**
     * Creates an uninitialized Output. {@link #setBuffer(byte[], int)} must be called before the Output is
     * used.
     */
    public POxOPrimitiveEncoder() {
    }

    /**
     * Creates a new Output for writing to a byte array.
     * 
     * @param bufferSize The initial and maximum size of the buffer. An exception is thrown if this size is
     *            exceeded.
     */
    public POxOPrimitiveEncoder(int bufferSize) {
        super(bufferSize);
    }

    // byte

    public void writeByte(byte value) {
        write((byte)value);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void writeBytes(byte[] bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("bytes cannot be null.");
        write(bytes, 0, bytes.length);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void writeBytes(byte[] bytes, int offset, int count) {
        if (bytes == null)
            throw new IllegalArgumentException("bytes cannot be null.");
        write(bytes, offset, count);
    }

    // int

    /**
     * Writes a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It
     * is not guaranteed that a variable length encoding will be really used. The stream may decide to use
     * native-sized integer representation for efficiency reasons.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small
     *            negative numbers will be inefficient (5 bytes).
     */
    public int writeInt(int value, boolean optimizePositive) {
        return writeVarInt(value, optimizePositive);
    }

    /**
     * Writes a 1-5 byte int. It is guaranteed that a varible length encoding will be used.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small
     *            negative numbers will be inefficient (5 bytes).
     */
    public int writeVarInt(int value, boolean optimizePositive) {
        if (!optimizePositive)
            value = (value << 1) ^ (value >> 31);
        if (value >>> 7 == 0) {
            writeByte((byte)value);
            return 1;
        }
        if (value >>> 14 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7));
            return 2;
        }
        if (value >>> 21 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14));
            return 3;
        }
        if (value >>> 28 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21));
            return 4;
        }
        writeByte((byte)((value & 0x7F) | 0x80));
        writeByte((byte)(value >>> 7 | 0x80));
        writeByte((byte)(value >>> 14 | 0x80));
        writeByte((byte)(value >>> 21 | 0x80));
        writeByte((byte)(value >>> 28));
        return 5;
    }

    // string

    /**
     * Writes the length and string, or null. Short strings are checked and if ASCII they are written more
     * efficiently, else they are written as UTF8. If a string is known to be ASCII,
     * {@link #writeAscii(String)} may be used. The string can be read using {@link Input#readString()} or
     * {@link Input#readStringBuilder()}.
     * 
     * @param value May be null.
     */
    public void writeString(String value) {
        if (value == null) {
            write(0x80); // 0 means null, bit 8 means UTF8.
            return;
        }
        int charCount = value.length();
        if (charCount == 0) {
            write(1 | 0x80); // 1 means empty string, bit 8 means UTF8.
            return;
        }
        // Detect ASCII.
        boolean ascii = false;
        if (charCount > 1 && charCount < 64) {
            ascii = true;
            for (int i = 0; i < charCount; i++) {
                int c = value.charAt(i);
                if (c > 127) {
                    ascii = false;
                    break;
                }
            }
        }
        if (ascii) {
            byte[] buffer = value.getBytes();
            buffer[charCount - 1] |= 0x80;
            writeBytes(buffer);
        } else {
            writeUtf8Length(charCount + 1);
            int charIndex = 0;

            for (; charIndex < charCount; charIndex++) {
                int c = value.charAt(charIndex);
                if (c > 127)
                    break;
                writeByte((byte)c);
            }

            if (charIndex < charCount)
                writeString_slow(value, charCount, charIndex);
        }
    }

    /**
     * Writes a string that is known to contain only ASCII characters. Non-ASCII strings passed to this method
     * will be corrupted. Each byte is a 7 bit character with the remaining byte denoting if another character
     * is available. This is slightly more efficient than {@link #writeString(String)}. The string can be read
     * using {@link Input#readString()} or {@link Input#readStringBuilder()}.
     * 
     * @param value May be null.
     */
    public void writeAscii(String value) {
        if (value == null) {
            write(0x80); // 0 means null, bit 8 means UTF8.
            return;
        }
        int charCount = value.length();
        switch (charCount) {
        case 0:
            write(1 | 0x80); // 1 is string length + 1, bit 8 means UTF8.
            return;
        case 1:
            write(2 | 0x80); // 2 is string length + 1, bit 8 means UTF8.
            write(value.charAt(0));
            return;
        }
        byte[] buffer = value.getBytes();
        buffer[charCount - 1] |= 0x80; // Bit 8 means end of ASCII.
        writeBytes(buffer);
    }

    /**
     * Writes the length of a string, which is a variable length encoded int except the first byte uses bit 8
     * to denote UTF8 and bit 7 to denote if another byte is present.
     */
    private void writeUtf8Length(int value) {
        if (value >>> 6 == 0) {
            writeByte((byte)(value | 0x80)); // Set bit 8.
        } else if (value >>> 13 == 0) {
            writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            writeByte((byte)(value >>> 6));
        } else if (value >>> 20 == 0) {
            writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            writeByte((byte)(value >>> 13));
        } else if (value >>> 27 == 0) {
            writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            writeByte((byte)((value >>> 13) | 0x80)); // Set bit 8.
            writeByte((byte)(value >>> 20));
        } else {
            writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            writeByte((byte)((value >>> 13) | 0x80)); // Set bit 8.
            writeByte((byte)((value >>> 20) | 0x80)); // Set bit 8.
            writeByte((byte)(value >>> 27));
        }
    }

    private void writeString_slow(CharSequence value, int charCount, int charIndex) {
        for (; charIndex < charCount; charIndex++) {
            int c = value.charAt(charIndex);
            if (c <= 0x007F) {
                writeByte((byte)c);
            } else if (c > 0x07FF) {
                writeByte((byte)(0xE0 | c >> 12 & 0x0F));
                writeByte((byte)(0x80 | c >> 6 & 0x3F));
                writeByte((byte)(0x80 | c & 0x3F));
            } else {
                writeByte((byte)(0xC0 | c >> 6 & 0x1F));
                writeByte((byte)(0x80 | c & 0x3F));
            }
        }
    }

    // float

    /** Writes a 4 byte float. */
    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value), true);
    }

    // short

    /** Writes a 2 byte short. Uses BIG_ENDIAN byte order. */
    public void writeShort(int value) {
        writeByte((byte)(value >>> 8));
        writeByte((byte)value);
    }

    // long

    /**
     * Writes a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It
     * is not guaranteed that a variable length encoding will be really used. The stream may decide to use
     * native-sized integer representation for efficiency reasons.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small
     *            negative numbers will be inefficient (9 bytes).
     */
    public int writeLong(long value, boolean optimizePositive) {
        return writeVarLong(value, optimizePositive);
    }

    /**
     * Writes a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small
     *            negative numbers will be inefficient (9 bytes).
     */
    public int writeVarLong(long value, boolean optimizePositive) {
        if (!optimizePositive)
            value = (value << 1) ^ (value >> 63);
        if (value >>> 7 == 0) {
            writeByte((byte)value);
            return 1;
        }
        if (value >>> 14 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7));
            return 2;
        }
        if (value >>> 21 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14));
            return 3;
        }
        if (value >>> 28 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21));
            return 4;
        }
        if (value >>> 35 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21 | 0x80));
            writeByte((byte)(value >>> 28));
            return 5;
        }
        if (value >>> 42 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21 | 0x80));
            writeByte((byte)(value >>> 28 | 0x80));
            writeByte((byte)(value >>> 35));
            return 6;
        }
        if (value >>> 49 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21 | 0x80));
            writeByte((byte)(value >>> 28 | 0x80));
            writeByte((byte)(value >>> 35 | 0x80));
            writeByte((byte)(value >>> 42));
            return 7;
        }
        if (value >>> 56 == 0) {
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >>> 7 | 0x80));
            writeByte((byte)(value >>> 14 | 0x80));
            writeByte((byte)(value >>> 21 | 0x80));
            writeByte((byte)(value >>> 28 | 0x80));
            writeByte((byte)(value >>> 35 | 0x80));
            writeByte((byte)(value >>> 42 | 0x80));
            writeByte((byte)(value >>> 49));
            return 8;
        }
        writeByte((byte)((value & 0x7F) | 0x80));
        writeByte((byte)(value >>> 7 | 0x80));
        writeByte((byte)(value >>> 14 | 0x80));
        writeByte((byte)(value >>> 21 | 0x80));
        writeByte((byte)(value >>> 28 | 0x80));
        writeByte((byte)(value >>> 35 | 0x80));
        writeByte((byte)(value >>> 42 | 0x80));
        writeByte((byte)(value >>> 49 | 0x80));
        writeByte((byte)(value >>> 56));
        return 9;
    }

    // boolean

    /** Writes a 1 byte boolean. */
    public void writeBoolean(boolean value) {
        writeByte((byte)(value ? 1 : 0));
    }

    // char

    /** Writes a 2 byte char. Uses BIG_ENDIAN byte order. */
    public void writeChar(char value) {
        writeByte((byte)(value >>> 8));
        writeByte((byte)value);
    }

    // double

    /** Writes an 8 byte double. */
    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value), true);
    }
}
