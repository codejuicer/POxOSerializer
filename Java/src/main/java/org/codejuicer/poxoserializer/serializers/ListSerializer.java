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

import java.util.ArrayList;
import java.util.List;

import org.codejuicer.poxoserializer.POxOSerializerClassPair;
import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.codejuicer.poxoserializer.io.POxOPrimitiveDecoder;
import org.codejuicer.poxoserializer.io.POxOPrimitiveEncoder;

public class ListSerializer extends GenericClassSerializer {

    private POxOSerializerClassPair pair;

    public ListSerializer(POxOSerializerClassPair pair) {
        super(true);
        this.pair = pair;
    }

    @Override
    public void write(POxOPrimitiveEncoder encoder, Object value) throws POxOSerializerException {
        List<?> list = (List<?>)value;
        if (canBeNull) {
            if (list == null) {
                encoder.write(0x00);
                return;
            } else {
                encoder.write(0x01);
            }
        }
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        encoder.writeVarInt(list.size(), true);
        for (Object o : list) {
            nestedSerializer.write(encoder, o);
        }
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

    @SuppressWarnings("unchecked")
    private <T> List<T> createAndFillListOfType(POxOPrimitiveDecoder decoder) throws POxOSerializerException {
        List<T> list = new ArrayList<T>();
        GenericClassSerializer nestedSerializer = pair.getSerializer();
        int size = decoder.readVarInt(true);

        for (int i = 0; i < size; i++) {
            T o = (T)nestedSerializer.read(decoder);
            list.add(o);
        }
        return list;
    }
}
