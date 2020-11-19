package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;

import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public class DownloadClientTask implements Task {
	private final String gameVersion;

	public DownloadClientTask(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	@Override
	public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
		return GameDownloader.download("client", gameVersion);
	}

	public enum Type implements TaskType<DownloadClientTask> {
		INSTANCE;

		@Override
		public DownloadClientTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			return new DownloadClientTask(context.getGameVersion());
		}
	}
}
