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

public class EnumSerializer : GenericClassSerializer
{

    private Type classToSerialize;

    public EnumSerializer(Type classToSerialize)
        : base(false)
    {
        this.classToSerialize = classToSerialize;
    }

    public override void write(POxOPrimitiveEncoder encoder, Object obj)
    {
        try
        {
            encoder.writeVarInt(((int)Convert.ChangeType(obj, ((Enum)obj).GetTypeCode()) + 1), true);
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Enum serializing.", e);
        }
        catch (NotSupportedException e)
        {
            throw new POxOSerializerException("Error during Enum serializing.", e);
        }
    }

    public override Object read(POxOPrimitiveDecoder decoder)
    {
        try
        {
            int ordinal = decoder.readVarInt(true);
            ordinal--;
            Array enumConstants = classToSerialize.GetEnumValues();

            return enumConstants.GetValue(ordinal);
        }
        catch (ArgumentException e)
        {
            throw new POxOSerializerException("Error during Enum deserializing.", e);
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Enum deserializing.", e);
        }
    }
}
