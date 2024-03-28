package ca.awoo.jabert;

import ca.awoo.jabert.SValue.*;

public class PrimativeSerializer implements Serializer {

    public SValue serialize(Object t) throws SerializationException {
        if(t instanceof Long) {
            return new SNumber((Long)t);
        } else if(t instanceof Integer){
            return new SNumber((Integer)t);
        } else if(t instanceof Short){
            return new SNumber((Short)t);
        } else if(t instanceof Byte){
            return new SNumber((Byte)t);
        } else if(t instanceof Double){
            return new SNumber((Double)t);
        } else if(t instanceof Float){
            return new SNumber((Float)t);
        } else if(t instanceof Boolean){
            return new SBool((Boolean)t);
        } else if(t instanceof Character){
            return new SString(t.toString());
        } else if(t instanceof String){
            return new SString((String)t);
        } else {
            throw new SerializationException("Cannot serialize object of type " + t.getClass().getName());
        }
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        if(sv instanceof SNumber){
            SNumber sn = (SNumber) sv;
            if(clazz.equals(Long.class)){
                return sn.longValue();
            } else if(clazz.equals(Integer.class)){
                return sn.intValue();
            } else if(clazz.equals(Short.class)){
                return sn.shortValue();
            } else if(clazz.equals(Byte.class)){
                return sn.byteValue();
            } else if(clazz.equals(Double.class)){
                return sn.doubleValue();
            } else if(clazz.equals(Float.class)){
                return sn.floatValue();
            } else {
                throw new SerializationException("Cannot deserialize number into type " + clazz.getName());
            }
        } else if(sv instanceof SBool){
            if(clazz.equals(Boolean.class)){
                return ((SBool)sv).value;
            } else {
                throw new SerializationException("Cannot deserialize boolean into type " + clazz.getName());
            }
        } else if(sv instanceof SString){
            if(clazz.equals(Character.class)){
                return ((SString)sv).value.charAt(0);
            } else if(clazz.equals(String.class)){
                return ((SString)sv).value;
            } else {
                throw new SerializationException("Cannot deserialize string into type " + clazz.getName());
            }
        } else {
            throw new SerializationException("PrimativeSerializer cannot deserialize object of type " + sv.getClass().getName() + " into type " + clazz.getName());
        }
    }
    
}
