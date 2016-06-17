package com.lamfire.json.deserializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.lamfire.json.parser.JavaObjectJSONParser;
import com.lamfire.json.parser.JSONLexer;
import com.lamfire.json.parser.JSONToken;
import com.lamfire.json.util.TypeConverters;

public class IntegerDeserializer implements ObjectDeserializer {
    public final static IntegerDeserializer instance = new IntegerDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(JavaObjectJSONParser parser, Type clazz) {
        return (T) deserialze(parser);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(JavaObjectJSONParser parser) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_INT) {
            int val = lexer.intValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) Integer.valueOf(val);
        }
        
        if (lexer.token() == JSONToken.LITERAL_FLOAT) {
            BigDecimal decimalValue = lexer.decimalValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) Integer.valueOf(decimalValue.intValue());
        }
        
        Object value = parser.parse();

        if (value == null) {
            return null;
        }
        
        return (T) TypeConverters.castToInt(value);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
