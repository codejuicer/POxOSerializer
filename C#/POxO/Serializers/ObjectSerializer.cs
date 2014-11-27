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
using POxO.IO;
using System.Reflection;
using System.Linq;
using POxO;

public class ObjectSerializer : GenericClassSerializer
{
    private Dictionary<String, FieldsSerializer> classFieldSerializerMap;

    private Dictionary<String, FieldSerializerUtil[]> fieldsSerializersMap;

    private POxOSerializerUtil serializerUtil;

    public ObjectSerializer()
        : base(true)
    {
        classFieldSerializerMap = new Dictionary<String, FieldsSerializer>();
        fieldsSerializersMap = new Dictionary<String, FieldSerializerUtil[]>();
        serializerUtil = new POxOSerializerUtil();
    }

    public override Object read(POxOPrimitiveDecoder decoder)
    {
        Object obj = null;
        byte isNull = (byte)decoder.ReadByte();
        if (isNull == 0x00)
        {
            return obj;
        }
        try
        {
            String className = decoder.readString();

            if (className != null)
            {
                Type type = serializerUtil.getClassFromName(className);
                
                GenericClassSerializer ser = serializerUtil.GetTypeSerializer(type);
                if (ser != null)
                {
                    obj = ser.read(decoder);
                }
                else
                {
                    FieldsSerializer fieldsSerializer = null;
                    if (!classFieldSerializerMap.ContainsKey(className))
                    {
                        fieldsSerializer = new FieldsSerializer(type, this);
                        retrieveOrderedFieldsList(type);
                        classFieldSerializerMap.Add(className, fieldsSerializer);
                    }
                    else
                    {
                        fieldsSerializer = classFieldSerializerMap[className];
                    }
                    obj = serializerUtil.createNewInstance(type);

                    fieldsSerializer.read(decoder, this, obj);
                }
            }
        }
        catch (Exception e)
        {
            throw new POxOSerializerException("Error during object deserializing.", e);
        }

        return obj;
    }

    public override void write(POxOPrimitiveEncoder encoder, Object obj)
    {
        if (obj == null)
        {
            encoder.writeByte(0x00);
            return;
        }
        else
        {
            encoder.writeByte(0x01);
        }
        Type type = obj.GetType();

        String name = serializerUtil.getNameFromClass(type);
        
        encoder.writeString(name);

        GenericClassSerializer ser = serializerUtil.GetTypeSerializer(type);
        if (ser != null)
        {
            ser.write(encoder, obj);
        }
        else
        {
            FieldsSerializer fieldsSerializer = null;
            if (!classFieldSerializerMap.ContainsKey(name))
            {
                fieldsSerializer = new FieldsSerializer(type, this);
                retrieveOrderedFieldsList(type);
                classFieldSerializerMap.Add(name, fieldsSerializer);
            }
            else
            {
                fieldsSerializer = classFieldSerializerMap[name];
            }

            fieldsSerializer.write(encoder, this, obj);
        }
    }

    private void retrieveOrderedFieldsList(Type type)
    {
        if (!fieldsSerializersMap.ContainsKey(type.Name))
        {
            List<FieldSerializerUtil> allFieldsSerializer = new List<FieldSerializerUtil>();
            Type nextClass = type;
            while (nextClass != typeof(Object))
            {
                FieldInfo[] declaredFields = nextClass.GetFields(BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.DeclaredOnly);
                if (declaredFields != null)
                {
                    foreach (FieldInfo f in declaredFields)
                    {
                        if (f.IsStatic)
                            continue;

                        allFieldsSerializer.Add(new FieldSerializerUtil(f, serializerUtil.GetFieldSerializer(f)));
                    }
                }
                nextClass = nextClass.BaseType;
            }

            allFieldsSerializer = allFieldsSerializer.OrderBy(o => o.Field.Name).ToList();

            fieldsSerializersMap.Add(type.Name,
                allFieldsSerializer.ToArray());
        }
    }

    public FieldSerializerUtil[] getFieldsSerializers(Type type)
    {
        return fieldsSerializersMap[type.Name];
    }
}
