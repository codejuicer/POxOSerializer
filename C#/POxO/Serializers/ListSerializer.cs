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
using POxO.IO;
using System.Collections.Generic;
using System.Collections;

public class ListSerializer : GenericClassSerializer
{
    private Type genericTypes;

    private GenericClassSerializer nestedSerializer;

    public ListSerializer(Type genericTypes, GenericClassSerializer nestedSerializer)
        : base(true)
    {
        this.genericTypes = genericTypes;
        this.nestedSerializer = nestedSerializer;
    }

    public override void write(POxOPrimitiveEncoder encoder, Object value)
    {
        IList list;
        try
        {
            list = (IList)value;
            if (canBeNull)
            {
                if (list == null)
                {
                    encoder.WriteByte(0x00);
                    return;
                }
                else
                {
                    encoder.WriteByte(0x01);
                }
            }
            encoder.writeVarInt(list.Count, true);
            foreach (Object o in list)
            {
                nestedSerializer.write(encoder, o);
            }
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during List serializing.", e);
        }
        catch (NotSupportedException e)
        {
            throw new POxOSerializerException("Error during List serializing.", e);
        }
    }

    public override Object read(POxOPrimitiveDecoder decoder)
    {
        try
        {
            if (canBeNull)
            {
                byte isNull = (byte)decoder.ReadByte();
                if (isNull == 0x00)
                {
                    return null;
                }
            }
            int size = decoder.readVarInt(true);

            IList list = (IList)typeof(List<>).MakeGenericType(genericTypes).GetConstructor(Type.EmptyTypes).Invoke(null);
            
            for (int i = 0; i < size; i++)
            {
                Object o = (Object)nestedSerializer.read(decoder);
                list.Add(o);
            }

            return list;
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during List deserializing.", e);
        }
    }
}
