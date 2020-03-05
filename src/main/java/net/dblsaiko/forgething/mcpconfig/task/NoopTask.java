package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class NoopTask implements Task {

    public static NoopTask INSTANCE = new NoopTask();

    private NoopTask() {
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) {
        return null;
    }

    public static class Type implements TaskType<NoopTask> {

        public static Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public NoopTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            return NoopTask.INSTANCE;
        }

    }

}
