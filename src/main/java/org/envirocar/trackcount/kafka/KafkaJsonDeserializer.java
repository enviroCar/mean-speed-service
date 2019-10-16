package org.envirocar.trackcount.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class KafkaJsonDeserializer<T> implements KafkaDeserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaJsonDeserializer.class);
    private final Class<T> type;
    private final ObjectMapper objectMapper;

    public KafkaJsonDeserializer(Class<T> type, ObjectMapper objectMapper) {
        this.type = Objects.requireNonNull(type);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        try {
        	File file = File.createTempFile("tmp", ".json");
        	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        	bufferedWriter.write(new String(bytes));
        	bufferedWriter.close();
        	LOG.info(file.getAbsolutePath());
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            LOG.error("Error reading " + type, e);
        }
        return null;
    }
}
