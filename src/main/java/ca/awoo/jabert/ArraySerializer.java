package ca.awoo.jabert;

import java.lang.reflect.Array;
import ca.awoo.jabert.SValue.SList;

public class ArraySerializer implements Serializer<Object> {
    private final Serializer<Object> baseSerializer;

    public ArraySerializer(Serializer<Object> baseSerializer) {
        this.baseSerializer = baseSerializer;
    }

    public SValue serialize(Object t) throws SerializationException {
        if(!t.getClass().isArray()){
            throw new SerializationException("Attempting to serialize non-array type with ArraySerializer");
        }
        SList list = new SList();
        for(int i = 0; i < Array.getLength(t); i++){
            list.value.add(baseSerializer.serialize(Array.get(t, i)));
        }
        return list;
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        if(!clazz.isArray()){
            throw new SerializationException("Attempting to deserialize to non-array type with ArraySerializer");
        }
        if(!(sv instanceof SList)){
            throw new SerializationException("Attempting to deserialize non-list type with ArraySerializer");
        }
        SList list = (SList) sv;
        Object array = Array.newInstance(clazz.getComponentType(), list.value.size());
        for(int i = 0; i < list.value.size(); i++){
            Array.set(array, i, baseSerializer.deserialize(list.value.get(i), clazz.getComponentType()));
        }
        return array;
    }
    
}
