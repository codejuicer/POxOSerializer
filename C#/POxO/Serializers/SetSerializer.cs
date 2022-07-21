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
using System.Linq;
using System.Reflection;
using POxO;

public class SetSerializer : GenericClassSerializer
{
    private POxOSerializerClassPair pair;

    public SetSerializer(POxOSerializerClassPair pair)
        : base(true)
    {
        this.pair = pair;
    }

    public override void write(POxOPrimitiveEncoder encoder, Object value)
    {
        ISet<Object> set;
        try
        {
            set = (ISet<Object>)value;
            if (canBeNull)
            {
                if (set == null)
                {
                    encoder.WriteByte(0x00);
                    return;
                }
                else
                {
                    encoder.WriteByte(0x01);
                }
            }
            encoder.writeVarInt(set.Count, true);
            GenericClassSerializer nestedSerializer = pair.getSerializer();
            
            foreach (Object o in set)
            {
                nestedSerializer.write(encoder, o);
            }
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Set serializing.", e);
        }
        catch (NotSupportedException e)
        {
            throw new POxOSerializerException("Error during Set serializing.", e);
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

            return InvokeGenericMethodWithRuntimeGenericArguments("createAndFillSetOfType", new Type[] { pair.getGenericClass() }, new object[] { decoder });
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during Set deserializing.", e);
        }
    }

    private ISet<T> createAndFillSetOfType<T>(POxOPrimitiveDecoder decoder)
    {
        int size = decoder.readVarInt(true);
        ISet<T> set = new HashSet<T>();
        
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        
        for (int i = 0; i < size; i++)
        {
            T o = (T)nestedSerializer.read(decoder);
            set.Add(o);
        }
        return set;
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
