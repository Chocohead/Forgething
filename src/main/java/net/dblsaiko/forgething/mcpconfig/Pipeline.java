package net.dblsaiko.forgething.mcpconfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.task.Task;
import net.dblsaiko.forgething.mcpconfig.task.TaskType;
import net.dblsaiko.forgething.mcpconfig.task.TaskType.Context;

public class Pipeline {
	private final List<Step<?>> steps;

	public Pipeline(List<Step<?>> steps) {
		this.steps = steps;
	}

	public static Pipeline from(Side name, List<JsonObject> pipeline, Function<String, TaskType<?>> taskProvider, Context context) {
		List<Step<?>> steps = pipeline.stream()
				.map(obj -> Step.from(name, obj, taskProvider, context))
				.collect(Collectors.toList());

		return new Pipeline(steps);
	}

	public Path exec(Path workDir) throws IOException {
		if (Files.exists(workDir)) {
			FileUtil.deleteDirectories(workDir);
		}
		Files.createDirectories(workDir);

		Map<String, Path> prevOutputs = new HashMap<>();
		Map<String, Path> prevOutputView = Collections.unmodifiableMap(prevOutputs);

		Path output = null;
		for (Step<?> step : steps) {
			output = workDir.resolve(step.name);
			output = step.task.execute(output, prevOutputView);
			if (output == null) continue;

			if (output.startsWith(workDir))
				output = fixFileName(output);

			prevOutputs.put(step.name, output);
		}

		return output;
	}

	private static Path fixFileName(Path output) throws IOException {
		if (isZip(output)) {
			// fix for fernflower shitting itself if the input file doesn't have an extension
			Path newOutput = output.getParent().resolve(String.format("%s.zip", output.getFileName()));
			Files.move(output, newOutput);
			return newOutput;
		}

		return output;
	}

	private static boolean isZip(Path p) {
		if (Files.isDirectory(p)) return false;

		try (InputStream is = Files.newInputStream(p)) {
			byte[] header = new byte[4];
			int pos = 0;

			int len;
			while (pos < 4 && (len = is.read(header, pos, header.length - pos)) != -1) {
				pos += len;
			}

			// check if header is 50 4b 03 04
			return pos == 4 && Arrays.hashCode(header) == 0x338f1d;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static class Step<T extends Task> {
		public final String name;
		public final T task;

		public Step(String name, T task) {
			this.name = name;
			this.task = task;
		}

		public static Step<?> from(Side pipeline, JsonObject object, Function<String, TaskType<?>> taskProvider, Context context) {
			String typeId = object.getAsJsonPrimitive("type").getAsString();
			String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : typeId;
			TaskType<?> type = taskProvider.apply(typeId);
			Function<String, ArgTemplate> f = key -> object.has(key) ? ArgTemplate.parse(object.getAsJsonPrimitive(key).getAsString()) : null;
			Task task = type.create(name, pipeline, context, object, f);
			return new Step<>(name, task);
		}
	}
}
