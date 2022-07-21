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
using System.Reflection;
using POxO.IO;

namespace POxO
{
    public class POxOSerializer
    {
        private static int DEFAULT_BUFFER_SIZE = 2048;

        private int customBufferSize;

        private POxOSerializerUtil serializerUtil;

        public POxOSerializer() : this(DEFAULT_BUFFER_SIZE)
        {
        }

        public POxOSerializer(int customBufferSize)
        {
            this.customBufferSize = customBufferSize;
            serializerUtil = new POxOSerializerUtil();
        }

        public Object deserialize(byte[] bytes)
        {
            POxOPrimitiveDecoder input = new POxOPrimitiveDecoder(bytes);
            ObjectSerializer objSerializer = new ObjectSerializer(serializerUtil);
            Object ret = objSerializer.read(input);

            try
            {
                input.Close();
            }
            catch (Exception e)
            {
                throw new POxOSerializerException("Error during decoder stream closing.", e);
            }
            return ret;
        }

        public byte[] serialize(Object obj)
        {

            if (obj == null)
            {
                throw new ArgumentException("It is not possible serialize null object");
            }

            POxOPrimitiveEncoder output = new POxOPrimitiveEncoder(2048);

            ObjectSerializer objSerializer = new ObjectSerializer(serializerUtil);
            objSerializer.write(output, obj);
            byte[] ret = output.GetBuffer();

            try
            {
                output.Close();
            }
            catch (Exception e)
            {
                throw new POxOSerializerException("Error during encoder stream closing.", e);
            }

            return ret;
        }
    }
}
