package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class DownloadClientTask implements Task {

    private final String gameVersion;

    public DownloadClientTask(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
        return GameDownloader.download("client", gameVersion);
    }

    public static class Type implements TaskType<DownloadClientTask> {

        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public DownloadClientTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            return new DownloadClientTask(header.getGameVersion());
        }

    }

}
