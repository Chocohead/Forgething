package net.dblsaiko.forgething.mcpconfig.task;

import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public interface TaskType<T extends Task> {
	interface Context {
		String getGameVersion();

		boolean hasData(String key);

		JsonElement getData(String key);
	}

	T create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver);
}