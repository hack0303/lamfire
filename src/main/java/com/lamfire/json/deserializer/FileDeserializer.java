package com.lamfire.json.deserializer;

import java.io.File;
import java.lang.reflect.Type;

import com.lamfire.json.parser.JavaObjectJSONParser;
import com.lamfire.json.parser.JSONToken;

public class FileDeserializer implements ObjectDeserializer {
    public final static FileDeserializer instance = new FileDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(JavaObjectJSONParser parser, Type clazz) {
        Object value = parser.parse();

        if (value == null) {
            return null;
        }
        
        String path = (String) value;
        
        return (T) new File(path);
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
