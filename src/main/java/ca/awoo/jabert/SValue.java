package ca.awoo.jabert;

import java.util.HashMap;
import java.util.Map;

public abstract class SValue {
    public static class SNumber extends SValue {
        public final Number value;
        public SNumber(Number value) {
            this.value = value;
        }

        public int intValue() {
            return value.intValue();
        }

        public double doubleValue() {
            return value.doubleValue();
        }

        public long longValue() {
            return value.longValue();
        }

        public float floatValue() {
            return value.floatValue();
        }

        public short shortValue() {
            return value.shortValue();
        }

        public byte byteValue() {
            return value.byteValue();
        }
    }
    public static class SString extends SValue {
        public final String value;
        public SString(String value) {
            this.value = value;
        }
    }
    public static class SList extends SValue {
        public final SValue[] value;
        public SList(SValue[] value) {
            this.value = value;
        }
    }
    public static class SBool extends SValue {
        public final boolean value;
        public SBool(boolean value) {
            this.value = value;
        }
    }
    public static class SObject extends SValue {
        public final Map<String, SValue> value;
        public SObject(Map<String, SValue> value) {
            this.value = value;
        }

        public SObject(){
            this.value = new HashMap<String, SValue>();
        }

        public SValue get(String key) {
            return value.get(key);
        }

        public boolean has(String key) {
            return value.containsKey(key);
        }

        public void put(String key, SValue value) {
            this.value.put(key, value);
        }
    }
    public static class SNull extends SValue {
    }
}
