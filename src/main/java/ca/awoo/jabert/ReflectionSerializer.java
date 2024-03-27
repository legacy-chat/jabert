package ca.awoo.jabert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;

import ca.awoo.fwoabl.Optional;
import ca.awoo.fwoabl.OptionalNoneException;
import ca.awoo.jabert.SValue.*;

/**
 * A serializer that uses reflection to serialize and deserialize objects.
 * <p>
 * This serializer gets the fields of an object using reflection and serializes them into an SValue. It then deserializes the fields from an SValue using reflection.
 * </p>
 */
public class ReflectionSerializer implements Serializer<Object> {
    private final Serializer<Object> baseSerializer;

    /**
     * Creates a new ReflectionSerializer with the given base serializer.
     * @param baseSerializer The serializer to use to serialize the fields of an object. The base serializer will most likely be a compound serializer with this serializer as a part of it.
     */
    public ReflectionSerializer(Serializer<Object> baseSerializer) {
        this.baseSerializer = baseSerializer;
    }

    /**
     * Serializes an object into an SValue using reflection.
     * <p>
     * This method gets the fields of the object using reflection and serializes them into an SValue. It will not serialize synthetic or transient fields.
     * </p>
     * <p>
     * If a field is an Optional, the serializer will serialize the value of the Optional if it is present. If the Optional is empty, the field will not be included in the output.
     * </p>
     * @param t The object to serialize.
     * @return The serialized object.
     */
    public SValue serialize(Object t) throws SerializationException {
        if(t == null){
            return new SNull();
        }
        SObject so = new SObject();
        Field[] fields = t.getClass().getDeclaredFields();
        for(Field f : fields){
            if(f.isSynthetic() || Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            f.setAccessible(true);
            try {
                Object value = f.get(t);
                if(value.equals(t)){
                    throw new SerializationException("Cannot serialize object that contains itself: " + t);
                }
                //Check if the field is optional
                if(Optional.class.isAssignableFrom(f.getType())){
                    Optional<?> opt = (Optional<?>) value;
                    if(opt.isSome()){
                        so.put(f.getName(), baseSerializer.serialize(opt.get()));
                    }
                    //If the field is optional and not present, don't put it in the final value
                } else {
                    so.put(f.getName(), baseSerializer.serialize(value));
                }
            } catch (IllegalArgumentException e) {
                throw new SerializationException("Could not serialize field " + f.getName() + " of object " + t.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                throw new SerializationException("Could not access field " + f.getName() + " of object " + t.getClass().getName(), e);
            } catch (OptionalNoneException e){
                throw new SerializationException("Unreachable");
            } catch (SerializationException e){
                throw new SerializationException("Could not serialize field " + f.getName() + " of object " + t.getClass().getName(), e);
            }
        }
        return so;
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        if(sv instanceof SNull){
            return null;
        }
        if(!(sv instanceof SObject)){
            throw new SerializationException("Cannot deserialize non-object value into object");
        }
        SObject so = (SObject) sv;
        try {
            Object t = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for(Field f : fields){
                if(f.isSynthetic() || Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                try{
                    f.setAccessible(true);
                    if(Optional.class.isAssignableFrom(f.getType())){
                        if(so.has(f.getName())){
                            //If the field is an optional and the value is present, deserialize the value
                            ParameterizedType type = (ParameterizedType)f.getGenericType();
                            Class<?> optionalType = (Class<?>)type.getActualTypeArguments()[0];
                            f.set(t, new Optional.Some<Object>(baseSerializer.deserialize(so.get(f.getName()), optionalType)));
                        }else{
                            //If the field is an optional and the value is not present, set the field to None
                            f.set(t, new Optional.None<Object>());
                        }
                    } else {
                        if(so.has(f.getName())){
                            f.set(t, baseSerializer.deserialize(so.get(f.getName()), f.getType()));
                        } else {
                            throw new SerializationException("Missing field " + f.getName() + " in object " + clazz.getName());
                        }
                    }
                } catch (SerializationException e){
                    throw new SerializationException("Could not deserialize field " + f.getName() + " of object " + clazz.getName(), e);
                }
            }
            return t;
        } catch (InstantiationException e) {
            throw new SerializationException("Could not instantiate class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new SerializationException("Could not access class " + clazz.getName(), e);
        }
    }
    
}
