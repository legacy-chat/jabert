package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import ca.awoo.jabert.SValue.*;

public class SValueSerializerTest {

    public static class TestClass{
        public int a;
        public SString b;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + a;
            result = prime * result + ((b == null) ? 0 : b.hashCode());
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
            if (a != other.a)
                return false;
            if (b == null) {
                if (other.b != null)
                    return false;
            } else if (!b.equals(other.b))
                return false;
            return true;
        }

        
    }

    @Test
    public void testSerialize() throws Exception{
        TestClass tc = new TestClass();
        tc.a = 5;
        tc.b = new SString("Hello, World!");
        Serializer s = Serializers.defaultSerializer();
        SValue sv = s.serialize(tc);
        TestClass tc2 = (TestClass)s.deserialize(sv, TestClass.class);
        assertEquals(tc, tc2);
    }

    @Test
    public void testRawDeserialize() throws Exception {
        String json = "{\"a\":5,\"b\":\"Hello, World!\"}";
        Serializer s = Serializers.defaultSerializer();
        Format f = new JsonFormat("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
        SValue sv = f.parse(bais);
        TestClass tc = (TestClass)s.deserialize(sv, TestClass.class);
        assertEquals(tc.a, 5);
        assertEquals(tc.b, new SString("Hello, World!"));
    }
}
