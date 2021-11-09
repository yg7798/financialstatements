package com.tekion.accounting.fs.core.minimisedResource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tekion.accounting.fs.core.metadata.AccountingBeanMetaDataHolder;
import com.tekion.core.utils.TCollectionUtils;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.tekion.accounting.fs.core.metadata.AccountingBeanMetaDataHolder.*;


public class MinimizedResourceSerializer extends StdSerializer<MinimizedResource> {

    public MinimizedResourceSerializer() {
        this(null);
    }
    public MinimizedResourceSerializer(Class<MinimizedResource> t) {
        super(t);
    }


    @SneakyThrows
    @Override
    public void serialize(MinimizedResource minimizedResource, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if(minimizedResource.getMinimizedResourceMetaData().isAddMinimizedFlag()) {
            jgen.writeBooleanField(MinimizedResourceMetaData.MINIMIZED, minimizedResource.isMinimizeObject());
        }
        Object minimisedResourceData = minimizedResource.getData();
        if (minimizedResource.isMinimizeObject()) {
            AccountingBeanMetaDataHolder.registerFields(minimisedResourceData.getClass());

            Map<String, Field> fieldNameToFieldObjectMap = getFieldsMap(minimisedResourceData.getClass());
            if (minimizedResource.getMinimizedResourceMetaData().getIncludeType().equals(MinimizedResourceMetaData.IncludeType.INCLUSION)) {
                Set<String> fields = minimizedResource.getMinimizedResourceMetaData().getFields();
                for (String s : TCollectionUtils.nullSafeCollection(fields)) {
                    if (fieldNameToFieldObjectMap.containsKey(s)) {
                        Field field = fieldNameToFieldObjectMap.get(s);
                        Method getter = getMethod(minimisedResourceData.getClass().getName(),field.getName());

                        JsonSerializer jsonSerializer = getSerializer(minimisedResourceData.getClass().getName(),field.getName());
                        Object value = getter.invoke(minimisedResourceData);
                        if (Objects.nonNull(jsonSerializer)) {
                            if (Objects.isNull(value)) {
                                jgen.writeNullField(field.getName());
                            } else {
                                jgen.writeFieldName(field.getName());
                                jsonSerializer.serialize(value, jgen, provider);
                            }
                        } else {
                            provider.defaultSerializeField(field.getName(), value, jgen);
                        }
                    }
                }
            } else {
                Collection<Field> allFields = getFields(minimizedResource.getData().getClass());
                List<Field> fields = new ArrayList<>(allFields);
                fields.removeIf(field -> minimizedResource.getMinimizedResourceMetaData().getFields().contains(field.getName()));
                for (Field f : fields) {
                    if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        Method getter = getMethod(minimisedResourceData.getClass().getName(),f.getName());
                        provider.defaultSerializeField(f.getName(), getter.invoke(minimisedResourceData), jgen);
                    }
                }
            }
        } else {
            for (Field f : getFields(minimisedResourceData.getClass())) {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    Method getter = getMethod(minimisedResourceData.getClass().getName(),f.getName());
                    provider.defaultSerializeField(f.getName(), getter.invoke(minimisedResourceData), jgen);
                }
            }
        }
        jgen.writeEndObject();
    }

}
