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

package org.codejuicer.poxoserializer.serializers;

import java.util.HashSet;
import java.util.Set;

import org.codejuicer.poxoserializer.POxOSerializerClassPair;
import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.codejuicer.poxoserializer.io.POxOPrimitiveDecoder;
import org.codejuicer.poxoserializer.io.POxOPrimitiveEncoder;

public class SetSerializer extends GenericClassSerializer {

    private POxOSerializerClassPair pair;

    public SetSerializer(POxOSerializerClassPair pair) {
        super(true);
        this.pair = pair;
    }

    @Override
    public Object read(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
        if (canBeNull) {
            byte isNull = decoder.readByte();
            if (isNull == 0x00) {
                return null;
            }
        }

        return createAndFillListOfType(decoder);
    }

    @Override
    public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
        Set<?> set = (Set<?>)value;
        if (canBeNull) {
            if (set == null) {
                encoder.write(0x00);
                return;
            } else {
                encoder.write(0x01);
            }
        }
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        encoder.writeVarInt(set.size(), true);
        for (Object o : set) {
            nestedSerializer.write(encoder, o);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> createAndFillListOfType(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        int size = decoder.readVarInt(true);
        Set<T> set = new HashSet<>(size);

        for (int i = 0; i < size; i++) {
            T o = (T)nestedSerializer.read(decoder);
            set.add(o);
        }
        return set;
    }
}
