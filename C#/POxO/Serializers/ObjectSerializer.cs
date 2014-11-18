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

public class ObjectSerializer : GenericClassSerializer
{

    private Dictionary<String, Type> classForName;

    private Dictionary<Type, String> nameForClass;

    private Dictionary<String, FieldsSerializer> classFieldSerializerMap;

    private Dictionary<String, FieldSerializerUtil[]> fieldsSerializersMap;

    public ObjectSerializer()
        : base(true)
    {
        classForName = new Dictionary<String, Type>();
        nameForClass = new Dictionary<Type, String>();
        classFieldSerializerMap = new Dictionary<String, FieldsSerializer>();
        fieldsSerializersMap = new Dictionary<String, FieldSerializerUtil[]>();
        initializePrimitiveType();
    }

    private void initializePrimitiveType()
    {
        nameForClass.Add(typeof(Int32), "int");
        nameForClass.Add(typeof(Int64), "long");
        nameForClass.Add(typeof(Int16), "short");
        nameForClass.Add(typeof(Double), "double");
        nameForClass.Add(typeof(Single), "float");
        nameForClass.Add(typeof(Boolean), "bool");
        nameForClass.Add(typeof(Byte), "byte");
        nameForClass.Add(typeof(Char), "char");
        nameForClass.Add(typeof(String), "string");
        nameForClass.Add(typeof(DateTime), "date");
        nameForClass.Add(typeof(Enum), "enum");
        nameForClass.Add(typeof(List<>), "list");
        nameForClass.Add(typeof(Dictionary<,>), "map");

        classForName.Add("int", typeof(Int32));
        classForName.Add("long", typeof(Int64));
        classForName.Add("short", typeof(Int16));
        classForName.Add("double", typeof(Double));
        classForName.Add("float", typeof(float));
        classForName.Add("bool", typeof(Boolean));
        classForName.Add("byte", typeof(Byte));
        classForName.Add("char", typeof(Char));
        classForName.Add("string", typeof(String));
        classForName.Add("date", typeof(DateTime));
        classForName.Add("enum", typeof(Enum));
        classForName.Add("list", typeof(List<>));
        classForName.Add("map", typeof(Dictionary<,>));
    }

    private Object createNewInstance(Type clazz)
    {
        Object ret = Activator.CreateInstance(clazz);

        return ret;
    }

    private Type LoadTypeFromName(String typeName)
    {
        Type ret = null;


        Assembly[] assemblies = AppDomain.CurrentDomain.GetAssemblies();
        // the trouble is that we don't know which assembly the class is defined in,
        // because we are using the "Web Site" model in Visual Studio that compiles
        // them on the fly into assemblies with random names
        // -> however, we do know that the assembly will be named App_Web_*
        // (http://msdn.microsoft.com/en-us/magazine/cc163496.aspx)
        foreach (Assembly assembly in assemblies)
        {
            Type t = assembly.GetType(typeName);
            if (t != null)
            {
                ret = t;
                break;
            }
        }

        return ret;
    }

    public override Object read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer)
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
                Type type = null;
                if (!classForName.ContainsKey(className))
                {
                    type = LoadTypeFromName(className);
                    classForName.Add(className, type);
                    nameForClass.Add(type, className);
                }
                else
                {
                    type = classForName[className];
                }

                GenericClassSerializer ser = GetFieldSerializer(type);
                if (ser != this)
                {
                    obj = ser.read(decoder, serializer);
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
                    obj = createNewInstance(type);

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

    public override void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object obj)
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

        String name = null;
        if (!nameForClass.ContainsKey(type))
        {
            name = type.FullName;
            nameForClass.Add(type, name);
            classForName.Add(name, type);
        }
        else
        {
            name = nameForClass[type];
        }

        encoder.writeString(name);

        GenericClassSerializer ser = GetFieldSerializer(type);
        if (ser != this)
        {
            ser.write(encoder, serializer, obj);
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

                        allFieldsSerializer.Add(new FieldSerializerUtil(f, GetFieldSerializer(f.FieldType)));
                    }
                }
                nextClass = nextClass.BaseType;
            }

            allFieldsSerializer = allFieldsSerializer.OrderBy(o => o.Field.Name).ToList();

            fieldsSerializersMap.Add(type.Name,
                allFieldsSerializer.ToArray());
        }
    }

    private GenericClassSerializer GetFieldSerializer(Type fieldType)
    {
        GenericClassSerializer ret = null;
        if (typeof(Int32).IsAssignableFrom(fieldType) || typeof(int).IsAssignableFrom(fieldType))
        {
            ret = new IntegerSerializer(fieldType);
        }
        else if (typeof(Int64).IsAssignableFrom(fieldType) || typeof(long).IsAssignableFrom(fieldType))
        {
            ret = new LongSerializer(fieldType);
        }
        else if (typeof(Int16).IsAssignableFrom(fieldType) || typeof(short).IsAssignableFrom(fieldType))
        {
            ret = new ShortSerializer(fieldType);
        }
        else if (typeof(float).IsAssignableFrom(fieldType))
        {
            ret = new FloatSerializer(fieldType);
        }
        else if (typeof(Double).IsAssignableFrom(fieldType)
          || typeof(double).IsAssignableFrom(fieldType))
        {
            ret = new DoubleSerializer(fieldType);
        }
        else if (typeof(String).IsAssignableFrom(fieldType))
        {
            ret = new StringSerializer();
        }
        else if (typeof(Byte).IsAssignableFrom(fieldType) || typeof(byte).IsAssignableFrom(fieldType))
        {
            ret = new ByteSerializer(fieldType);
        }
        else if (typeof(Char).IsAssignableFrom(fieldType)
          || typeof(char).IsAssignableFrom(fieldType))
        {
            ret = new CharSerializer(fieldType);
        }
        else if (typeof(Boolean).IsAssignableFrom(fieldType)
          || typeof(bool).IsAssignableFrom(fieldType))
        {
            ret = new BooleanSerializer(fieldType);
        }
        else if (typeof(DateTime).IsAssignableFrom(fieldType))
        {
            ret = new DateSerializer();
        }
        else if (typeof(Enum).IsAssignableFrom(fieldType))
        {
            ret = new EnumSerializer(fieldType);
        }
        else if (fieldType.IsGenericType && (fieldType.GetGenericTypeDefinition() == typeof(List<>)))
        {
            ret = new ListSerializer(fieldType.GetGenericArguments());
        }
        else if (typeof(Dictionary<,>).IsAssignableFrom(fieldType))
        {
            ret = new MapSerializer(fieldType.GetGenericArguments());
        }
        else
        {
            ret = this;
        }
        return ret;
    }

    public FieldSerializerUtil[] getFieldsSerializers(Type type)
    {
        return fieldsSerializersMap[type.Name];
    }
}
