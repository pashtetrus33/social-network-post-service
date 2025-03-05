package ru.skillbox.social_network_post.config;

import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

public class BooleanDecoder implements Decoder {

    private final Decoder defaultDecoder = new Decoder.Default();

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (type == Boolean.class) {
            String body = Util.toString((Reader) response.body());
            return Boolean.parseBoolean(body);
        }
        return defaultDecoder.decode(response, type);
    }
}
