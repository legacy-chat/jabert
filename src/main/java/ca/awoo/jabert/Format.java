package ca.awoo.jabert;

import java.io.InputStream;
import java.io.OutputStream;

public interface Format<Token> {
    public void emit(SValue sv, OutputStream os);
    public SValue parse(InputStream is);
}
