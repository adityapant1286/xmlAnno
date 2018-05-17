package com.xmlanno.reflection.serializers;

import com.xmlanno.reflection.Reflections;

import java.io.File;
import java.io.InputStream;

public interface Serializer {

    Reflections read(InputStream inputStream);

    File save(Reflections reflections, String fileName);

    String toString(Reflections reflections);
}
