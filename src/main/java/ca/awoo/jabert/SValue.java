package ca.awoo.jabert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        @Override
        public String toString() {
            return "SNumber(" + value.toString() + ")";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            SNumber other = (SNumber) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
                else
                    return true;
            }
            if(value.doubleValue() == other.value.doubleValue()){
                return true;
            }
            return value.equals(other.value);
        }
        
    }
    public static class SString extends SValue {
        public final String value;
        public SString(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SString(" + value + ")";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            SString other = (SString) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        
    }
    public static class SList extends SValue {
        public final List<SValue> value;
        public SList(List<SValue> value) {
            this.value = value;
        }
        public SList(){
            this.value = new ArrayList<SValue>();
        }
        public SList(SValue... values){
            this();
            for(SValue value : values){
                this.value.add(value);
            }
        }

        public SValue get(int index) {
            return value.get(index);
        }

        public void add(SValue value) {
            this.value.add(value);
        }

        public int size() {
            return value.size();
        }

        public boolean isEmpty() {
            return value.isEmpty();
        }

        public void remove(int index) {
            value.remove(index);
        }

        public void clear() {
            value.clear();
        }

        public void addAll(SList list) {
            value.addAll(list.value);
        }

        @Override
        public String toString() {
            return "SList(" + value.toString() + ")";
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            SList other = (SList) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }
    public static class SBool extends SValue {
        public final boolean value;
        public SBool(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "SBool(" + value + ")";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (value ? 1231 : 1237);
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
            SBool other = (SBool) obj;
            if (value != other.value)
                return false;
            return true;
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

        public int size() {
            return value.size();
        }

        public boolean isEmpty() {
            return value.isEmpty();
        }

        @Override
        public String toString() {
            return "SObject(" + value.toString() + ")";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            SObject other = (SObject) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
        
    }
    public static class SNull extends SValue {
        @Override
        public String toString() {
            return "SNull()";
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SNull;
        }
    }
}
