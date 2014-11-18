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

public class MapSerializer : GenericClassSerializer
{
    private Type[] genericTypes;

    public MapSerializer(Type[] genericTypes)
        : base(true)
    {
        this.genericTypes = genericTypes;
    }

    public override Object read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer)
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

            var list = (IDictionary)typeof(Dictionary<,>).MakeGenericType(genericTypes).GetConstructor(Type.EmptyTypes).Invoke(null);

            for (int i = 0; i < size; i++)
            {
                Object key = serializer.read(decoder, serializer);
                Object value = serializer.read(decoder, serializer);
                list.Add(key, value);
            }

            return list;
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during List deserializing.", e);
        }
    }

    public override void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object value)
    {
        Dictionary<Object, Object> map;
        try
        {
            map = (Dictionary<Object, Object>)value;
            if (canBeNull)
            {
                if (map == null)
                {
                    encoder.WriteByte(0x00);
                    return;
                }
                else
                {
                    encoder.WriteByte(0x01);
                }
            }
            encoder.writeVarInt(map.Count, true);
            foreach (Object key in map.Keys)
            {
                serializer.write(encoder, serializer, key);
                serializer.write(encoder, serializer, map[key]);
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

}
