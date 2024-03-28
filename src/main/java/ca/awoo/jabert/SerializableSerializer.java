package ca.awoo.jabert;

/**
 * A serializer for serializable objects.
 */
public class SerializableSerializer implements Serializer{

    public SValue serialize(Object t) throws SerializationException{
        if(!(t instanceof Serializable)){
            throw new SerializationException("Object " + t.getClass().getName() + " does not implement Serializable");
        }
        return ((Serializable)t).serialize();
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        try {
            Object s = clazz.newInstance();
            if(!(s instanceof Serializable)){
                throw new SerializationException("Class " + clazz.getName() + " does not implement Serializable");
            }
            ((Serializable)s).deserialize(sv);
            return s;
        } catch (InstantiationException e) {
            throw new SerializationException("Could not instantiate class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new SerializationException("Could not access class " + clazz.getName(), e);
        }
    }
    
}
