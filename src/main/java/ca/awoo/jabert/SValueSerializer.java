package ca.awoo.jabert;

/**
 * A sort of identity serializer that just returns the direct SValue given.
 * <p>
 * Sometimes you need data from a field on an object to know what to do with another field.
 * Using this serializer, you can get the SValue and handle it later.
 * </p>
 */
public class SValueSerializer implements Serializer {

    public SValue serialize(Object t) throws SerializationException {
        if(t instanceof SValue){
            return (SValue)t;
        }
        throw new SerializationException("Cannot serialize object of type " + t.getClass().getName() + " with SValueSerializer");
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        return sv;
    }
    
}
