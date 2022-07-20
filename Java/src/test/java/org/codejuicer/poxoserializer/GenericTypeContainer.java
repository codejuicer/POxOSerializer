package org.codejuicer.poxoserializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericTypeContainer {
    private List<String> list;

    private Map<String, String> map;

    private List<Map<String, String>> listMap;

    private List<List<String>> listList;

    private Map<String, List<Integer>> mapList;

    private List<TestObjectClass> listObject;

    private Set<TestObjectClass> setObject;

    public List<String> getList() {
        return list;
    }

    public List<List<String>> getListList() {
        return listList;
    }

    public List<Map<String, String>> getListMap() {
        return listMap;
    }

    public List<TestObjectClass> getListObject() {
        return listObject;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public Map<String, List<Integer>> getMapList() {
        return mapList;
    }

    public Set<TestObjectClass> getSetObject() {
        return setObject;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public void setListList(List<List<String>> listList) {
        this.listList = listList;
    }

    public void setListMap(List<Map<String, String>> listMap) {
        this.listMap = listMap;
    }

    public void setListObject(List<TestObjectClass> listObject) {
        this.listObject = listObject;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public void setMapList(Map<String, List<Integer>> mapList) {
        this.mapList = mapList;
    }

    public void setSetObject(Set<TestObjectClass> setObject) {
        this.setObject = setObject;
    }
}
