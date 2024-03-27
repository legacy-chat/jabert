package ca.awoo.jabert;

import java.util.Arrays;

import org.junit.Test;

import ca.awoo.fwoabl.function.Predicate;

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

}
