package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class DownloadServerTask implements Task {

    private final String gameVersion;

    public DownloadServerTask(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
        return GameDownloader.download("server", gameVersion);
    }

    public static class Type implements TaskType<DownloadServerTask> {

        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public DownloadServerTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            return new DownloadServerTask(header.getGameVersion());
        }

    }

}
