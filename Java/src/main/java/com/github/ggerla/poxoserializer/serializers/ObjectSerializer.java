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

package com.github.ggerla.poxoserializer.serializers;

import com.github.ggerla.poxoserializer.POxOSerializerUtil;
import com.github.ggerla.poxoserializer.exception.POxOSerializerException;
import com.github.ggerla.poxoserializer.io.POxOPrimitiveDecoder;
import com.github.ggerla.poxoserializer.io.POxOPrimitiveEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ObjectSerializer extends GenericClassSerializer {

	private POxOSerializerUtil serializerUtil;

	private Map<String, FieldsSerializer> classFieldSerializerMap;

	private Map<String, FieldSerializerUtil[]> fieldsSerializersMap;

	public ObjectSerializer(POxOSerializerUtil serializerUtil) {
		super(true);
		classFieldSerializerMap = new TreeMap<String, FieldsSerializer>();
		fieldsSerializersMap = new TreeMap<String, FieldSerializerUtil[]>();
		this.serializerUtil = serializerUtil;
	}

	@Override
	public Object read(POxOPrimitiveDecoder decoder)
			throws POxOSerializerException {
		Object obj = null;
		byte isNull = decoder.readByte();
		if (isNull == 0x00) {
			return obj;
		}
		try {
			String className = decoder.readString();

			if (className != null) {
				Class<?> type = serializerUtil.getClassFromName(className);

				FieldsSerializer fieldsSerializer = classFieldSerializerMap
						.get(className);
				if (fieldsSerializer == null) {
					fieldsSerializer = new FieldsSerializer(type, this);
					retrieveOrderedFieldsList(type);
					classFieldSerializerMap.put(className, fieldsSerializer);
				}
				obj = serializerUtil.createNewInstance(type);

				fieldsSerializer.read(decoder, obj);
			}
		} catch (POxOSerializerException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new POxOSerializerException(
					"Error during object deserializing.", e);
		}

		return obj;
	}

	@Override
	public void write(POxOPrimitiveEncoder encoder, Object obj)
			throws POxOSerializerException {
		if (obj == null) {
			encoder.write(0x00);
			return;
		} else {
			encoder.write(0x01);
		}
		Class<?> type = obj.getClass();

		String name = serializerUtil.getNameFromClass(type);

		encoder.writeString(name);

		FieldsSerializer fieldsSerializer = classFieldSerializerMap.get(name);
		if (fieldsSerializer == null) {
			fieldsSerializer = new FieldsSerializer(type, this);
			retrieveOrderedFieldsList(type);
			classFieldSerializerMap.put(type.getName(), fieldsSerializer);
		}

		fieldsSerializer.write(encoder, obj);
	}

	private void retrieveOrderedFieldsList(Class<?> type)
			throws POxOSerializerException {
		if (fieldsSerializersMap.get(type.getName()) == null) {
			List<FieldSerializerUtil> allFieldsSerializer = new ArrayList<FieldSerializerUtil>();
			Class<?> nextClass = type;
			while (nextClass != Object.class) {
				Field[] declaredFields = nextClass.getDeclaredFields();
				if (declaredFields != null) {
					for (Field f : declaredFields) {
						if (Modifier.isStatic(f.getModifiers()))
							continue;
						allFieldsSerializer.add(new FieldSerializerUtil(f,
								serializerUtil.getFieldSerializer(f)));
					}
				}
				nextClass = nextClass.getSuperclass();
			}

			Collections.sort(allFieldsSerializer,
					new Comparator<FieldSerializerUtil>() {

						@Override
						public int compare(FieldSerializerUtil object1,
								FieldSerializerUtil object2) {
							return object1.getField().getName()
									.compareTo(object2.getField().getName());
						}
					});

			fieldsSerializersMap.put(type.getName(),
					allFieldsSerializer.toArray(new FieldSerializerUtil[0]));
		}
	}

	public FieldSerializerUtil[] getFieldsSerializers(Class<?> type) {
		return fieldsSerializersMap.get(type.getName());
	}
}
