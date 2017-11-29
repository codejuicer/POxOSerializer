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

package org.codejuicer.poxoserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codejuicer.poxoserializer.exception.POxOSerializerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPOxOSerializer {

    PrimitiveClassesContainer classToTest;

    Map<String, Double> map;

    @Before
    public void initialize() {
        classToTest = new PrimitiveClassesContainer();

        classToTest.setbNotNull((byte)0x7F);
        classToTest.setcNotNull('c');
        classToTest.setsNotNull((short)32000);
        classToTest.setiNotNull(1000000000);
        classToTest.setlNotNull(4000000000L);
        classToTest.setSt("test ascii string");
        classToTest.setStUTF8("SãoVicente");
        classToTest.setBoNotNull(true);
        Date date = Calendar.getInstance().getTime();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        classToTest.setTimestamp(date);
        classToTest.setZonedDateTime(zonedDateTime);
        classToTest.setLocalDateTime(zonedDateTime.toLocalDateTime());
        classToTest.setfNotNull(125.758F);
        classToTest.setdNotNull(6546874.16513154644);
        classToTest.setEnumValue(TestEnum.NOTWORK);

        List<Integer> ints = new ArrayList<Integer>();
        ints.add(123);
        ints.add(456);
        ints.add(789);

        List<String> strings = new ArrayList<String>();
        strings.add("pippo");
        strings.add("pluto");
        strings.add("paperino");

        map = new HashMap<String, Double>();
        map.put("A", 123.4560);
        map.put("B", 456.7890);
        map.put("C", 789.1230);
        map.put("D", 147.2580);

        classToTest.setInts(ints);
        classToTest.setStrings(strings);
        classToTest.setMap(map);

        List<Map<String, List<Integer>>> nestedCollections = new ArrayList<Map<String, List<Integer>>>();
        Map<String, List<Integer>> map2 = new HashMap<String, List<Integer>>();
        map2.put("test", ints);
        nestedCollections.add(map2);
        classToTest.setNestedCollections(nestedCollections);

        NestedObjectClass nestedClass = new NestedObjectClass();
        nestedClass.setIndex(50);
        List<NestedObjectClass> listNestedClass = new ArrayList<NestedObjectClass>();
        listNestedClass.add(nestedClass);
        classToTest.setNestedClass(listNestedClass);

        Map<String, Object> genericValueMap = new HashMap<String, Object>();
        genericValueMap.put("bool", true);
        genericValueMap.put("int", new Integer(74));
        genericValueMap.put("string", "pippo");
        genericValueMap.put("object", nestedClass);
        genericValueMap.put("enum", TestEnum.WORK);
        classToTest.setGenericValueMap(genericValueMap);
    }

    @After
    public void close() {
    }

    private void writeDataToFile(String fileName, byte[] data) throws IOException {
        FileOutputStream stream = new FileOutputStream(new File(fileName));
        stream.write(data, 0, data.length);
        stream.flush();
        stream.close();
    }

    @Test
    public void test1() throws POxOSerializerException, IOException {

        POxOSerializer serializer = new POxOSerializer();
        PrimitiveClassesContainer retB = null;
        byte[] serialized = serializer.serialize(classToTest);
        retB = (PrimitiveClassesContainer)serializer.deserialize(serialized);

        assertEquals(retB.getbNotNull(), classToTest.getbNotNull());
        assertEquals(retB.getcNotNull(), classToTest.getcNotNull());
        assertEquals(retB.getsNotNull(), classToTest.getsNotNull());
        assertEquals(retB.getiNotNull(), classToTest.getiNotNull());
        assertEquals(retB.getlNotNull(), classToTest.getlNotNull());
        assertEquals(retB.getSt(), classToTest.getSt());
        assertEquals(retB.getStUTF8(), classToTest.getStUTF8());
        assertEquals(retB.isBoNotNull(), classToTest.isBoNotNull());
        assertEquals(retB.getTimestamp(), classToTest.getTimestamp());
        assertEquals(retB.getZonedDateTime(), classToTest.getZonedDateTime());
        assertEquals(retB.getLocalDateTime(), classToTest.getLocalDateTime());
        assertEquals(retB.getfNotNull(), classToTest.getfNotNull(), 0.0000000001);
        assertEquals(retB.getdNotNull(), classToTest.getdNotNull(), 0.0000000001);
        assertEquals(retB.getEnumValue(), classToTest.getEnumValue());

        assertEquals(retB.getbCanNull(), classToTest.getbCanNull());
        assertEquals(retB.getcCanNull(), classToTest.getcCanNull());
        assertEquals(retB.getsCanNull(), classToTest.getsCanNull());
        assertEquals(retB.getiCanNull(), classToTest.getiCanNull());
        assertEquals(retB.getlCanNull(), classToTest.getlCanNull());
        assertNull(retB.getBoCanNull());
        assertNull(retB.getfCanNull());
        assertNull(retB.getdCanNull());

        assertEquals(retB.getInts().size(), classToTest.getInts().size());
        assertEquals(retB.getMap().size(), classToTest.getMap().size());
        assertEquals(retB.getNestedCollections().size(), classToTest.getNestedCollections().size());
        assertEquals(retB.getNestedCollections().get(0).size(),
                     classToTest.getNestedCollections().get(0).size());
        assertEquals(retB.getNestedCollections().get(0).get("test").get(2),
                     classToTest.getNestedCollections().get(0).get("test").get(2));
        assertEquals(retB.getNestedClass().get(0).getIndex(), classToTest.getNestedClass().get(0).getIndex());
        assertEquals(retB.getGenericValueMap().get("bool"), classToTest.getGenericValueMap().get("bool"));
        assertEquals(retB.getGenericValueMap().get("int"), classToTest.getGenericValueMap().get("int"));
        assertEquals(retB.getGenericValueMap().get("string"), classToTest.getGenericValueMap().get("string"));
        assertEquals(true, retB.getGenericValueMap().get("object") instanceof NestedObjectClass);
        assertEquals(((NestedObjectClass)retB.getGenericValueMap().get("object")).getIndex(),
                     ((NestedObjectClass)classToTest.getGenericValueMap().get("object")).getIndex());
    }

    @Test
    public void testListSerializer() throws POxOSerializerException, IOException {

        List<Object> list = new ArrayList<Object>();
        list.add(classToTest);
        list.add(classToTest);
        POxOSerializer serializer = new POxOSerializer();

        byte[] serialized = serializer.serialize(list);
        List<Object> listB = (List<Object>)serializer.deserialize(serialized);
        assertEquals(listB.size(), list.size());

        PrimitiveClassesContainer retB = (PrimitiveClassesContainer)listB.get(0);
        assertEquals(retB.getbNotNull(), classToTest.getbNotNull());
        assertEquals(retB.getcNotNull(), classToTest.getcNotNull());
        assertEquals(retB.getsNotNull(), classToTest.getsNotNull());
        assertEquals(retB.getiNotNull(), classToTest.getiNotNull());
        assertEquals(retB.getlNotNull(), classToTest.getlNotNull());
        assertEquals(retB.getSt(), classToTest.getSt());
        assertEquals(retB.getStUTF8(), classToTest.getStUTF8());
        assertEquals(retB.isBoNotNull(), classToTest.isBoNotNull());
        assertEquals(retB.getTimestamp(), classToTest.getTimestamp());
        assertEquals(retB.getZonedDateTime(), classToTest.getZonedDateTime());
        assertEquals(retB.getLocalDateTime(), classToTest.getLocalDateTime());
        assertEquals(retB.getfNotNull(), classToTest.getfNotNull(), 0.0000000001);
        assertEquals(retB.getdNotNull(), classToTest.getdNotNull(), 0.0000000001);
        assertEquals(retB.getEnumValue(), classToTest.getEnumValue());

        assertEquals(retB.getbCanNull(), classToTest.getbCanNull());
        assertEquals(retB.getcCanNull(), classToTest.getcCanNull());
        assertEquals(retB.getsCanNull(), classToTest.getsCanNull());
        assertEquals(retB.getiCanNull(), classToTest.getiCanNull());
        assertEquals(retB.getlCanNull(), classToTest.getlCanNull());
        assertNull(retB.getBoCanNull());
        assertNull(retB.getfCanNull());
        assertNull(retB.getdCanNull());

        assertEquals(retB.getInts().size(), classToTest.getInts().size());
        assertEquals(retB.getMap().size(), classToTest.getMap().size());
        assertEquals(retB.getNestedCollections().size(), classToTest.getNestedCollections().size());
        assertEquals(retB.getNestedCollections().get(0).size(),
                     classToTest.getNestedCollections().get(0).size());
        assertEquals(retB.getNestedCollections().get(0).get("test").get(2),
                     classToTest.getNestedCollections().get(0).get("test").get(2));
        assertEquals(retB.getNestedClass().get(0).getIndex(), classToTest.getNestedClass().get(0).getIndex());
        assertEquals(retB.getGenericValueMap().get("bool"), classToTest.getGenericValueMap().get("bool"));
        assertEquals(retB.getGenericValueMap().get("int"), classToTest.getGenericValueMap().get("int"));
        assertEquals(retB.getGenericValueMap().get("string"), classToTest.getGenericValueMap().get("string"));
        assertEquals(true, retB.getGenericValueMap().get("object") instanceof NestedObjectClass);
        assertEquals(((NestedObjectClass)retB.getGenericValueMap().get("object")).getIndex(),
                     ((NestedObjectClass)classToTest.getGenericValueMap().get("object")).getIndex());
    }

    @Test
    public void testMapSerializer() throws POxOSerializerException, IOException {

        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("key", classToTest);
        POxOSerializer serializer = new POxOSerializer();

        byte[] serialized = serializer.serialize(map);
        Map<Object, Object> mapB = (Map<Object, Object>)serializer.deserialize(serialized);
        assertEquals(mapB.size(), map.size());

        Entry<Object, Object> entry = map.entrySet().iterator().next();
        assertEquals(entry.getKey(), "key");
        PrimitiveClassesContainer retB = (PrimitiveClassesContainer)entry.getValue();
        assertEquals(retB.getbNotNull(), classToTest.getbNotNull());
        assertEquals(retB.getcNotNull(), classToTest.getcNotNull());
        assertEquals(retB.getsNotNull(), classToTest.getsNotNull());
        assertEquals(retB.getiNotNull(), classToTest.getiNotNull());
        assertEquals(retB.getlNotNull(), classToTest.getlNotNull());
        assertEquals(retB.getSt(), classToTest.getSt());
        assertEquals(retB.getStUTF8(), classToTest.getStUTF8());
        assertEquals(retB.isBoNotNull(), classToTest.isBoNotNull());
        assertEquals(retB.getTimestamp(), classToTest.getTimestamp());
        assertEquals(retB.getZonedDateTime(), classToTest.getZonedDateTime());
        assertEquals(retB.getLocalDateTime(), classToTest.getLocalDateTime());
        assertEquals(retB.getfNotNull(), classToTest.getfNotNull(), 0.0000000001);
        assertEquals(retB.getdNotNull(), classToTest.getdNotNull(), 0.0000000001);
        assertEquals(retB.getEnumValue(), classToTest.getEnumValue());

        assertEquals(retB.getbCanNull(), classToTest.getbCanNull());
        assertEquals(retB.getcCanNull(), classToTest.getcCanNull());
        assertEquals(retB.getsCanNull(), classToTest.getsCanNull());
        assertEquals(retB.getiCanNull(), classToTest.getiCanNull());
        assertEquals(retB.getlCanNull(), classToTest.getlCanNull());
        assertNull(retB.getBoCanNull());
        assertNull(retB.getfCanNull());
        assertNull(retB.getdCanNull());

        assertEquals(retB.getInts().size(), classToTest.getInts().size());
        assertEquals(retB.getMap().size(), classToTest.getMap().size());
        assertEquals(retB.getNestedCollections().size(), classToTest.getNestedCollections().size());
        assertEquals(retB.getNestedCollections().get(0).size(),
                     classToTest.getNestedCollections().get(0).size());
        assertEquals(retB.getNestedCollections().get(0).get("test").get(2),
                     classToTest.getNestedCollections().get(0).get("test").get(2));
        assertEquals(retB.getNestedClass().get(0).getIndex(), classToTest.getNestedClass().get(0).getIndex());
        assertEquals(retB.getGenericValueMap().get("bool"), classToTest.getGenericValueMap().get("bool"));
        assertEquals(retB.getGenericValueMap().get("int"), classToTest.getGenericValueMap().get("int"));
        assertEquals(retB.getGenericValueMap().get("string"), classToTest.getGenericValueMap().get("string"));
        assertEquals(true, retB.getGenericValueMap().get("object") instanceof NestedObjectClass);
        assertEquals(((NestedObjectClass)retB.getGenericValueMap().get("object")).getIndex(),
                     ((NestedObjectClass)classToTest.getGenericValueMap().get("object")).getIndex());
    }

    @Test
    public void testListMapStringStringSerializer() throws POxOSerializerException, IOException {
        List<Map<String, String>> testData = new ArrayList<Map<String, String>>();
        for (int i = 0; i < 2500; i++) {
            Map<String, String> mapData = new HashMap<String, String>();
            for (int j = 0; j < 16; j++) {
                mapData.put("key" + j, this.getClass().getSimpleName() + j);
            }
            testData.add(mapData);
        }

        GenericTypeContainer gt = new GenericTypeContainer();
        gt.setListMap(testData);
        POxOSerializer serializer = new POxOSerializer();

        long checkTime = System.currentTimeMillis();
        byte[] output = serializer.serialize(gt);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("serialization time " + checkTime);
        writeDataToFile("testListMap", output);

        checkTime = System.currentTimeMillis();
        GenericTypeContainer testDataCheck = (GenericTypeContainer)serializer.deserialize(output);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("deserialization time " + checkTime);
        assertEquals(testDataCheck.getListMap().size(), testData.size());
    }

    @Test
    public void testListListStringSerializer() throws POxOSerializerException, IOException {
        List<List<String>> testData = new ArrayList<List<String>>();
        for (int i = 0; i < 2500; i++) {
            List<String> mapData = new ArrayList<String>();
            for (int j = 0; j < 16; j++) {
                mapData.add(this.getClass().getSimpleName() + j);
            }
            testData.add(mapData);
        }

        POxOSerializer serializer = new POxOSerializer();

        long checkTime = System.currentTimeMillis();
        byte[] output = serializer.serialize(testData);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("serialization time " + checkTime);
        writeDataToFile("testListList", output);

        checkTime = System.currentTimeMillis();
        List<List<String>> testDataCheck = (List<List<String>>)serializer.deserialize(output);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("deserialization time " + checkTime);
        assertEquals(testDataCheck.size(), testData.size());
    }

    @Test
    public void testListObjectSerializer() throws POxOSerializerException, IOException {
        List<TestObjectClass> testData = new ArrayList<TestObjectClass>();
        for (int i = 0; i < 10000; i++) {
            TestObjectClass mapData = new TestObjectClass(this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName());
            testData.add(mapData);
        }

        POxOSerializer serializer = new POxOSerializer();

        long checkTime = System.currentTimeMillis();
        byte[] output = serializer.serialize(testData);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("serialization time " + checkTime);

        checkTime = System.currentTimeMillis();
        List<TestObjectClass> testDataCheck = (List<TestObjectClass>)serializer.deserialize(output);
        checkTime = System.currentTimeMillis() - checkTime;
        System.out.println("deserialization time " + checkTime);
        assertEquals(testDataCheck.size(), testData.size());
    }

    @Test
    public void testGenericTypeContainer() throws POxOSerializerException, IOException {
        List<TestObjectClass> testData = new ArrayList<TestObjectClass>();
        for (int i = 0; i < 10000; i++) {
            TestObjectClass mapData = new TestObjectClass(this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName(),
                                                          this.getClass().getSimpleName());
            testData.add(mapData);
        }

        GenericTypeContainer container = new GenericTypeContainer();
        container.setListObject(testData);

        POxOSerializer serializer = new POxOSerializer();
        byte[] output = serializer.serialize(container);
        writeDataToFile("testListObject", output);
        GenericTypeContainer testDataCheck = (GenericTypeContainer)serializer.deserialize(output);
        assertEquals(testDataCheck.getListObject().size(), testData.size());

        List<List<String>> testDataListList = new ArrayList<List<String>>();
        for (int i = 0; i < 10000; i++) {
            List<String> mapData = new ArrayList<String>();
            for (int j = 0; j < 16; j++) {
                mapData.add(this.getClass().getSimpleName() + j);
            }
            testDataListList.add(mapData);
        }

        container = new GenericTypeContainer();
        container.setListList(testDataListList);

        serializer = new POxOSerializer();
        output = serializer.serialize(container);
        writeDataToFile("testListList", output);
        GenericTypeContainer testDataCheckListList = (GenericTypeContainer)serializer.deserialize(output);
        assertEquals(testDataCheckListList.getListList().size(), testDataListList.size());

        List<Map<String, String>> testDataListMap = new ArrayList<Map<String, String>>();
        for (int i = 0; i < 10000; i++) {
            Map<String, String> mapData = new HashMap<String, String>();
            for (int j = 0; j < 16; j++) {
                mapData.put("key" + j, this.getClass().getSimpleName() + j);
            }
            testDataListMap.add(mapData);
        }

        container = new GenericTypeContainer();
        container.setListMap(testDataListMap);

        serializer = new POxOSerializer();
        output = serializer.serialize(container);
        writeDataToFile("testListMap", output);
        GenericTypeContainer testDataCheckListMap = (GenericTypeContainer)serializer.deserialize(output);
        assertEquals(testDataCheckListMap.getListMap().size(), testDataListMap.size());

        Map<String, List<Integer>> testDataMapList = new HashMap<String, List<Integer>>();
        for (int i = 0; i < 2100; i++) {
            List<Integer> mapData = new ArrayList<Integer>();
            for (int j = 0; j < 8; j++) {
                mapData.add(j);
            }
            testDataMapList.put("row" + i, mapData);
        }

        container = new GenericTypeContainer();
        container.setMapList(testDataMapList);

        serializer = new POxOSerializer();
        output = serializer.serialize(container);
        writeDataToFile("testMapList", output);
        GenericTypeContainer testDataCheckMapList = (GenericTypeContainer)serializer.deserialize(output);
        assertEquals(testDataCheckMapList.getMapList().size(), testDataMapList.size());
    }
}
