package com.lamfire.json.deserializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import com.lamfire.json.parser.JavaObjectJSONParser;
import com.lamfire.json.parser.JSONToken;

public class TreeMapDeserializer implements ObjectDeserializer {
    public final static TreeMapDeserializer instance = new TreeMapDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(JavaObjectJSONParser parser, Type clazz) {
        Map<String, Object> map = new TreeMap<String, Object>();
        parser.parseObject(map);
        return (T) map;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
