package net.dblsaiko.forgething.mcpconfig.task;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;

import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public enum NoopTask implements Task {
	INSTANCE;

	@Override
	public Path execute(Path output, Map<String, Path> prevTaskOutputs) {
		return null;
	}

	public enum Type implements TaskType<NoopTask> {
		INSTANCE;

		@Override
		public NoopTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			return NoopTask.INSTANCE;
		}
	}
}
