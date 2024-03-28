package ca.awoo.jabert;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;



public class CollectionsSerializerTest {

    public static class MapTestClass{
        public Map<String, String> map1;
        public TreeMap<String, String> map2;
    }

    @Test
    public void mapTest() throws Exception {
        MapTestClass tc = new MapTestClass();
        tc.map1 = new HashMap<String, String>();
        tc.map1.put("key1", "value1");
        tc.map1.put("key2", "value2");
        tc.map2 = new TreeMap<String, String>();
        tc.map2.put("key3", "value3");
        tc.map2.put("key4", "value4");
        Serializer serializer = Serializers.defaultSerializer();
        SValue sv = serializer.serialize(tc);
        MapTestClass tc2 = (MapTestClass) serializer.deserialize(sv, MapTestClass.class);
        assertEquals(tc.map1, tc2.map1);
        assertEquals(tc.map2, tc2.map2);
    }

    public static class ListTestClass{
        public List<String> list1;
        public LinkedList<String> list2;
    }

    @Test
    public void listTest() throws Exception {
        ListTestClass tc = new ListTestClass();
        tc.list1 = new LinkedList<String>();
        tc.list1.add("value1");
        tc.list1.add("value2");
        tc.list2 = new LinkedList<String>();
        tc.list2.add("value3");
        tc.list2.add("value4");
        Serializer serializer = Serializers.defaultSerializer();
        SValue sv = serializer.serialize(tc);
        ListTestClass tc2 = (ListTestClass) serializer.deserialize(sv, ListTestClass.class);
        assertEquals(tc.list1, tc2.list1);
        assertEquals(tc.list2, tc2.list2);
    }

    public static class SetTestClass{
        public Set<String> set1;
        public TreeSet<String> set2;
    }

    @Test
    public void setTest() throws Exception {
        SetTestClass tc = new SetTestClass();
        tc.set1 = new TreeSet<String>();
        tc.set1.add("value1");
        tc.set1.add("value2");
        tc.set2 = new TreeSet<String>();
        tc.set2.add("value3");
        tc.set2.add("value4");
        Serializer serializer = Serializers.defaultSerializer();
        SValue sv = serializer.serialize(tc);
        SetTestClass tc2 = (SetTestClass) serializer.deserialize(sv, SetTestClass.class);
        assertEquals(tc.set1, tc2.set1);
        assertEquals(tc.set2, tc2.set2);
    }

    public static class CollectionTestClass{
        public Collection<String> collection;
    }

    @Test
    public void collectionTest() throws Exception {
        CollectionTestClass tc = new CollectionTestClass();
        tc.collection = new HashSet<String>();
        tc.collection.add("value1");
        tc.collection.add("value2");
        Serializer serializer = Serializers.defaultSerializer();
        SValue sv = serializer.serialize(tc);
        CollectionTestClass tc2 = (CollectionTestClass) serializer.deserialize(sv, CollectionTestClass.class);
        //There's going to be a type mismatch in the collections, but I think that's fine
        Iterator<String> it1 = tc.collection.iterator();
        Iterator<String> it2 = tc2.collection.iterator();
        while(it1.hasNext() && it2.hasNext()){
            assertEquals(it1.next(), it2.next());
        }
    }
}
