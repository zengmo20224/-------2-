package com.petcare.common.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Jackson serializer that writes a snowflake {@link Long} ID as a JSON string.
 *
 * <p>Applied via {@code @JsonSerialize(using = SnowflakeIdSerializer.class)} on
 * individual response DTO fields that carry snowflake IDs.  This avoids a global
 * Long-to-String conversion which would break pagination totals, quantities and
 * other non-ID numeric values.</p>
 *
 * <p>For {@code List<Long>} ID collections use
 * {@code @JsonSerialize(contentUsing = SnowflakeIdSerializer.class)}.</p>
 */
public class SnowflakeIdSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(value.toString());
        }
    }
}
