package org.envirocar.qad;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.envirocar.trackcount.configuration.JacksonConfiguration;
import org.envirocar.trackcount.configuration.JtsConfiguration;
import org.envirocar.trackcount.configuration.KafkaConfiguration;
import org.envirocar.trackcount.kafka.KafkaJsonDeserializer;
import org.envirocar.trackcount.model.FeatureCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {KafkaConfiguration.class})
public class TrackDeserializationTest {
	
	@Test
	public void testDeserialization() throws IOException {
		
		JtsModule jtsModule =  new JtsModule();
		
		JsonFactory jf = new JsonFactory();
		
		ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .registerModule(jtsModule);
		
        StringWriter writer = new StringWriter();
        String encoding = StandardCharsets.UTF_8.name();
        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("tmp12148761520090307978.json"), writer, encoding);
        		
		byte[] bytes = writer.toString().getBytes();
		
		FeatureCollection featureCollection = new KafkaJsonDeserializer<>(FeatureCollection.class, objectMapper).deserialize("", bytes);
		
		assertTrue(featureCollection != null);
		
	}
	
}
