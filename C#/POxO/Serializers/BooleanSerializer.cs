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

public class BooleanSerializer : GenericClassSerializer
{

    public BooleanSerializer(Type classToSerialize)
        : base(Nullable.GetUnderlyingType(classToSerialize) !=null)
    {
    }

    public override void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object value)
    {
        try
        {
            if (canBeNull)
            {
                if (value == null)
                {
                    encoder.writeByte(0x00);
                    return;
                }
                else
                {
                    encoder.writeByte(0x01);
                }
            }
            encoder.writebool((bool)value);
        }
        catch (NotSupportedException e)
        {
            throw new POxOSerializerException("Error during Date serializing.", e);
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Date serializing.", e);
        }
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
            return decoder.readbool();
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Date serializing.", e);
        }
    }
}
