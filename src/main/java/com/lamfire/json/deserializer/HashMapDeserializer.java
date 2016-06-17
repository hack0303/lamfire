package com.lamfire.json.deserializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.lamfire.json.parser.JavaObjectJSONParser;
import com.lamfire.json.parser.JSONLexer;
import com.lamfire.json.parser.JSONToken;

public class HashMapDeserializer implements ObjectDeserializer {

    public final static HashMapDeserializer instance = new HashMapDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(JavaObjectJSONParser parser, Type type) {
        final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken(JSONToken.COMMA);
            return null;
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        parser.parseObject(map);
        return (T) map;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
