package ca.awoo.jabert;

import java.util.HashSet;
import java.util.Set;

import ca.awoo.fwoabl.function.Predicate;

public class CompoundSerializer implements Serializer{
    private static class SerializationOption{
        private final Predicate<Class<?>> predicate;
        private final Serializer serializer;

        public SerializationOption(Predicate<Class<?>> predicate, Serializer serializer) {
            this.predicate = predicate;
            this.serializer = serializer;
        }

        public boolean matches(Class<?> clazz){
            return predicate.invoke(clazz);
        }

        public Serializer getSerializer(){
            return serializer;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
            result = prime * result + ((serializer == null) ? 0 : serializer.hashCode());
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
            SerializationOption other = (SerializationOption) obj;
            if (predicate == null) {
                if (other.predicate != null)
                    return false;
            } else if (!predicate.equals(other.predicate))
                return false;
            if (serializer == null) {
                if (other.serializer != null)
                    return false;
            } else if (!serializer.equals(other.serializer))
                return false;
            return true;
        }
    }

    private final Set<SerializationOption> options = new HashSet<SerializationOption>();
    private Serializer defaultSerializer;

    public void addOption(Predicate<Class<?>> predicate, Serializer serializer){
        options.add(new SerializationOption(predicate, serializer));
    }

    public void setDefaultSerializer(Serializer serializer){
        defaultSerializer = serializer;
    }

    /**
     * Converts a primative class to its object equivalent. Mike Tyson is a boxer.
     * @param primative The primative class to convert
     * @return The object equivalent of the primative class
     */
    private Class<?> mikeTyson(Class<?> primative){
        if(primative == int.class){
            return Integer.class;
        }else if(primative == long.class){
            return Long.class;
        }else if(primative == float.class){
            return Float.class;
        }else if(primative == double.class){
            return Double.class;
        }else if(primative == boolean.class){
            return Boolean.class;
        }else if(primative == char.class){
            return Character.class;
        }else if(primative == byte.class){
            return Byte.class;
        }else if(primative == short.class){
            return Short.class;
        }else{
            return primative;
        }
    }

    private Serializer getSerializer(Class<?> clazz) throws SerializationException{
        for(SerializationOption option : options){
            if(option.matches(clazz)){
                return option.getSerializer();
            }
        }
        if(defaultSerializer != null){
            return defaultSerializer;
        }
        throw new SerializationException("No serializer found for class " + clazz.getName());
    }

    public SValue serialize(Object t) throws SerializationException {
        Class<?> clazz = mikeTyson(t.getClass());
        return getSerializer(clazz).serialize(t);
    }

    public Object deserialize(SValue sv, Class<? extends Object> clazz) throws SerializationException {
        clazz = mikeTyson(clazz);
        return getSerializer(clazz).deserialize(sv, clazz);
    }
    
}
