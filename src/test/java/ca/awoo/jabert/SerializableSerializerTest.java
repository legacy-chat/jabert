package ca.awoo.jabert;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import ca.awoo.jabert.SValue.*;

public class SerializableSerializerTest {

    public static class TestClass implements Serializable {
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

        public SValue serialize() throws SerializationException {
            return new SObject(new HashMap<String, SValue>() { {
                put("x", new SNumber(x));
                put("y", new SString(y));
                put("z", new SList(new SValue[] { new SNumber(z[0]), new SNumber(z[1]) }));
            } });
        }

        public void deserialize(SValue sv) throws SerializationException {
            SObject so = (SObject) sv;
            x = ((SNumber) so.get("x")).value.intValue();
            y = ((SString) so.get("y")).value;
            SList sl = (SList) so.get("z");
            z = new double[sl.value.length];
            for (int i = 0; i < sl.value.length; i++) {
                z[i] = ((SNumber) sl.value[i]).value.doubleValue();
            }
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
    public void testSerialize() throws Exception {
        TestClass tc = new TestClass(5, "hello", new double[] { 1.0, 2.0 });
        SerializableSerializer ss = new SerializableSerializer();
        SValue sv = ss.serialize(tc);
        TestClass tc2 = (TestClass) ss.deserialize(sv, TestClass.class);
        assertEquals(tc, tc2);
    }
}
