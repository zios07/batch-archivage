package com.cirb.archiver.batch.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonUtils {

	private JsonUtils() {

	}

	public static <T> String convertObjectToJsonString(T object) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		// Invalid when performing deserialization, use this format instead yyyy-MM-dd'T'HH:mm:ss.SSSZ
//		objectMapper.setDateFormat(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));
		ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
		return objectWriter.writeValueAsString(object);
	}

}