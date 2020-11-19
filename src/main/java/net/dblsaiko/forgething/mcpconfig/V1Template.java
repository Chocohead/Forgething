package net.dblsaiko.forgething.mcpconfig;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.task.CustomTask.Type;

class V1Template {
	public final int spec;
	public final String version;
	public final Map<String, JsonElement> data;
	public final Map<Side, List<JsonObject>> steps;
	public final Map<String, Type> functions;
	public final Map<Side, List<String>> libraries;

	public V1Template(int spec, String version, Map<String, JsonElement> data, Map<Side, List<JsonObject>> steps, Map<String, Type> functions, Map<Side, List<String>> libraries) {
		this.spec = spec;
		this.version = version;
		this.data = data;
		this.steps = steps;
		this.functions = functions;
		this.libraries = libraries;
	}

	public JsonObject extractData(FileSystem fs, Path targetDir) throws IOException {
		return extractData(fs, targetDir, data.entrySet());
	}

	private static JsonObject extractData(FileSystem fs, Path targetDir, Iterable<Entry<String, JsonElement>> data) throws IOException {
		JsonObject newObject = new JsonObject();

		for (Entry<String, JsonElement> entry : data) {
			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value.isJsonPrimitive()) {
				Path path = fs.getPath(value.getAsString());

				if (Files.exists(path)) {
					Path newPath = targetDir.resolve(value.getAsString()).toAbsolutePath();

					Files.createDirectories(newPath.getParent());
					if (Files.notExists(newPath)) {
						FileUtil.copyAll(path, newPath);
					}

					newObject.addProperty(key, newPath.toString());
				} else {
					newObject.add(key, value);
				}
			} else if (value.isJsonObject()) {
				newObject.add(key, extractData(fs, targetDir, value.getAsJsonObject().entrySet()));
			} else {
				newObject.add(key, value);
			}
		}

		return newObject;
	}
}