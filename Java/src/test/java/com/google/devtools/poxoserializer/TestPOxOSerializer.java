package com.google.devtools.poxoserializer;

import com.google.devtools.poxoserializer.POxOSerializer;
import com.google.devtools.poxoserializer.exception.POxOSerializerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPOxOSerializer {

  PrimitiveClassesContainer classToTest;

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
    
    Map<String,Double> map = new HashMap<String,Double>();
    map.put("A", 123.4560);
    map.put("B", 456.7890);
    map.put("C", 789.1230);
    map.put("D", 147.2580);
    
    classToTest.setInts(ints);
    classToTest.setMap(map);
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
    assertEquals(retB.isBoCanNull(), classToTest.isBoCanNull());
    assertEquals(retB.getfCanNull(), classToTest.getfCanNull(), 0.0000000001);
    assertEquals(retB.getdCanNull(), classToTest.getdCanNull(), 0.0000000001);
    
    assertEquals(retB.getInts().size(), classToTest.getInts().size());
    assertEquals(retB.getMap().size(), classToTest.getMap().size());
  }
}
