package ca.awoo.jabert;

/**
 * A serializer for serializable objects.
 */
public class SerializableSerializer implements Serializer<Serializable>{

    public SValue serialize(Serializable t) throws SerializationException{
        return t.serialize();
    }

    public Serializable deserialize(SValue sv, Class<? extends Serializable> clazz) throws SerializationException {
        try {
            Serializable s = clazz.newInstance();
            s.deserialize(sv);
            return s;
        } catch (InstantiationException e) {
            throw new SerializationException("Could not instantiate class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new SerializationException("Could not access class " + clazz.getName(), e);
        }
    }
    
}
