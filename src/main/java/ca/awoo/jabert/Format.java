package ca.awoo.jabert;

import java.io.InputStream;
import java.io.OutputStream;

public interface Format {
    public void emit(SValue sv, OutputStream os) throws FormatException;
    public SValue parse(InputStream is) throws FormatException;
}
