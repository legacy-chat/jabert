package ca.awoo.jabert;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class FastJsonTest {
    @Test
    public void objectWithSpaces() throws Exception{
        String what = " { \"foo\" : \"bar\" } ";
        FastJsonFormat format = new FastJsonFormat("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(what.getBytes());
        SValue sv = format.parse(in);
        System.out.println(sv);
    }

    @Test
    public void listWithSpaces() throws Exception{
        String what = " [ \"foo\" , \"bar\" ] ";
        FastJsonFormat format = new FastJsonFormat("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(what.getBytes());
        SValue sv = format.parse(in);
        System.out.println(sv);
    }

    @Test
    public void emptyObjectWithSpaces() throws Exception{
        String what = " { } ";
        FastJsonFormat format = new FastJsonFormat("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(what.getBytes());
        SValue sv = format.parse(in);
        System.out.println(sv);
    }

    @Test
    public void emptyListWithSpaces() throws Exception{
        String what = " [ ] ";
        FastJsonFormat format = new FastJsonFormat("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(what.getBytes());
        SValue sv = format.parse(in);
        System.out.println(sv);
    }
}
