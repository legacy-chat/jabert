package ca.awoo.jabert;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

import ca.awoo.fwoabl.Optional;
import ca.awoo.fwoabl.OptionalNoneException;
import ca.awoo.fwoabl.function.Functions;
import ca.awoo.fwoabl.function.Predicate;
import ca.awoo.jabert.SValue.*;
import ca.awoo.praser.Context;
import ca.awoo.praser.ParseException;
import ca.awoo.praser.Parser;
import ca.awoo.praser.StreamException;
import ca.awoo.praser.FilterContext;
import ca.awoo.praser.ParsedContext;

import static ca.awoo.praser.Text.*;
import static ca.awoo.praser.Combinators.*;

public class JsonFormat implements Format {
    private final String encoding;
    
    @SuppressWarnings("unchecked")
    public JsonFormat(String encoding) {
        this.encoding = encoding;
        jsonParser = or(jsonStringParser, jsonNumberParser, jsonBoolParser, jsonNullParser, jsonListParser, jsonObjectParser);
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
    
    public void emit(SValue sv, OutputStream os) throws FormatException {
        try{
            PrintStream ps = new PrintStream(os, true, encoding);
            if(sv instanceof SNumber){
                SNumber sn = (SNumber)sv;
                ps.print(sn.value);
            }else if(sv instanceof SString){
                SString ss = (SString)sv;
                ps.print("\"");
                ps.print(escape(ss.value));
                ps.print("\"");
            }else if(sv instanceof SList){
                SList sl = (SList)sv;
                ps.print("[");
                for(int i = 0; i < sl.value.size(); i++){
                    if(i > 0){
                        ps.print(",");
                    }
                    emit(sl.value.get(i), os);
                }
                ps.print("]");
            }else if(sv instanceof SObject){
                SObject so = (SObject)sv;
                ps.print("{");
                boolean first = true;
                for(String key : so.value.keySet()){
                    if(!first){
                        ps.print(",");
                    }
                    first = false;
                    ps.print("\"");
                    ps.print(escape(key));
                    ps.print("\":");
                    emit(so.value.get(key), os);
                }
                ps.print("}");
            }else if(sv instanceof SBool){
                SBool sb = (SBool)sv;
                ps.print(sb.value ? "true" : "false");
            }else if(sv instanceof SNull){
                ps.print("null");
            }else{
                throw new FormatException("Unknown SValue type: " + sv.getClass().getName());
            }
        } catch(UnsupportedEncodingException e){
            throw new FormatException("JsonFormat was created with an invalid encoding: " + encoding, e);
        }
    }
    
    private static class JsonToken{
        public final String original;
        
        public static class JsonString extends JsonToken{
            public final String value;
            public JsonString(String original, String value){
                super(original);
                this.value = value;
            }
        }
        
        public static class JsonNumber extends JsonToken{
            public final Number value;
            public JsonNumber(String original, Number value){
                super(original);
                this.value = value;
            }
        }
        
        public static class Whitespace extends JsonToken {
            public Whitespace(String original){
                super(original);
            }
        }
        
        public static class OpenBracket extends JsonToken {
            public OpenBracket(String original){
                super(original);
            }
        }
        
        public static class CloseBracket extends JsonToken {
            public CloseBracket(String original){
                super(original);
            }
        }
        
        public static class OpenBrace extends JsonToken {
            public OpenBrace(String original){
                super(original);
            }
        }
        
        public static class CloseBrace extends JsonToken {
            public CloseBrace(String original){
                super(original);
            }
        }
        
        public static class Colon extends JsonToken {
            public Colon(String original){
                super(original);
            }
        }
        
        public static class Comma extends JsonToken {
            public Comma(String original){
                super(original);
            }
        }

        public static class JsonTrue extends JsonToken {
            public JsonTrue(String original){
                super(original);
            }
        }

        public static class JsonFalse extends JsonToken {
            public JsonFalse(String original){
                super(original);
            }
        }

        public static class JsonNull extends JsonToken {
            public JsonNull(String original){
                super(original);
            }
        }
        
        public JsonToken(String original){
            this.original = original;
        }
        
    }
    
    private final Parser<Character, JsonToken.JsonString> stringParser = new Parser<Character, JsonToken.JsonString>() {
        @SuppressWarnings("unchecked")
        public JsonToken.JsonString parse(Context<Character> context) throws ParseException {
            try{
                tag("\"").parse(context);
                StringBuilder original = new StringBuilder();
                original.append("\"");
                StringBuilder value = new StringBuilder();
                Parser<Character, Character> normalChar = not(or(tag("\\"), tag("\"")));
                Parser<Character, List<String>> escapeChar = seq(tag("\\"), or(tag("\""), tag("\\"), tag("/"), tag("b"), tag("f"), tag("n"), tag("r"), tag("t"), stringAppend(seq(tag("u"), stringFold(repN(4, hexDigit()))))));
                
                while(true){
                    Context<Character> clone = context.clone();
                    try{
                        char c = normalChar.parse(clone);
                        context.skip(clone.getOffset() - context.getOffset());
                        original.append(c);
                        value.append(c);
                    } catch(ParseException e){
                        try{
                            clone = context.clone();
                            List<String> escape = escapeChar.parse(clone);
                            context.skip(clone.getOffset() - context.getOffset());
                            original.append(escape.get(0));
                            original.append(escape.get(1));
                            String escapeValue = escape.get(1);
                            if(escapeValue.equals("\"")){
                                value.append("\"");
                            }else if(escapeValue.equals("\\")){
                                value.append("\\");
                            }else if(escapeValue.equals("/")){
                                value.append("/");
                            }else if(escapeValue.equals("b")){
                                value.append("\b");
                            }else if(escapeValue.equals("f")){
                                value.append("\f");
                            }else if(escapeValue.equals("n")){
                                value.append("\n");
                            }else if(escapeValue.equals("r")){
                                value.append("\r");
                            }else if(escapeValue.equals("t")){
                                value.append("\t");
                            }else{
                                value.append((char)Integer.parseInt(escapeValue.substring(1), 16));
                            }
                        } catch(ParseException e2){
                            //Check if we're at the end of the string
                            try{
                                tag("\"").parse(context);
                                original.append("\"");
                                return new JsonToken.JsonString(original.toString(), value.toString());
                            } catch(ParseException e3){
                                throw new ParseException(context, "Invalid escape sequence in string: " + original.toString(), e2);
                            }
                        }
                    }
                }
            } catch(ParseException e){
                throw new ParseException(context, "Failed to parse json string", e);
            } catch(StreamException e){
                throw new ParseException(context, "Stream exception while parsing json string", e);
            }
        }

        @Override
        public String toString(){
            return "JsonStringToken";
        }
    };
    
    private final Parser<Character, JsonToken.JsonNumber> numberParser = new Parser<Character,JsonFormat.JsonToken.JsonNumber>() {
        
        @SuppressWarnings("unchecked")
        public JsonToken.JsonNumber parse(Context<Character> context) throws ParseException {
            Optional<String> minus = optional(tag("-")).parse(context);
            String integer = or(tag("0"),stringAppend(seq(charToString(oneOf("123456789")), optionOr(optional(stringFold(many(digit()))), Functions.constant(""))))).parse(context);
            Optional<String> fraction = optional(stringAppend(seq(tag("."), stringFold(many(digit()))))).parse(context);
            Optional<String> exponent = optional(stringAppend(seq(charToString(oneOf("eE")), optionOr(optional(charToString(oneOf("+-"))), Functions.constant("")), stringFold(many(digit()))))).parse(context);
            String number = minus.or("") + integer + fraction.or("") + exponent.or("");
            try{
                BigDecimal bd = new BigDecimal(number);
                return new JsonToken.JsonNumber(number, bd);
            } catch(NumberFormatException e){
                throw new ParseException(context, "Failed to parse json number: " + number, e);
            }
        }

        @Override
        public String toString(){
            return "JsonNumberToken";
        }
        
    };

    private final Parser<Character, JsonToken.JsonTrue> trueParser = new Parser<Character,JsonFormat.JsonToken.JsonTrue>() {
        public JsonToken.JsonTrue parse(Context<Character> context) throws ParseException {
            return new JsonToken.JsonTrue(tag("true").parse(context));
        }

        @Override
        public String toString(){
            return "JsonTrue";
        }
    };

    private final Parser<Character, JsonToken.JsonFalse> falseParser = new Parser<Character,JsonFormat.JsonToken.JsonFalse>() {
        public JsonToken.JsonFalse parse(Context<Character> context) throws ParseException {
            return new JsonToken.JsonFalse(tag("false").parse(context));
        }

        @Override
        public String toString(){
            return "JsonFalse";
        }
    };
    
    private final Parser<Character, JsonToken.Whitespace> whitespaceParser = new Parser<Character, JsonToken.Whitespace>() {
        public JsonToken.Whitespace parse(Context<Character> context) throws ParseException {
            return new JsonToken.Whitespace(oneOf(" \t\n\r").parse(context).toString());
        }

        @Override
        public String toString(){
            return "Whitespace";
        }
    };
    
    private final Parser<Character, JsonToken.OpenBracket> openBracketParser = new Parser<Character, JsonToken.OpenBracket>() {
        public JsonToken.OpenBracket parse(Context<Character> context) throws ParseException {
            return new JsonToken.OpenBracket(tag("[").parse(context));
        }

        @Override
        public String toString(){
            return "OpenBracket";
        }
    };
    
    private final Parser<Character, JsonToken.CloseBracket> closeBracketParser = new Parser<Character, JsonToken.CloseBracket>() {
        public JsonToken.CloseBracket parse(Context<Character> context) throws ParseException {
            return new JsonToken.CloseBracket(tag("]").parse(context));
        }

        @Override
        public String toString(){
            return "CloseBracket";
        }
    };
    
    private final Parser<Character, JsonToken.OpenBrace> openBraceParser = new Parser<Character, JsonToken.OpenBrace>() {
        public JsonToken.OpenBrace parse(Context<Character> context) throws ParseException {
            return new JsonToken.OpenBrace(tag("{").parse(context));
        }

        @Override
        public String toString(){
            return "OpenBrace";
        }
    };
    
    private final Parser<Character, JsonToken.CloseBrace> closeBraceParser = new Parser<Character, JsonToken.CloseBrace>() {
        public JsonToken.CloseBrace parse(Context<Character> context) throws ParseException {
            return new JsonToken.CloseBrace(tag("}").parse(context));
        }

        @Override
        public String toString(){
            return "CloseBrace";
        }
    };
    
    private final Parser<Character, JsonToken.Colon> colonParser = new Parser<Character, JsonToken.Colon>() {
        public JsonToken.Colon parse(Context<Character> context) throws ParseException {
            return new JsonToken.Colon(tag(":").parse(context));
        }

        @Override
        public String toString(){
            return "Colon";
        }
    };
    
    private final Parser<Character, JsonToken.Comma> commaParser = new Parser<Character, JsonToken.Comma>() {
        public JsonToken.Comma parse(Context<Character> context) throws ParseException {
            return new JsonToken.Comma(tag(",").parse(context));
        }

        @Override
        public String toString(){
            return "Comma";
        }
    };
    
    @SuppressWarnings("unchecked")
    private final Parser<Character, JsonToken> jsonTokenParser = or(stringParser, numberParser, whitespaceParser, openBracketParser, closeBracketParser, openBraceParser, closeBraceParser, colonParser, commaParser);
    
    private final Parser<JsonToken, SValue> jsonParser;

    private final Parser<JsonToken, SString> jsonStringParser = new Parser<JsonToken, SString>() {
        public SString parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken token = context.next().get();
                if(token instanceof JsonToken.JsonString){
                    JsonToken.JsonString js = (JsonToken.JsonString)token;
                    return new SString(js.value);
                }else{
                    throw new ParseException(context, "Expected a string token, but got: " + token.getClass().getName());
                }
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json string", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonString";
        }
    };

    private final Parser<JsonToken, SNumber> jsonNumberParser = new Parser<JsonToken, SNumber>() {
        public SNumber parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken token = context.next().get();
                if(token instanceof JsonToken.JsonNumber){
                    JsonToken.JsonNumber jn = (JsonToken.JsonNumber)token;
                    return new SNumber(jn.value);
                }else{
                    throw new ParseException(context, "Expected a number token, but got: " + token.getClass().getName());
                }
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json number", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonNumber";
        }
    };

    private final Parser<JsonToken, SBool> jsonBoolParser = new Parser<JsonToken, SBool>() {
        public SBool parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken token = context.next().get();
                if(token instanceof JsonToken.JsonTrue){
                    return new SBool(true);
                }else if(token instanceof JsonToken.JsonFalse){
                    return new SBool(false);
                }else{
                    throw new ParseException(context, "Expected a boolean token, but got: " + token.getClass().getName());
                }
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json boolean", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonBool";
        }
    };

    private final Parser<JsonToken, SNull> jsonNullParser = new Parser<JsonToken, SNull>() {
        public SNull parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken token = context.next().get();
                if(token instanceof JsonToken.JsonNull){
                    return new SNull();
                }else{
                    throw new ParseException(context, "Expected a null token, but got: " + token.getClass().getName());
                }
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json null", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonNull";
        }
    };

    private final Parser<JsonToken, SList> jsonListParser = new Parser<JsonToken, SList>() {
        public SList parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken openBracket = context.next().get();
                if(!(openBracket instanceof JsonToken.OpenBracket)){
                    throw new ParseException(context, "Expected an open bracket token, but got: " + openBracket.getClass().getName());
                }
                SList values = new SList();
                while(true){
                    try{
                        SValue value = jsonParser.parse(context);
                        values.add(value);
                    } catch(ParseException e){
                        throw new ParseException(context, "Failed to parse json list value", e);
                    }
                    JsonToken comma = context.next().get();
                    if(comma instanceof JsonToken.Comma){
                        continue;
                    }else if(comma instanceof JsonToken.CloseBracket){
                        break;
                    }else{
                        throw new ParseException(context, "Expected a comma or close bracket token, but got: " + comma.getClass().getName());
                    }
                }
                return values;
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json list", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonList";
        }
    };

    private final Parser<JsonToken, SObject> jsonObjectParser = new Parser<JsonToken, SObject>() {
        public SObject parse(Context<JsonToken> context) throws ParseException {
            try {
                JsonToken openBrace = context.next().get();
                if(!(openBrace instanceof JsonToken.OpenBrace)){
                    throw new ParseException(context, "Expected an open brace token, but got: " + openBrace.getClass().getName());
                }
                SObject values = new SObject();
                while(true){
                    SString key;
                    try{
                        key = jsonStringParser.parse(context);
                    } catch(ParseException e){
                        throw new ParseException(context, "Failed to parse json object key", e);
                    }
                    JsonToken colon = context.next().get();
                    if(!(colon instanceof JsonToken.Colon)){
                        throw new ParseException(context, "Expected a colon token, but got: " + colon.getClass().getName());
                    }
                    try{ 
                        SValue value = jsonParser.parse(context);
                        values.put(key.value, value);
                    }catch(ParseException e){
                        throw new ParseException(context, "Failed to parse value for json object field: " + key.value, e);
                    }

                    JsonToken comma = context.next().get();
                    if(comma instanceof JsonToken.Comma){
                        continue;
                    }else if(comma instanceof JsonToken.CloseBrace){
                        break;
                    }else{
                        throw new ParseException(context, "Expected a comma or close brace token, but got: " + comma.getClass().getName());
                    }
                }
                return values;
            } catch (StreamException e) {
                throw new ParseException(context, "Stream exception while parsing json object", e);
            } catch (OptionalNoneException e){
                throw new ParseException(context, "Unexpected end of stream", e);
            }
        }

        @Override
        public String toString(){
            return "JsonObject";
        }
    };

    
    public SValue parse(InputStream is) throws FormatException{
        Context<Character> charContext = contextFromStream(is, Charset.forName(encoding));
        Context<JsonToken> tokenContext = new FilterContext<JsonToken>(new ParsedContext<Character, JsonToken>(charContext, jsonTokenParser), new Predicate<JsonToken>() {
            public boolean invoke(JsonToken token) {
                return !(token instanceof JsonToken.Whitespace);
            }
        });
        try{
            return jsonParser.parse(tokenContext);
        } catch(ParseException e){
            throw new FormatException("Failed to parse json", e);
        }
    }
    
}