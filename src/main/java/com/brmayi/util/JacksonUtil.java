package com.brmayi.util;

/**
 * Created by xiexiaojing on 2018/8/7.
 */
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class JacksonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtil.class);
    private static ObjectMapper mapper;

    public JacksonUtil() {
    }

    public static synchronized ObjectMapper getMapperInstance(boolean createNew) {
        if(createNew) {
            return new ObjectMapper();
        } else {
            if(mapper == null) {
                mapper = new ObjectMapper();
                mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.setSerializationInclusion(Include.NON_NULL);
                mapper.setLocale(Locale.CHINA);
            }

            return mapper;
        }
    }

    public static String toJson(Object param) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
            String dataJson = e.writeValueAsString(param);
            return dataJson;
        } catch (Exception var3) {
            LOGGER.warn("toJson", var3);
            return null;
        }
    }

    public static String toJsonWithRoot(Object param) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            String dataJson = e.writeValueAsString(param);
            return dataJson;
        } catch (Exception var3) {
            LOGGER.warn("toJson", var3);
            return null;
        }
    }

    public static <T> T jsonToBeanWithRoot(String json, Class<T> cls) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            return e.readValue(json, cls);
        } catch (Exception var3) {
            LOGGER.warn("jsonToBean", var3);
            return null;
        }
    }

    public static <T> T jsonToBean(String json, Class<T> cls) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
            return e.readValue(json, cls);
        } catch (Exception var3) {
            LOGGER.warn("jsonToBean", var3);
            return null;
        }
    }

    public static <T> T jsonToBeanByTypeReference(String json, TypeReference typeReference) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
            return e.readValue(json, typeReference);
        } catch (Exception var3) {
            LOGGER.warn("jsonToBean", var3);
            return null;
        }
    }

    public static <T> T jsonToBeanByTypeReferenceWithRoot(String json, TypeReference typeReference) {
        try {
            ObjectMapper e = getMapperInstance(false);
            e.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            return e.readValue(json, typeReference);
        } catch (Exception var3) {
            LOGGER.warn("jsonToBean", var3);
            return null;
        }
    }
}