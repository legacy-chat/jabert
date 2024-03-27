package ca.awoo.jabert;

public interface Serializer<T> {
    public SValue serialize(T t) throws SerializationException;
    public T deserialize(SValue sv, Class<? extends T> clazz) throws SerializationException;
}
