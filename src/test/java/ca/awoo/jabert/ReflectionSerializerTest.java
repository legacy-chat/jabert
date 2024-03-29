package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import ca.awoo.fwoabl.Optional;
import ca.awoo.fwoabl.function.Predicate;
import ca.awoo.jabert.SValue.SObject;

public class ReflectionSerializerTest {

    public static class TestClass {
        private int x;
        private String y;
        private double[] z;

        public TestClass(int x, String y, double[] z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        protected TestClass() {
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + ((y == null) ? 0 : y.hashCode());
            result = prime * result + Arrays.hashCode(z);
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
            if (x != other.x)
                return false;
            if (y == null) {
                if (other.y != null)
                    return false;
            } else if (!y.equals(other.y))
                return false;
            if (!Arrays.equals(z, other.z))
                return false;
            return true;
        }
    }

    @Test
    public void testReflectionSerializer() throws SerializationException {
        CompoundSerializer cs = new CompoundSerializer();
        PrimativeSerializer ps = new PrimativeSerializer();
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return  t.equals(Long.class) ||
                        t.equals(Integer.class) ||
                        t.equals(Short.class) ||
                        t.equals(Byte.class) ||
                        t.equals(Double.class) ||
                        t.equals(Float.class) ||
                        t.equals(Boolean.class) ||
                        t.equals(Character.class) ||
                        t.equals(String.class);
            }
        }, ps);
        ArraySerializer as = new ArraySerializer(cs);
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return t.isArray();
            }
        }, as);
        ReflectionSerializer rs = new ReflectionSerializer(cs);
        cs.setDefaultSerializer(rs);
        TestClass tc = new TestClass(5, "Hello", new double[] { 1.0, 2.0 });
        SValue sv = cs.serialize(tc);
        TestClass tc2 = (TestClass) cs.deserialize(sv, TestClass.class);
        assert(tc.equals(tc2));
    }

    public static class NullTestClass {
        @SerializeAsNull
        public Optional<Integer> x;
    }

    @Test
    public void serializeNoneNull() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        NullTestClass tc = new NullTestClass();
        tc.x = new Optional.None<Integer>();
        SValue sv = s.serialize(tc);
        assertTrue(sv instanceof SObject);
        SObject so = (SObject) sv;
        assertTrue(so.get("x") instanceof SValue.SNull);
    }

    @Test
    public void serializeSome() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        NullTestClass tc = new NullTestClass();
        tc.x = new Optional.Some<Integer>(5);
        SValue sv = s.serialize(tc);
        assertTrue(sv instanceof SObject);
        SObject so = (SObject) sv;
        assertTrue(so.get("x") instanceof SValue.SNumber);
        assertEquals(so.get("x"), new SValue.SNumber(5));
    }

    @Test
    public void deserializeNoneNull() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        SObject so = new SObject();
        so.put("x", new SValue.SNull());
        NullTestClass tc = (NullTestClass) s.deserialize(so, NullTestClass.class);
        assertTrue(tc.x instanceof Optional.None);
    }

    @Test
    public void deserializeSome() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        SObject so = new SObject();
        so.put("x", new SValue.SNumber(5));
        NullTestClass tc = (NullTestClass) s.deserialize(so, NullTestClass.class);
        assertTrue(tc.x instanceof Optional.Some);
        assertEquals(tc.x.get(), new Integer(5));
    }

    @Test
    public void deserializeNoneNothing() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        SObject so = new SObject();
        NullTestClass tc = (NullTestClass) s.deserialize(so, NullTestClass.class);
        assertTrue(tc.x instanceof Optional.None);
    }

    public static class BasicClass{
        public String s;
    }

    @Test
    public void nullFieldTest() throws Exception {
        Serializer s = Serializers.defaultSerializer();
        BasicClass bc = new BasicClass();
        SValue sv = s.serialize(bc);
        assertTrue(sv instanceof SObject);
        SObject so = (SObject) sv;
        assertTrue(so.has("s"));
        assertTrue(so.get("s") instanceof SValue.SNull);
    }

}
