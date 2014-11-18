/*
 * Copyright 2014 Giuseppe Gerla. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace POxO.IO
{
    public class POxOPrimitiveEncoder : MemoryStream
    {
        /** Creates an uninitialized Output. {@link #setBuffer(byte[], int)} must be called before the Output is used. */
        public POxOPrimitiveEncoder() : base()
        {   
        }

        /** Creates a new Output for writing to a byte array.
         * @param bufferSize The initial and maximum size of the buffer. An exception is thrown if this size is exceeded. */
        public POxOPrimitiveEncoder(int bufferSize) : base(bufferSize)
        {
        }

        
        
        // byte

        public void writeByte(int value)
        {
            WriteByte((byte)value);
        }

        ///** Writes the bytes. Note the byte[] length is not written. */
        public void WriteBytes(byte[] bytes)
        {
            if (bytes == null) throw new ArgumentException("bytes cannot be null.");
            Write(bytes, 0, bytes.Length);
        }

  
        // int

        /** Writes a 4 byte int. Uses BIG_ENDIAN byte order. */
        public void writeInt(int value)
        {
            writeByte((byte)(value >> 24));
            writeByte((byte)(value >> 16));
            writeByte((byte)(value >> 8));
            writeByte((byte)value);
        }

        /** Writes a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not guaranteed that
         * a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
         * efficiency reasons.
         * 
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (5 bytes). */
        public int writeInt(int value, bool optimizePositive)
        {
            return writeVarInt(value, optimizePositive);
        }

        /** Writes a 1-5 byte int. It is guaranteed that a varible length encoding will be used.
         * 
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (5 bytes). */
        public int writeVarInt(int value, bool optimizePositive)
        {
            if (!optimizePositive) value = (value << 1) ^ (value >> 31);
            if (value >> 7 == 0)
            {
                writeByte((byte)value);
                return 1;
            }
            if (value >> 14 == 0)
            {
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7));
                return 2;
            }
            if (value >> 21 == 0)
            {
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14));
                return 3;
            }
            if (value >> 28 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21));
                return 4;
            }
            
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >> 7 | 0x80));
            writeByte((byte)(value >> 14 | 0x80));
            writeByte((byte)(value >> 21 | 0x80));
            writeByte((byte)(value >> 28));
            return 5;
        }

        // string

        /** Writes the length and string, or null. Short strings are checked and if ASCII they are written more efficiently, else they
         * are written as UTF8. If a string is known to be ASCII, {@link #writeAscii(String)} may be used. The string can be read using
         * {@link Input#readString()} or {@link Input#readStringBuilder()}.
         * @param value May be null. */
        public void writeString(String value)
        {
            if (value == null)
            {
                writeByte(0x80); // 0 means null, bit 8 means UTF8.
                return;
            }
            int charCount = value.Length;
            if (charCount == 0)
            {
                writeByte(1 | 0x80); // 1 means empty string, bit 8 means UTF8.
                return;
            }
            // Detect ASCII.
            bool ascii = false;
            if (charCount > 1 && charCount < 64)
            {
                ascii = true;
                for (int i = 0; i < charCount; i++)
                {
                    int c = value.ElementAt(i);
                    if (c > 127)
                    {
                        ascii = false;
                        break;
                    }
                }
            }
            if (ascii)
            {
                byte[] bytes = new byte[charCount];
                Encoding.ASCII.GetBytes(value.ToCharArray(), 0, charCount, bytes, 0);
                bytes[charCount - 1] |= 0x80;
                WriteBytes(bytes);
            }
            else
            {
                writeUtf8Length(charCount + 1);
                int charIndex = 0;

                for (; charIndex < charCount; charIndex++)
                {
                    int c = value.ElementAt(charIndex);
                    if (c > 127) break;
                    WriteByte((byte)c);
                }
                
                if (charIndex < charCount) writeString_slow(value.ToCharArray(), charCount, charIndex);
            }
        }

        /** Writes a string that is known to contain only ASCII characters. Non-ASCII strings passed to this method will be corrupted.
         * Each byte is a 7 bit character with the remaining byte denoting if another character is available. This is slightly more
         * efficient than {@link #writeString(String)}. The string can be read using {@link Input#readString()} or
         * {@link Input#readStringBuilder()}.
         * @param value May be null. */
        public void writeAscii(String value)
        {
            if (value == null)
            {
                writeByte(0x80); // 0 means null, bit 8 means UTF8.
                return;
            }
            int charCount = value.Length;
            switch (charCount)
            {
                case 0:
                    writeByte(1 | 0x80); // 1 is string length + 1, bit 8 means UTF8.
                    return;
                case 1:
                    writeByte(2 | 0x80); // 2 is string length + 1, bit 8 means UTF8.
                    writeByte(value.ElementAt(0));
                    return;
            }
            byte[] buffer = new byte[charCount];
            Encoding.UTF8.GetBytes(value.ToCharArray(), 0, charCount, buffer, 0);
            buffer[charCount - 1] |= 0x80; // Bit 8 means end of ASCII.
            WriteBytes(buffer);
        }

        /** Writes the length of a string, which is a variable length encoded int except the first byte uses bit 8 to denote UTF8 and
         * bit 7 to denote if another byte is present. */
        private void writeUtf8Length(int value)
        {
            if (value >> 6 == 0)
            {
                writeByte((byte)(value | 0x80)); // Set bit 8.
            }
            else if (value >> 13 == 0)
            {
                writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
                writeByte((byte)(value >> 6));
            }
            else if (value >> 20 == 0)
            {
                writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
                writeByte((byte)((value >> 6) | 0x80)); // Set bit 8.
                writeByte((byte)(value >> 13));
            }
            else if (value >> 27 == 0)
            {
                writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
                writeByte((byte)((value >> 6) | 0x80)); // Set bit 8.
                writeByte((byte)((value >> 13) | 0x80)); // Set bit 8.
                writeByte((byte)(value >> 20));
            }
            else
            {
                writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
                writeByte((byte)((value >> 6) | 0x80)); // Set bit 8.
                writeByte((byte)((value >> 13) | 0x80)); // Set bit 8.
                writeByte((byte)((value >> 20) | 0x80)); // Set bit 8.
                writeByte((byte)(value >> 27));
            }
        }

        private void writeString_slow(char[] value, int charCount, int charIndex)
        {
            for (; charIndex < charCount; charIndex++)
            {
                int c = value.ElementAt(charIndex);
                if (c <= 0x007F)
                {
                    writeByte((byte)c);
                }
                else if (c > 0x07FF)
                {
                    writeByte((byte)(0xE0 | c >> 12 & 0x0F));
                    
                    writeByte((byte)(0x80 | c >> 6 & 0x3F));
                    writeByte((byte)(0x80 | c & 0x3F));
                }
                else
                {
                    writeByte((byte)(0xC0 | c >> 6 & 0x1F));
                    
                    writeByte((byte)(0x80 | c & 0x3F));
                }
            }
        }


        // float

        /** Writes a 4 byte float. */
        public void writeFloat(float value)
        {
            byte[] buffer = BitConverter.GetBytes(value);
            WriteBytes(buffer);
        }

        /** Writes a 1-5 byte float with reduced precision.
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (5 bytes). */
        public int writeFloat(float value, float precision, bool optimizePositive)
        {
            return writeInt((int)(value * precision), optimizePositive);
        }

        // short

        /** Writes a 2 byte short. Uses BIG_ENDIAN byte order. */
        public void writeShort(int value)
        {
            writeByte((byte)(value >> 8));
            writeByte((byte)value);
        }

        // long

        /** Writes an 8 byte long. Uses BIG_ENDIAN byte order. */
        public void writeLong(long value)
        {
            writeByte((byte)(value >> 56));
            writeByte((byte)(value >> 48));
            writeByte((byte)(value >> 40));
            writeByte((byte)(value >> 32));
            writeByte((byte)(value >> 24));
            writeByte((byte)(value >> 16));
            writeByte((byte)(value >> 8));
            writeByte((byte)value);
        }

        /** Writes a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not guaranteed
         * that a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
         * efficiency reasons.
         * 
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (9 bytes). */
        public int writeLong(long value, bool optimizePositive)
        {
            return writeVarLong(value, optimizePositive);
        }

        /** Writes a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (9 bytes). */
        public int writeVarLong(long value, bool optimizePositive)
        {
            if (!optimizePositive) value = (value << 1) ^ (value >> 63);
            if (value >> 7 == 0)
            {
                
                writeByte((byte)value);
                return 1;
            }
            if (value >> 14 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7));
                return 2;
            }
            if (value >> 21 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14));
                return 3;
            }
            if (value >> 28 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21));
                return 4;
            }
            if (value >> 35 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21 | 0x80));
                writeByte((byte)(value >> 28));
                return 5;
            }
            if (value >> 42 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21 | 0x80));
                writeByte((byte)(value >> 28 | 0x80));
                writeByte((byte)(value >> 35));
                return 6;
            }
            if (value >> 49 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21 | 0x80));
                writeByte((byte)(value >> 28 | 0x80));
                writeByte((byte)(value >> 35 | 0x80));
                writeByte((byte)(value >> 42));
                return 7;
            }
            if (value >> 56 == 0)
            {
                
                writeByte((byte)((value & 0x7F) | 0x80));
                writeByte((byte)(value >> 7 | 0x80));
                writeByte((byte)(value >> 14 | 0x80));
                writeByte((byte)(value >> 21 | 0x80));
                writeByte((byte)(value >> 28 | 0x80));
                writeByte((byte)(value >> 35 | 0x80));
                writeByte((byte)(value >> 42 | 0x80));
                writeByte((byte)(value >> 49));
                return 8;
            }
            
            writeByte((byte)((value & 0x7F) | 0x80));
            writeByte((byte)(value >> 7 | 0x80));
            writeByte((byte)(value >> 14 | 0x80));
            writeByte((byte)(value >> 21 | 0x80));
            writeByte((byte)(value >> 28 | 0x80));
            writeByte((byte)(value >> 35 | 0x80));
            writeByte((byte)(value >> 42 | 0x80));
            writeByte((byte)(value >> 49 | 0x80));
            writeByte((byte)(value >> 56));
            return 9;
        }

        // bool

        /** Writes a 1 byte bool. */
        public void writebool(bool value)
        {
            writeByte((byte)(value ? 1 : 0));
        }

        // char

        /** Writes a 2 byte char. Uses BIG_ENDIAN byte order. */
        public void writeChar(char value)
        {
            writeByte((byte)(value >> 8));
            writeByte((byte)value);
        }

        // double

        /** Writes an 8 byte double. */
        public void writeDouble(double value)
        {
            writeLong(BitConverter.DoubleToInt64Bits(value));
        }

        /** Writes a 1-9 byte double with reduced precision.
         * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
         *           inefficient (9 bytes). */
        public int writeDouble(double value, double precision, bool optimizePositive)
        {
            return writeLong((long)(value * precision), optimizePositive);
        }

        /** Returns the number of bytes that would be written with {@link #writeInt(int, bool)}. */
        static public int intLength(int value, bool optimizePositive)
        {
            if (!optimizePositive) value = (value << 1) ^ (value >> 31);
            if (value >> 7 == 0) return 1;
            if (value >> 14 == 0) return 2;
            if (value >> 21 == 0) return 3;
            if (value >> 28 == 0) return 4;
            return 5;
        }

        /** Returns the number of bytes that would be written with {@link #writeLong(long, bool)}. */
        static public int longLength(long value, bool optimizePositive)
        {
            if (!optimizePositive) value = (value << 1) ^ (value >> 63);
            if (value >> 7 == 0) return 1;
            if (value >> 14 == 0) return 2;
            if (value >> 21 == 0) return 3;
            if (value >> 28 == 0) return 4;
            if (value >> 35 == 0) return 5;
            if (value >> 42 == 0) return 6;
            if (value >> 49 == 0) return 7;
            if (value >> 56 == 0) return 8;
            return 9;
        }

        // Methods implementing bulk operations on arrays of primitive types

        /** Bulk output of an int array. */
        public void writeInts(int[] obj, bool optimizePositive)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeInt(obj[i], optimizePositive);
        }

        /** Bulk output of an long array. */
        public void writeLongs(long[] obj, bool optimizePositive)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeLong(obj[i], optimizePositive);
        }

        /** Bulk output of an int array. */
        public void writeInts(int[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeInt(obj[i]);
        }

        /** Bulk output of an long array. */
        public void writeLongs(long[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeLong(obj[i]);
        }

        /** Bulk output of a float array. */
        public void writeFloats(float[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeFloat(obj[i]);
        }

        /** Bulk output of a short array. */
        public void writeShorts(short[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeShort(obj[i]);
        }

        /** Bulk output of a char array. */
        public void writeChars(char[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeChar(obj[i]);
        }

        /** Bulk output of a double array. */
        public void writeDoubles(double[] obj)
        {
            for (int i = 0, n = obj.Length; i < n; i++)
                writeDouble(obj[i]);
        }
    }
}
