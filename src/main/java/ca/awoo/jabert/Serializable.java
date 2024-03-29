package ca.awoo.jabert;

/**
 * Interface for serializable objects.
 * <p>
 * Implementing classes should provide a method to serialize themselves into an SValue and a method to deserialize themselves from an SValue.
 * Implementing classes also require a constructor that takes no arguments for the SerializableSerializer to work. This constructor must be public.
 * </p>
 * @see SerializableSerializer
 */
public interface Serializable {
    public SValue serialize() throws SerializationException;
    public void deserialize(SValue sv) throws SerializationException;
}
