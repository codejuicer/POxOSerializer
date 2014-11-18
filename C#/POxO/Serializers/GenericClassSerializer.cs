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


public abstract class GenericClassSerializer : FieldsReaderVisitor, FieldsWriterVisitor
{

    protected bool canBeNull;

    public GenericClassSerializer(bool canBeNull)
    {
        this.canBeNull = canBeNull;
    }

    public abstract Object read(POxOPrimitiveDecoder decoder, ObjectSerializer serializer);

    public abstract void write(POxOPrimitiveEncoder encoder, ObjectSerializer serializer, Object value);
}
