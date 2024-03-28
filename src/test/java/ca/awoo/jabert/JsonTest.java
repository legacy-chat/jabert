package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

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
}
