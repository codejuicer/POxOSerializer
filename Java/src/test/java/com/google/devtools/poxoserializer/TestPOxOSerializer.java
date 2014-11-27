package com.google.devtools.poxoserializer;

import com.google.devtools.poxoserializer.exception.POxOSerializerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPOxOSerializer {

  PrimitiveClassesContainer classToTest;

  Map<String,Double> map;
  
  @Before
  public void initialize() {
    classToTest = new PrimitiveClassesContainer();

    classToTest.setbNotNull((byte) 0x7F);
    classToTest.setcNotNull('c');
    classToTest.setsNotNull((short) 32000);
    classToTest.setiNotNull(1000000000);
    classToTest.setlNotNull(4000000000L);
    classToTest.setSt("test ascii string");
    classToTest.setStUTF8("SãoVicente");
    classToTest.setBoNotNull(true);
    classToTest.setTimestamp(Calendar.getInstance().getTime());
    classToTest.setfNotNull(125.758F);
    classToTest.setdNotNull(6546874.16513154644);
    
    List<Integer> ints = new ArrayList<Integer>();
    ints.add(123);
    ints.add(456);
    ints.add(789);
    
    List<String> strings = new ArrayList<String>();
    strings.add("pippo");
    strings.add("pluto");
    strings.add("paperino");
    
    map = new HashMap<String,Double>();
    map.put("A", 123.4560);
    map.put("B", 456.7890);
    map.put("C", 789.1230);
    map.put("D", 147.2580);
    
    classToTest.setInts(ints);
    classToTest.setStrings(strings);
    classToTest.setMap(map);
    
    
    List<Map<String, List<Integer>>> nestedCollections = new ArrayList<Map<String,List<Integer>>>();
    Map<String,List<Integer>> map2 = new HashMap<String, List<Integer>>();
    map2.put("test", ints);
    nestedCollections.add(map2);
    classToTest.setNestedCollections(nestedCollections);
  }

  @After
  public void close() {
  }

  @Test
  public void test1() throws POxOSerializerException {

    POxOSerializer serializer = new POxOSerializer();
    PrimitiveClassesContainer retB = null;
    retB = (PrimitiveClassesContainer) serializer.deserialize(serializer.serialize(classToTest));

    assertEquals(retB.getbNotNull(), classToTest.getbNotNull());
    assertEquals(retB.getcNotNull(), classToTest.getcNotNull());
    assertEquals(retB.getsNotNull(), classToTest.getsNotNull());
    assertEquals(retB.getiNotNull(), classToTest.getiNotNull());
    assertEquals(retB.getlNotNull(), classToTest.getlNotNull());
    assertEquals(retB.getSt(), classToTest.getSt());
    assertEquals(retB.getStUTF8(), classToTest.getStUTF8());
    assertEquals(retB.isBoNotNull(), classToTest.isBoNotNull());
    assertEquals(retB.getTimestamp(), classToTest.getTimestamp());
    assertEquals(retB.getfNotNull(), classToTest.getfNotNull(), 0.0000000001);
    assertEquals(retB.getdNotNull(), classToTest.getdNotNull(), 0.0000000001);

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
    assertEquals(retB.getNestedCollections().get(0).size(), classToTest.getNestedCollections().get(0).size());
    assertEquals(retB.getNestedCollections().get(0).get("A"), classToTest.getNestedCollections().get(0).get("A"));
  }
}
