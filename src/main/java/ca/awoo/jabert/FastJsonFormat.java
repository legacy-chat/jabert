package ca.awoo.jabert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import ca.awoo.jabert.SValue.*;

public class FastJsonFormat implements Format{
    private final String encoding;

    public FastJsonFormat(String encoding){
        this.encoding = encoding;
    }

    public void emit(SValue sv, OutputStream os) throws FormatException {
        try {
            emit(sv, new PrintStream(os, true, encoding));
        } catch (UnsupportedEncodingException e) {
            throw new FormatException("Unsupported encoding: " + encoding, e);
        }
    }

    private void emit(SValue sv, PrintStream os){
        if(sv instanceof SString){
            emitString((SString)sv, os);
        }else if(sv instanceof SNumber){
            emitNumber((SNumber)sv, os);
        }else if(sv instanceof SBool){
            emitBool((SBool)sv, os);
        }else if(sv instanceof SNull){
            emitNull((SNull)sv, os);
        }else if(sv instanceof SList){
            emitList((SList)sv, os);
        }else if(sv instanceof SObject){
            emitObject((SObject)sv, os);
        }else{
            throw new UnsupportedOperationException("Unimplemented method 'emit' for " + sv.getClass().getName());
        }
    }

    private void emitString(SString s, PrintStream os) {
        os.print("\"");
        os.print(escape(s.value));
        os.print("\"");
    }

    private void emitNumber(SNumber n, PrintStream os) {
        os.print(n.value);
    }

    private void emitBool(SBool b, PrintStream os) {
        os.print(b.value);
    }

    private void emitNull(SNull n, PrintStream os) {
        os.print("null");
    }

    private void emitList(SList l, PrintStream os) {
        os.print("[");
        boolean first = true;
        for(SValue sv : l.value){
            if(first){
                first = false;
            }else{
                os.print(",");
            }
            emit(sv, os);
        }
        os.print("]");
    }

    private void emitObject(SObject o, PrintStream os) {
        os.print("{");
        boolean first = true;
        for(String key : o.value.keySet()){
            if(first){
                first = false;
            }else{
                os.print(",");
            }
            emitString(new SString(key), os);
            os.print(":");
            emit(o.value.get(key), os);
        }
        os.print("}");
    }

    private int offset = 0;

    public SValue parse(InputStream is) throws FormatException {
        try {
            offset = 0;
            return parse(new BufferedReader(new InputStreamReader(is, encoding)));
        } catch (UnsupportedEncodingException e) {
            throw new FormatException("Unsupported encoding: " + encoding, e);
        } catch (IOException e) {
            throw new FormatException("IOException", e);
        }
    }

    private int readNotSpace(BufferedReader r) throws IOException {
        r.mark(1);
        int next = r.read();
        offset++;
        while(Character.isWhitespace(next)){
            r.mark(1);
            next = r.read();
            offset++;
        }
        return next;
    }

    private SValue parse(BufferedReader r) throws IOException, FormatException {
        int next = readNotSpace(r);
        SValue value;
        switch(next){
            case -1:
                throw new FormatException("Unexpected EOF");
            case '\"':
                value = parseString(r);
                break;
            case 't':
                value = parseTrue(r);
                break;
            case 'f':
                value = parseFalse(r);
                break;
            case 'n':
                value = parseNull(r);
                break;
            case '[':
                value = parseList(r);
                break;
            case '{':
                value = parseObject(r);
                break;
            default:
                if(Character.isDigit(next) || next == '-'){
                    StringBuilder sb = new StringBuilder();
                    sb.append((char)next);
                    r.mark(1);
                    next = r.read();
                    offset++;
                    while(Character.isDigit(next) || next == '.' || next == 'e' || next == 'E' || next == '+' || next == '-'){
                        sb.append((char)next);
                        r.mark(1);
                        next = r.read();
                        offset++;
                    }
                    r.reset();
                    offset--;
                    value = new SNumber(Double.parseDouble(sb.toString()));
                }else{
                    throw new FormatException("Unexpected character: " + (char)next + " at " + offset);
                }
        }
        return value;
    }

    private SString parseString(BufferedReader r) throws IOException, FormatException {
        int next = r.read();
        offset++;
        StringBuilder sb = new StringBuilder();
        while(next != '\"'){
            if(next == -1){
                throw new FormatException("Unexpected EOF at " + offset);
            }
            if(next == '\\'){
                parseEscape(r, sb);
            }else{
                sb.append((char)next);
            }
            next = r.read();
            offset++;
        }
        return new SString(sb.toString());
    }

    private void parseEscape(BufferedReader r, StringBuilder sb) throws IOException, FormatException {
        int next = r.read();
        offset++;
        switch(next){
            case -1:
                throw new FormatException("Unexpected EOF at " + offset);
            case '\"':
                sb.append('\"');
                break;
            case '\\':
                sb.append('\\');
                break;
            case '/':
                sb.append('/');
                break;
            case 'b':
                sb.append('\b');
                break;
            case 'f':
                sb.append('\f');
                break;
            case 'n':
                sb.append('\n');
                break;
            case 'r':
                sb.append('\r');
                break;
            case 't':
                sb.append('\t');
                break;
            case 'u':
                StringBuilder hex = new StringBuilder();
                for(int i = 0; i < 4; i++){
                    next = r.read();
                    offset++;
                    if(next == -1){
                        throw new FormatException("Unexpected EOF at " + offset);
                    }
                    hex.append((char)next);
                }
                sb.append((char)Integer.parseInt(hex.toString(), 16));
                break;
            default:
                throw new FormatException("Invalid escape sequence: \\" + (char)next + " at " + offset);
        }
    }

    private SBool parseTrue(BufferedReader r) throws IOException, FormatException {
        if(r.read() != 'r' || r.read() != 'u' || r.read() != 'e'){
            throw new FormatException("Invalid value: true at " + offset);
        }
        offset += 3;
        return new SBool(true);
    }

    private SBool parseFalse(BufferedReader r) throws IOException, FormatException {
        if(r.read() != 'a' || r.read() != 'l' || r.read() != 's' || r.read() != 'e'){
            throw new FormatException("Invalid value: false at " + offset);
        }
        offset += 4;
        return new SBool(false);
    }

    private SNull parseNull(BufferedReader r) throws IOException, FormatException {
        if(r.read() != 'u' || r.read() != 'l' || r.read() != 'l'){
            throw new FormatException("Invalid value: null at " + offset);
        }
        offset += 3;
        return new SNull();
    }

    private SList parseList(BufferedReader r) throws IOException, FormatException {
        SList l = new SList();
        int next = readNotSpace(r);
        if(next == ']'){
            return l;
        }
        r.reset();
        offset--;
        while(true){
            l.value.add(parse(r));
            next = readNotSpace(r);
            if(next == ']'){
                return l;
            }else if(next != ','){
                throw new FormatException("Expected ',' or ']', got " + (char)next + " at " + offset);
            }
        }
    }

    private SObject parseObject(BufferedReader r) throws IOException, FormatException {
        SObject o = new SObject();
        r.mark(1);
        int next = readNotSpace(r);
        if(next == '}'){
            return o;
        }
        r.reset();
        offset--;
        while(true){
            next = readNotSpace(r);
            if(next != '\"'){
                throw new FormatException("Expected '\"', got " + (char)next + " at " + offset);
            }
            SString key = parseString(r);
            next = readNotSpace(r);
            if(next != ':'){
                throw new FormatException("Expected ':', got " + (char)next + " after \"" + escape(key.value) + "\" at " + offset);
            }
            SValue value = parse(r);
            o.value.put(key.value, value);
            next = readNotSpace(r);
            if(next == '}'){
                return o;
            }else if(next != ','){
                throw new FormatException("Expected ',' or '}', got " + (char)next + " at " + offset);
            }
        }
    }


    
    private String escape(String s){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if(c == '\\'){
                sb.append("\\\\");
            }else if(c == '\"'){
                sb.append("\\\"");
            }else if(c == '\n'){
                sb.append("\\n");
            }else if(c == '\r'){
                sb.append("\\r");
            }else if(c == '\t'){
                sb.append("\\t");
            }else if(c == '\b'){
                sb.append("\\b");
            }else if(c == '\f'){
                sb.append("\\f");
            }else if(c < 32 || c >= 127){
                sb.append(String.format("\\u%04x", (int)c));
            }else{
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
