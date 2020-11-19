package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;

import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public class DownloadServerTask implements Task {
	private final String gameVersion;

	public DownloadServerTask(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	@Override
	public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
		return GameDownloader.download("server", gameVersion);
	}

	public enum Type implements TaskType<DownloadServerTask> {
		INSTANCE;

		@Override
		public DownloadServerTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			return new DownloadServerTask(context.getGameVersion());
		}
	}
}
