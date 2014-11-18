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

public class FieldsSerializer
{

    private ObjectSerializer objectSerializer;

    private Type type;

    public FieldsSerializer(Type classToSerialize, ObjectSerializer objectSerializer)
    {
        this.objectSerializer = objectSerializer;
        this.type = classToSerialize;
    }

    public void read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer, Object obj)
    {
        try
        {
            FieldSerializerUtil[] fieldsSerializerList = objectSerializer.getFieldsSerializers(type);
            for (int i = 0, n = fieldsSerializerList.Length; i < n; i++)
                fieldsSerializerList[i].Field.SetValue(obj,
                    fieldsSerializerList[i].Serializer.read(decoder, serializer));
        }
        catch (Exception e)
        {
            throw new POxOSerializerException("Error during fields deserializing. ", e);
        }
    }

    public void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object obj)
    {
        try
        {
            FieldSerializerUtil[] fieldsSerializerList = objectSerializer.getFieldsSerializers(type);
            for (int i = 0, n = fieldsSerializerList.Length; i < n; i++)
                fieldsSerializerList[i].Serializer.write(encoder, serializer,
                    fieldsSerializerList[i].Field.GetValue(obj));
        }
        catch (Exception e)
        {
            throw new POxOSerializerException("Error during fields serializing. ", e);
        }
    }
}
