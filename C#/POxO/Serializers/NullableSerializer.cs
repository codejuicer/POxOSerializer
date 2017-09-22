/*
 * Copyright 2017 Giuseppe Gerla. All Rights Reserved.
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
using System.Linq;
using POxO.IO;
using POxO;
using System.Reflection;
using System.Collections.Generic;

public class NullableSerializer : GenericClassSerializer
{
    private POxOSerializerClassPair pair;

    public NullableSerializer(POxOSerializerClassPair pair)
        : base(true)
    {
        this.pair = pair;
    }

    public override void write(POxOPrimitiveEncoder encoder, Object value)
    {
        try
        {
            if (canBeNull)
            {
                if (value == null)
                {
                    encoder.WriteByte(0x00);
                    return;
                }
                else
                {
                    encoder.WriteByte(0x01);
                }
            }
            GenericClassSerializer nestedSerializer = pair.getSerializer();
            nestedSerializer.write(encoder, value);
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during String serializing.", e);
        }
        catch (NotSupportedException e)
        {
            throw new POxOSerializerException("Error during String serializing.", e);
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
            return InvokeGenericMethodWithRuntimeGenericArguments("ReadNullableValueByNonNullable", new Type[] { pair.getGenericClass() }, new object[] { decoder });
        }
        catch (ObjectDisposedException e)
        {
            throw new POxOSerializerException("Error during String deserializing.", e);
        }
    }

    private T ReadNullableValueByNonNullable<T>(POxOPrimitiveDecoder decoder)
    {
        GenericClassSerializer nestedSerializer = pair.getSerializer();

        return (T)nestedSerializer.read(decoder);
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
