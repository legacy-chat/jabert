package ca.awoo.jabert;

import ca.awoo.fwoabl.function.Predicate;

public final class Serializers {
    private Serializers(){}

    public static Serializer defaultSerializer(){
        CompoundSerializer cs = new CompoundSerializer();
        PrimativeSerializer ps = new PrimativeSerializer();
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return  t.equals(Long.class) ||
                        t.equals(Integer.class) ||
                        t.equals(Short.class) ||
                        t.equals(Byte.class) ||
                        t.equals(Double.class) ||
                        t.equals(Float.class) ||
                        t.equals(Boolean.class) ||
                        t.equals(Character.class) ||
                        t.equals(String.class);
            }
        }, ps);
        ArraySerializer as = new ArraySerializer(cs);
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return t.isArray();
            }
        }, as);
        SerializableSerializer ss = new SerializableSerializer();
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return Serializable.class.isAssignableFrom(t);
            }
        }, ss);
        SValueSerializer svs = new SValueSerializer();
        cs.addOption(new Predicate<Class<?>>() {
            public boolean invoke(Class<?> t) {
                return SValue.class.isAssignableFrom(t);
            }
        }, svs);
        ReflectionSerializer rs = new ReflectionSerializer(cs);
        cs.setDefaultSerializer(rs);
        return cs;
    }
}
