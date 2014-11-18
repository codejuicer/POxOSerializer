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
    public class POxOPrimitiveDecoder : MemoryStream
    {
        protected char[] chars = new char[32];

        /** Creates a new Input for reading from a byte array.
         * @param buffer An exception is thrown if more bytes than this are read. */
        public POxOPrimitiveDecoder(byte[] buffer) : base(buffer)
        {   
        }

        /** Creates a new Input for reading from a byte array.
         * @param buffer An exception is thrown if more bytes than this are read. */
        public POxOPrimitiveDecoder(byte[] buffer, int offset, int count) : base(buffer, offset, count)
        {
        }

        /** Reads bytes.length bytes or less and writes them to the specified byte[], starting at 0, and returns the number of bytes
         * read. */
        public int Read(byte[] bytes)
        {
            return Read(bytes, 0, bytes.Length);
        }

        
        // byte

        
        /** Reads a byte as an int from 0 to 255. */
        public int ReadByteUnsigned()
        {
            return (ReadByte() & 0xFF);
        }

        /** Reads the specified number of bytes into a new byte[]. */
        public byte[] ReadBytes(int length)
        {
            byte[] bytes = new byte[length];
            Read(bytes, 0, length);
            return bytes;
        }

        /** Reads bytes.length bytes and writes them to the specified byte[], starting at index 0. */
        public void ReadBytes(byte[] bytes)
        {
            Read(bytes, 0, bytes.Length);
        }

        
        // int

        /** Reads a 4 byte int. */
        public int readInt()
        {
            return (ReadByte() & 0xFF) << 24 //
              | (ReadByte() & 0xFF) << 16 //
              | (ReadByte() & 0xFF) << 8 //
              | ReadByte() & 0xFF;
        }

        /** Reads a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not guaranteed that
         * a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
         * efficiency reasons. **/
        public int readInt(bool optimizePositive)
        {
            return readVarInt(optimizePositive);
        }

        /** Reads a 1-5 byte int. It is guaranteed that a varible length encoding will be used. */
        public int readVarInt(bool optimizePositive)
        {
            //if ((inputStream.Length-inputStream.Position) < 5) return readInt_slow(optimizePositive);
            int b = ReadByte();
            int result = b & 0x7F;
            if ((b & 0x80) != 0)
            {
                b = ReadByte();
                result |= (b & 0x7F) << 7;
                if ((b & 0x80) != 0)
                {
                    b = ReadByte();
                    result |= (b & 0x7F) << 14;
                    if ((b & 0x80) != 0)
                    {
                        b = ReadByte();
                        result |= (b & 0x7F) << 21;
                        if ((b & 0x80) != 0)
                        {
                            b = ReadByte();
                            result |= (b & 0x7F) << 28;
                        }
                    }
                }
            }
            return optimizePositive ? result : ((result >> 1) ^ -(result & 1));
        }

        
        
        // string

        /** Reads the length and string of UTF8 characters, or null. This can read strings written by {@link Output#writeString(String)}
         * , {@link Output#writeString(CharSequence)}, and {@link Output#writeAscii(String)}.
         * @return May be null. */
        public String readString()
        {
            int b = ReadByte();
            if ((b & 0x80) == 0) return readAscii(); // ASCII.
            // Null, empty, or UTF8.
            //int charCount = (inputStream.Length - inputStream.Position) >= 5 ? readUtf8Length(b) : readUtf8Length_slow(b);
            int charCount = readUtf8Length(b);
            switch (charCount)
            {
                case 0:
                    return null;
                case 1:
                    return "";
            }
            charCount--;
            if (chars.Length < charCount) chars = new char[charCount];
            readUtf8(charCount);
            return new String(chars, 0, charCount);
        }

        private int readUtf8Length(int b)
        {
            int result = b & 0x3F; // Mask all but first 6 bits.
            if ((b & 0x40) != 0)
            { // Bit 7 means another byte, bit 8 means UTF8.
                b = ReadByte();
                result |= (b & 0x7F) << 6;
                if ((b & 0x80) != 0)
                {
                    b = ReadByte();
                    result |= (b & 0x7F) << 13;
                    if ((b & 0x80) != 0)
                    {
                        b = ReadByte();
                        result |= (b & 0x7F) << 20;
                        if ((b & 0x80) != 0)
                        {
                            b = ReadByte();
                            result |= (b & 0x7F) << 27;
                        }
                    }
                }
            }
            return result;
        }

        private void readUtf8(int charCount)
        {
            char[] chars = this.chars;
            // Try to read 7 bit ASCII chars.
            int charIndex = 0;
            long count = Math.Min((Length - Position), (long)charCount);
            
            sbyte b;
            while (charIndex < count)
            {
                b = (sbyte)(ReadByte());
                if (b < 0)
                {
                    Position--;
                    break;
                }
                chars[charIndex++] = (char)b;
            }
            
            // If buffer didn't hold all chars or any were not ASCII, use slow path for remainder.
            if (charIndex < charCount) readUtf8_slow(charCount, charIndex);
        }

        private void readUtf8_slow(int charCount, int charIndex)
        {
            char[] chars = this.chars;
            
            while (charIndex < charCount)
            {
                int b = ReadByte() &0xFF;
                switch (b >> 4)
                {
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
                        chars[charIndex] = (char)((b & 0x1F) << 6 | ReadByte() & 0x3F);
                        break;
                    case 14:
                        chars[charIndex] = (char)((b & 0x0F) << 12 | (ReadByte() & 0x3F) << 6 | ReadByte() & 0x3F);
                        break;
                }
                charIndex++;
            }
        }

        private String readAscii()
        {
            byte[] buffer = new byte[Length - (Position - 1)];
            int index = 0;
            this.Seek(this.Position - 1, SeekOrigin.Begin);

            int b;
            do
            {
                b = ReadByte();
                buffer[index++] = (byte)b;
            } while ((b & 0x80) == 0);
            buffer[index - 1] &= 0x7F; // Mask end of ascii bit.
            String value = Encoding.ASCII.GetString(buffer, 0, index);
            buffer[index - 1] |= 0x80;
            
            return value;
        }

        
        /** Reads the length and string of UTF8 characters, or null. This can read strings written by {@link Output#writeString(String)}
         * , {@link Output#writeString(CharSequence)}, and {@link Output#writeAscii(String)}.
         * @return May be null. */
        public StringBuilder readStringBuilder()
        {
            int b = ReadByte();
            if ((b & 0x80) == 0) return new StringBuilder(readAscii()); // ASCII.
            // Null, empty, or UTF8.
            //int charCount = (inputStream.Length - inputStream.Position) >= 5 ? readUtf8Length(b) : readUtf8Length_slow(b);
            int charCount = readUtf8Length(b);
            switch (charCount)
            {
                case 0:
                    return null;
                case 1:
                    return new StringBuilder("");
            }
            charCount--;
            if (chars.Length < charCount) chars = new char[charCount];
            readUtf8(charCount);
            StringBuilder builder = new StringBuilder(charCount);
            builder.Append(chars, 0, charCount);
            return builder;
        }

        // float

        /** Reads a 4 byte float. */
        public float readFloat()
        {
            byte[] input = new byte[4];

            Read(input, 0, 4);
            return BitConverter.ToSingle(input, 0);
        }

        /** Reads a 1-5 byte float with reduced precision. */
        public float readFloat(float precision, bool optimizePositive)
        {
            return readInt(optimizePositive) / (float)precision;
        }

        // short

        /** Reads a 2 byte short. */
        public short readShort()
        {
            return (short)(((ReadByte() & 0xFF) << 8) | (ReadByte() & 0xFF));
        }

        /** Reads a 2 byte short as an int from 0 to 65535. */
        public int readShortUnsigned()
        {
            return ((ReadByte() & 0xFF) << 8) | (ReadByte() & 0xFF);
        }

        // long

        /** Reads an 8 byte long. */
        public long readLong()
        {
            return (long)ReadByte() << 56 //
              | (long)(ReadByte() & 0xFF) << 48 //
              | (long)(ReadByte() & 0xFF) << 40 //
              | (long)(ReadByte() & 0xFF) << 32 //
              | (long)(ReadByte() & 0xFF) << 24 //
              | (long)(ReadByte() & 0xFF) << 16 //
              | (long)(ReadByte() & 0xFF) << 8 //
              | (long)ReadByte() & 0xFF;

        }

        /** Reads a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not guaranteed that
         * a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
         * efficiency reasons. */
        public long readLong(bool optimizePositive)
        {
            return readVarLong(optimizePositive);
        }

        /** Reads a 1-9 byte long. It is guaranteed that a varible length encoding will be used. */
        public long readVarLong(bool optimizePositive)
        {
            //if ((inputStream.Length - inputStream.Position) < 9) return readLong_slow(optimizePositive);
            int b = ReadByte();
            long result = b & 0x7F;
            if ((b & 0x80) != 0)
            {
                b = ReadByte();
                result |= (long)(b & 0x7F) << 7;
                if ((b & 0x80) != 0)
                {
                    b = ReadByte();
                    result |= (long)(b & 0x7F) << 14;
                    if ((b & 0x80) != 0)
                    {
                        b = ReadByte();
                        result |= (long)(b & 0x7F) << 21;
                        if ((b & 0x80) != 0)
                        {
                            b = ReadByte();
                            result |= (long)(b & 0x7F) << 28;
                            if ((b & 0x80) != 0)
                            {
                                b = ReadByte();
                                result |= (long)(b & 0x7F) << 35;
                                if ((b & 0x80) != 0)
                                {
                                    b = ReadByte();
                                    result |= (long)(b & 0x7F) << 42;
                                    if ((b & 0x80) != 0)
                                    {
                                        b = ReadByte();
                                        result |= (long)(b & 0x7F) << 49;
                                        if ((b & 0x80) != 0)
                                        {
                                            b = ReadByte();
                                            result |= (long)b << 56;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!optimizePositive) result = (result >> 1) ^ -(result & 1);
            return result;
        }

        
        // bool

        /** Reads a 1 byte bool. */
        public bool readbool()
        {
            return ReadByte() == 1;
        }

        // char

        /** Reads a 2 byte char. */
        public char readChar()
        {
            return (char)(((ReadByte() & 0xFF) << 8) | (ReadByte() & 0xFF));
        }

        // double

        /** Reads an 8 bytes double. */
        public double readDouble()
        {
            return BitConverter.Int64BitsToDouble(readLong());
        }

        /** Reads a 1-9 byte double with reduced precision. */
        public double readDouble(double precision, bool optimizePositive)
        {
            return readLong(optimizePositive) / (double)precision;
        }

        // Methods implementing bulk operations on arrays of primitive types

        /** Bulk input of an int array. */
        public int[] readInts(int length, bool optimizePositive)
        {
            int[] array = new int[length];
            for (int i = 0; i < length; i++)
                array[i] = readInt(optimizePositive);
            return array;
        }

        /** Bulk input of a long array. */
        public long[] readLongs(int length, bool optimizePositive)
        {
            long[] array = new long[length];
            for (int i = 0; i < length; i++)
                array[i] = readLong(optimizePositive);
            return array;
        }

        /** Bulk input of an int array. */
        public int[] readInts(int length)
        {
            int[] array = new int[length];
            for (int i = 0; i < length; i++)
                array[i] = readInt();
            return array;
        }

        /** Bulk input of a long array. */
        public long[] readLongs(int length)
        {
            long[] array = new long[length];
            for (int i = 0; i < length; i++)
                array[i] = readLong();
            return array;
        }

        /** Bulk input of a float array. */
        public float[] readFloats(int length)
        {
            float[] array = new float[length];
            for (int i = 0; i < length; i++)
                array[i] = readFloat();
            return array;
        }

        /** Bulk input of a short array. */
        public short[] readShorts(int length)
        {
            short[] array = new short[length];
            for (int i = 0; i < length; i++)
                array[i] = readShort();
            return array;
        }

        /** Bulk input of a char array. */
        public char[] readChars(int length)
        {
            char[] array = new char[length];
            for (int i = 0; i < length; i++)
                array[i] = readChar();
            return array;
        }

        /** Bulk input of a double array. */
        public double[] readDoubles(int length)
        {
            double[] array = new double[length];
            for (int i = 0; i < length; i++)
                array[i] = readDouble();
            return array;
        }
    }
}
