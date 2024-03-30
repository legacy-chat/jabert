package ca.awoo.jabert;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.awoo.jabert.SValue.*;

public class FormatTest {

    @Test
    public void testJson() throws Exception {
        testAll(new JsonFormat("UTF-8"));
    }

    @Test
    public void testFastJson() throws Exception {
        testAll(new FastJsonFormat("UTF-8"));
    }

    @Test
    public void testSlowToFast() throws Exception {
        testAll(new JsonFormat("UTF-8"), new FastJsonFormat("UTF-8"));
    }

    public void testValue(Format emitter, Format parser, SValue sv) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        emitter.emit(sv, out);
        byte[] bytes = out.toByteArray();
        SValue sv2;
        try{
            sv2 = parser.parse(new ByteArrayInputStream(bytes));
        }catch(Exception e){
            System.out.println("Failed to parse " + new String(bytes) + " on " + parser.toString());
            throw e;
        }
        assertEquals("test" + new String(bytes) + " on " + parser.toString(), sv, sv2);
    }

    public void testValue(Format format, SValue sv) throws Exception{
        testValue(format, format, sv);
    }

    public void testAll(Format emitter, Format parser) throws Exception{
        List<SValue> values = everyValue(3);
        for(SValue value : values){
            testValue(emitter, parser, value);
        }
    }

    public void testAll(Format format) throws Exception{
        testAll(format, format);
    }

    public static List<SValue> everyValue(int maxDepth){
        List<SValue> everyValue = new ArrayList<SValue>();
        everyValue.add(new SNumber(0));
        everyValue.add(new SNumber(1));
        everyValue.add(new SNumber(-1));
        everyValue.add(new SNumber(0.0));
        everyValue.add(new SNumber(1.0));
        everyValue.add(new SNumber(-1.0));
        everyValue.add(new SNumber(0.1));
        everyValue.add(new SNumber(1.1));
        everyValue.add(new SNumber(-1.1));
        everyValue.add(new SString(""));
        everyValue.add(new SString(" "));
        everyValue.add(new SString("a"));
        everyValue.add(new SString("a "));
        everyValue.add(new SString(" a"));
        everyValue.add(new SString(" a "));
        everyValue.add(new SString("a\""));
        everyValue.add(new SString("a\\"));
        everyValue.add(new SString("a/"));
        everyValue.add(new SString("a\b"));
        everyValue.add(new SString("a\f"));
        everyValue.add(new SString("a\n"));
        everyValue.add(new SString("a\r"));
        everyValue.add(new SString("a\t"));
        everyValue.add(new SString("a\u0000"));
        everyValue.add(new SString("a\u0001"));
        everyValue.add(new SString("a\u0002"));
        everyValue.add(new SString("a\u0003"));
        everyValue.add(new SBool(true));
        everyValue.add(new SBool(false));
        everyValue.add(new SNull());
        everyValue.add(new SObject());
        everyValue.add(new SList());
        if(maxDepth > 0){
            SList list = new SList();
            for(SValue value : everyValue(maxDepth - 1)){
                list.add(value);
            }
            everyValue.add(list);
            SObject object = new SObject();
            for(SValue value : everyValue(maxDepth - 1)){
                object.put(value.toString(), value);
            }
            everyValue.add(object);
        }
        return everyValue;
    }
}
