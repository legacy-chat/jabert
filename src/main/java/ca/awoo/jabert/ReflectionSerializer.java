package ca.awoo.jabert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.awoo.fwoabl.Optional;
import ca.awoo.fwoabl.OptionalNoneException;
import ca.awoo.jabert.SValue.*;

/**
 * A serializer that uses reflection to serialize and deserialize objects.
 * <p>
 * This serializer gets the fields of an object using reflection and serializes them into an SValue. It then deserializes the fields from an SValue using reflection.
 * </p>
 */
public class ReflectionSerializer implements Serializer {
    private final Serializer baseSerializer;

    /**
     * Creates a new ReflectionSerializer with the given base serializer.
     * @param baseSerializer The serializer to use to serialize the fields of an object. The base serializer will most likely be a compound serializer with this serializer as a part of it.
     */
    public ReflectionSerializer(Serializer baseSerializer) {
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
                    }else{
                        //The field is optional and None, we only put it in if we were told to by an annotation
                        if(f.getAnnotation(SerializeAsNull.class) != null){
                            so.put(f.getName(), new SNull());
                        }
                    }
                } else if (Map.class.isAssignableFrom(f.getType())) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    ParameterizedType type = (ParameterizedType)f.getGenericType();
                    Class<?> keyType = (Class<?>)type.getActualTypeArguments()[0];
                    if(keyType != String.class){
                        throw new SerializationException("Can only serialize Maps with String keys");
                    }
                    SObject mapObject = new SObject();
                    for(Map.Entry<?, ?> entry : map.entrySet()){
                        mapObject.put(entry.getKey().toString(), baseSerializer.serialize(entry.getValue()));
                    }
                    so.put(f.getName(), mapObject);
                } else if (Collection.class.isAssignableFrom(f.getType())) {
                    Collection<?> collection = (Collection<?>) value;
                    SList list = new SList();
                    for(Object o : collection){
                        list.add(baseSerializer.serialize(o));
                    }
                    so.put(f.getName(), list);
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

    @SuppressWarnings("unchecked")
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
                            SValue value = so.get(f.getName());
                            if(value instanceof SNull){
                                //Use optionals to handle nulls in apis
                                f.set(t, new Optional.None<Object>());
                            }else{
                                //If the field is an optional and the value is present, deserialize the value
                                ParameterizedType type = (ParameterizedType)f.getGenericType();
                                Class<?> optionalType = (Class<?>)type.getActualTypeArguments()[0];
                                f.set(t, new Optional.Some<Object>(baseSerializer.deserialize(so.get(f.getName()), optionalType)));
                            }
                        }else{
                            //If the field is an optional and the value is not present, set the field to None
                            f.set(t, new Optional.None<Object>());
                        }
                    } else if(Map.class.isAssignableFrom(f.getType())) {
                        SObject mapObject = (SObject) so.get(f.getName());
                        ParameterizedType type = (ParameterizedType)f.getGenericType();
                        Class<?> keyType = (Class<?>)type.getActualTypeArguments()[0];
                        if(keyType != String.class){
                            throw new SerializationException("Can only serialize Maps with String keys");
                        }
                        Class<?> valueType = (Class<?>)type.getActualTypeArguments()[1];
                        Map<String, Object> map;
                        if(f.getType().equals(Map.class)){
                            map = new HashMap<String,Object>();
                        }else{
                            map = (Map<String, Object>) f.getType().newInstance();
                        }
                        for(Map.Entry<String, SValue> entry : mapObject.entrySet()){
                            map.put(entry.getKey(), baseSerializer.deserialize(entry.getValue(), valueType));
                        }
                        f.set(t, map);
                    } else if (Collection.class.isAssignableFrom(f.getType())) {
                        SList list = (SList) so.get(f.getName());
                        ParameterizedType type = (ParameterizedType)f.getGenericType();
                        Class<?> valueType = (Class<?>)type.getActualTypeArguments()[0];
                        Collection<Object> collection;
                        if(f.getType().equals(Collection.class) || f.getType().equals(List.class)){
                            collection = new java.util.ArrayList<Object>();
                        }else if(f.getType().equals(Set.class)){
                            collection = new java.util.HashSet<Object>();
                        }else{
                            collection = (Collection<Object>) f.getType().newInstance();
                        }
                        for(SValue value : list){
                            collection.add(baseSerializer.deserialize(value, valueType));
                        }
                        f.set(t, collection);
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
