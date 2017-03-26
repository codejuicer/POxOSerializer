package org.codejuicer.poxoserializer;

import java.util.List;
import java.util.Map;

public class GenericTypeContainer {
    private List<String> list;

    private Map<String, String> map;

    private List<Map<String, String>> listMap;

    private List<List<String>> listList;

    private Map<String, List<Integer>> mapList;

    private List<TestObjectClass> listObject;
    
    public List<TestObjectClass> getListObject() {
        return listObject;
    }

    public void setListObject(List<TestObjectClass> listObject) {
        this.listObject = listObject;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public List<Map<String, String>> getListMap() {
        return listMap;
    }

    public void setListMap(List<Map<String, String>> listMap) {
        this.listMap = listMap;
    }

    public List<List<String>> getListList() {
        return listList;
    }

    public void setListList(List<List<String>> listList) {
        this.listList = listList;
    }

    public Map<String, List<Integer>> getMapList() {
        return mapList;
    }

    public void setMapList(Map<String, List<Integer>> mapList) {
        this.mapList = mapList;
    }
}
