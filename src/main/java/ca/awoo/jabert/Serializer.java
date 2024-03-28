package ca.awoo.jabert;

public interface Serializer {
    public SValue serialize(Object t) throws SerializationException;
    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException;
}
