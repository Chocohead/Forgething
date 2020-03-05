package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InjectTask implements Task {

    private final ArgTemplate input;
    private final Path injected;

    public InjectTask(ArgTemplate input, Path injected) {
        this.input = input;
        this.injected = injected;
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
        Path input = Paths.get(this.input.resolveTaskOutput(prevTaskOutputs).unwrap());
        Files.copy(input, output);
        try (FileSystem zipfs = FileSystems.newFileSystem(output, null)) {
            Path packageInfoTemplate = injected.resolve("package-info-template.java");
            FileUtil.copyAll(injected, zipfs.getPath("/"), path -> !path.equals(packageInfoTemplate));
            List<Path> dirs = Files.walk(zipfs.getPath("/"))
                .filter(p -> Files.isDirectory(p))
                .collect(Collectors.toList());
            List<String> lines = Files.readAllLines(packageInfoTemplate, StandardCharsets.UTF_8);
            for (Path dir : dirs) {
                boolean b = Files.list(dir)
                    .anyMatch($ -> $.toString().endsWith(".java"));
                if (!b) continue;
                String pkg = dir.toString().replaceFirst("^/+", "").replace('/', '.');
                List<String> newLines = lines.stream()
                    .map($ -> $.replace("{PACKAGE}", pkg))
                    .collect(Collectors.toList());
                Files.write(dir.resolve("package-info.java"), newLines);
            }
        }
        return output;
    }

    public static class Type implements TaskType<InjectTask> {

        public static Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public InjectTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            ArgTemplate input = paramResolver.apply("input");
            Path path = Paths.get(header.getData().get("inject").getAsString());
            return new InjectTask(input, path);
        }

    }

    @Override
    public Set<String> getDependencies() {
        return input.taskDependencies();
    }

}
