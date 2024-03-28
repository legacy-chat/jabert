package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.awoo.jabert.SValue.*;
import ca.awoo.praser.ParseException;

public class JsonTest {
    public static class TestClass{
        public String name;
        public int age;
        public TestClass(String name, int age){
            this.name = name;
            this.age = age;
        }
        public TestClass(){}
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + age;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestClass other = (TestClass) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (age != other.age)
                return false;
            return true;
        }
    }

    @Test
    public void testJson() throws Exception{
        TestClass test = new TestClass("test", 10);
        Serializer s = Serializers.defaultSerializer();
        Format json = new JsonFormat("UTF-8");
        SValue sv = s.serialize(test);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(sv, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv2 = json.parse(in);
        TestClass test2 = (TestClass)s.deserialize(sv2, TestClass.class);
        assertEquals(test, test2);
    }

    @Test
    public void testList() throws Exception{
        Format json = new JsonFormat("UTF-8");
        SList list = new SList(new SNumber(1), new SNumber(2), new SNumber(3), new SNumber(4), new SNumber(5));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(list, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv = json.parse(in);
        SList list2 = (SList)sv;
        assertEquals(5, list2.size());
        for(int i = 0; i < 5; i++){
            assertEquals(i+1, ((SNumber)list2.get(i)).intValue());
        }
    }

    @Test
    public void testEmptyList() throws Exception{
        Format json = new JsonFormat("UTF-8");
        SList list = new SList();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(list, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv = json.parse(in);
        SList list2 = (SList)sv;
        assertEquals(0, list2.size());
    }

    @Test
    public void testObject() throws Exception{
        Format json = new JsonFormat("UTF-8");
        SObject obj = new SObject();
        obj.put("name", new SString("test"));
        obj.put("age", new SNumber(10));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(obj, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv = json.parse(in);
        SObject obj2 = (SObject)sv;
        assertEquals("test", ((SString)obj2.get("name")).value);
        assertEquals(10, ((SNumber)obj2.get("age")).intValue());
    }

    @Test
    public void testEmptyObject() throws Exception{
        Format json = new JsonFormat("UTF-8");
        SObject obj = new SObject();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(obj, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv = json.parse(in);
        SObject obj2 = (SObject)sv;
        assertEquals(0, obj2.size());
    }

    @Test
    public void testNull() throws Exception{
        Format json = new JsonFormat("UTF-8");
        SNull n = new SNull();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        json.emit(n, out);
        System.out.println(out.toString("UTF-8"));
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        SValue sv = json.parse(in);
        assertEquals(n, sv);
    }

    private List<SValue> everyValue(int maxDepth){
        List<SValue> everyValue = new ArrayList<SValue>();
        everyValue.add(new SNumber(0));
        everyValue.add(new SNumber(1));
        everyValue.add(new SNumber(-1));
        everyValue.add(new SNumber(0.0));
        everyValue.add(new SNumber(1.0));
        everyValue.add(new SNumber(-1.0));
        everyValue.add(new SNumber(0.1));
        everyValue.add(new SNumber(1.1));
        everyValue.add(new SNumber(-1.1));
        everyValue.add(new SString(""));
        everyValue.add(new SString(" "));
        everyValue.add(new SString("a"));
        everyValue.add(new SString("a "));
        everyValue.add(new SString(" a"));
        everyValue.add(new SString(" a "));
        everyValue.add(new SString("a\""));
        everyValue.add(new SString("a\\"));
        everyValue.add(new SString("a/"));
        everyValue.add(new SString("a\b"));
        everyValue.add(new SString("a\f"));
        everyValue.add(new SString("a\n"));
        everyValue.add(new SString("a\r"));
        everyValue.add(new SString("a\t"));
        everyValue.add(new SString("a\u0000"));
        everyValue.add(new SString("a\u0001"));
        everyValue.add(new SString("a\u0002"));
        everyValue.add(new SString("a\u0003"));
        everyValue.add(new SBool(true));
        everyValue.add(new SBool(false));
        everyValue.add(new SNull());
        everyValue.add(new SObject());
        everyValue.add(new SList());
        if(maxDepth > 0){
            SList list = new SList();
            for(SValue value : everyValue(maxDepth - 1)){
                list.add(value);
            }
            everyValue.add(list);
            SObject object = new SObject();
            for(SValue value : everyValue(maxDepth - 1)){
                object.put(value.toString(), value);
            }
            everyValue.add(object);
        }
        return everyValue;
    }

    @Test
    public void testEveryValue() throws Exception{
        List<SValue> everyValue = everyValue(2);
        Format json = new JsonFormat("UTF-8");
        for(SValue value : everyValue){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            json.emit(value, out);
            System.out.println(out.toString("UTF-8"));
            byte[] bytes = out.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            try{
                SValue sv = json.parse(in);
                assertEquals("Value survived", value, sv);
            }catch(FormatException e){
                fail("FormatException on " + value + ": " + e.getMessage());
            }
        }
    }
}
