package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PatchTask implements Task {

    private final Path patchesDir;
    private final ArgTemplate input;

    public PatchTask(Path patchesDir, ArgTemplate input) {
        this.patchesDir = patchesDir;
        this.input = input;
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
        Path input = Paths.get(this.input.resolveTaskOutput(prevTaskOutputs).unwrap());
        Path tmpDir = Files.createTempDirectory(output.getParent(), "patch");
        try (FileSystem zipfs = FileSystems.newFileSystem(input, null)) {
            FileUtil.copyAll(zipfs.getPath("/"), tmpDir);
        }

        Files.walk(patchesDir)
            .filter(p -> !Files.isDirectory(p))
            .filter(p -> p.toString().endsWith(".patch"))
            .forEach(path -> patch(path, tmpDir));

        try (FileSystem zipfs = FileSystems.newFileSystem(new URI("jar:file:" + output.toAbsolutePath().toString()), Collections.singletonMap("create", "true"))) {
            FileUtil.copyAll(tmpDir, zipfs.getPath("/"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        FileUtil.deleteDirectories(tmpDir);

        return output;
    }

    private static boolean patch(Path patchFile, Path dir) {
        try {
            return new ProcessBuilder("patch", "-uNtlp1")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(patchFile.toFile())
                .directory(dir.toFile())
                .start()
                .waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Type implements TaskType<PatchTask> {

        public static Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public PatchTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            ArgTemplate input = paramResolver.apply("input");
            return new PatchTask(Paths.get(header.getData().getAsJsonObject("patches").get(pipeline).getAsString()), input);
        }

    }

    @Override
    public Set<String> getDependencies() {
        return input.taskDependencies();
    }

}
