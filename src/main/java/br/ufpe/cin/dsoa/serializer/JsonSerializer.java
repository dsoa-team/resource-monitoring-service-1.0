package br.ufpe.cin.dsoa.serializer;

import java.util.Map;

import com.google.gson.Gson;

public class JsonSerializer {

	private static JsonSerializer instance;

	private Gson gson;

	private JsonSerializer() {
		this.gson = new Gson();
	}

	public static JsonSerializer getInstance() {
		if (null == instance) {
			instance = new JsonSerializer();
		}
		return instance;
	}
	public String getJson(Map<String, Object> event) {
		String json = this.gson.toJson(event);

		return json;
	}
}
