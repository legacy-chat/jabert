package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import ca.awoo.jabert.SValue.SList;

public class SpeedTest {
    @Test
    public void jsonSpeedTest() throws Exception {
        SList list = new SList(FormatTest.everyValue(5));
        Format format = new JsonFormat("UTF-8");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        format.emit(list, out);
        byte[] bytes = out.toByteArray();
        System.out.println("Size: " + bytes.length);
        SValue sv2 = format.parse(new ByteArrayInputStream(bytes));
        assertEquals("test" + list.toString() + " on " + format.toString(), list, sv2);
    }

    @Test
    public void fastJsonSpeedTest() throws Exception {
        SList list = new SList(FormatTest.everyValue(5));
        Format format = new FastJsonFormat("UTF-8");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        format.emit(list, out);
        byte[] bytes = out.toByteArray();
        System.out.println("Size: " + bytes.length);
        SValue sv2 = format.parse(new ByteArrayInputStream(bytes));
        assertEquals("test" + list.toString() + " on " + format.toString(), list, sv2);
    }
}
