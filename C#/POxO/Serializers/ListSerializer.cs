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
using System.Linq;
using System.Reflection;
using POxO;

public class ListSerializer : GenericClassSerializer
{
    private POxOSerializerClassPair pair;

    public ListSerializer(POxOSerializerClassPair pair)
        : base(true)
    {
        this.pair = pair;
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
            GenericClassSerializer nestedSerializer = pair.getSerializer();
            
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

            return InvokeGenericMethodWithRuntimeGenericArguments("createAndFillListOfType", new Type[] { pair.getGenericClass() }, new object[] { decoder });
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during List deserializing.", e);
        }
    }

    private IList<T> createAndFillListOfType<T>(POxOPrimitiveDecoder decoder)
    {
        IList<T> list = new List<T>();

        int size = decoder.readVarInt(true);
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        
        for (int i = 0; i < size; i++)
        {
            T o = (T)nestedSerializer.read(decoder);
            list.Add(o);
        }
        return list;
    }

    private object InvokeGenericMethodWithRuntimeGenericArguments(String genericMethodName, Type[] runtimeGenericArguments, params object[] parameters)
    {
        if (parameters == null)
        {
            parameters = new object[0];
        }
        if (runtimeGenericArguments == null)
        {
            runtimeGenericArguments = new Type[0];
        }

        MethodInfo[] methods = this.GetType()
                     .GetMethods(BindingFlags.NonPublic | BindingFlags.Instance);
        List<MethodInfo> met = methods.Where(m => m.Name.Contains(genericMethodName)).ToList();
        var myMethod = this.GetType()
                     .GetMethods(BindingFlags.NonPublic | BindingFlags.Instance)
                     .Where(m => m.Name.Contains(genericMethodName))
                     .Select(m => new
                     {
                         Method = m,
                         Params = m.GetParameters(),
                         Args = m.GetGenericArguments()
                     })
                     .Where(x => x.Params.Length == parameters.Length
                                 && x.Args.Length == runtimeGenericArguments.Length
                     )
                     .Select(x => x.Method)
                     .First().MakeGenericMethod(runtimeGenericArguments);
        return myMethod.Invoke(this, parameters);
    }
}
