package com.lamfire.json.serializer;

import java.lang.reflect.Type;
import java.util.Set;


public interface AutowiredObjectSerializer extends ObjectSerializer {
    Set<Type> getAutowiredFor();
}
