package net.dblsaiko.forgething.mcpconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.task.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class McpConfig implements Closeable {

    public static final Path WORK_DIR = Paths.get("work");

    private final Path mcpConfigFile;
    private final Path dataDir;
    private final FileSystem zipfs;

    private final McpConfigHeader header;
    private final Map<String, TaskType<?>> tasks;
    private final Map<String, Pipeline> pipelines;

    private McpConfig(Path mcpConfigFile,
                      Path dataDir,
                      FileSystem zipfs,
                      McpConfigHeader header,
                      Map<String, TaskType<?>> tasks,
                      Map<String, Pipeline> pipelines) {
        this.mcpConfigFile = mcpConfigFile;
        this.dataDir = dataDir;
        this.zipfs = zipfs;
        this.header = header;
        this.tasks = tasks;
        this.pipelines = pipelines;
    }

    @Override
    public void close() throws IOException {
        zipfs.close();
        FileUtil.deleteDirectories(dataDir);
    }

    public static McpConfig from(Path path) {
        try {
            FileSystem zipfs = FileSystems.newFileSystem(path, null);
            Path config = zipfs.getPath("config.json");
            try (InputStream is = Files.newInputStream(config)) {
                JsonObject root = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                Path tempDirectory = Files.createTempDirectory(WORK_DIR, "mcp-config-data");
                root.add("data", extractFiles(zipfs, tempDirectory, root.getAsJsonObject("data")));
                McpConfigHeader header = McpConfigHeader.from(root);
                Map<String, CustomTask.Type> ctc = loadCustomTaskMap(root);
                Map<String, TaskType<?>> tasks = new HashMap<>(ctc);
                tasks.put("downloadManifest", NoopTask.Type.INSTANCE);
                tasks.put("downloadJson", NoopTask.Type.INSTANCE);
                tasks.put("downloadClient", DownloadClientTask.Type.INSTANCE);
                tasks.put("downloadServer", DownloadServerTask.Type.INSTANCE);
                tasks.put("strip", StripTask.Type.INSTANCE);
                tasks.put("listLibraries", ListLibrariesTask.Type.INSTANCE);
                tasks.put("inject", InjectTask.Type.INSTANCE);
                tasks.put("patch", PatchTask.Type.INSTANCE);
                Map<String, Pipeline> pipelines = loadPipelineMap(root, tasks::get, header);

                return new McpConfig(path, tempDirectory, zipfs, header, tasks, pipelines);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject extractFiles(FileSystem zipfs, Path targetDir, JsonObject obj) throws IOException {
        JsonObject newObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                Path path = zipfs.getPath(value.getAsString());
                if (Files.exists(path)) {
                    Path newPath = targetDir.resolve(value.getAsString()).toAbsolutePath();
                    Files.createDirectories(newPath.getParent());
                    if (Files.notExists(newPath)) {
                        FileUtil.copyAll(path, newPath);
                    }
                    newObject.addProperty(key, newPath.toString());
                } else {
                    newObject.add(key, value);
                }
            } else if (value.isJsonObject()) {
                newObject.add(key, extractFiles(zipfs, targetDir, value.getAsJsonObject()));
            } else {
                newObject.add(key, value);
            }
        }
        return newObject;
    }

    public Pipeline getPipeline(String name) {
        return pipelines.get(name);
    }

    public Path execPipeline(String name) throws IOException {
        return getPipeline(name).exec(Paths.get("work").resolve(name));
    }

    private static Map<String, CustomTask.Type> loadCustomTaskMap(JsonObject root) {
        JsonObject functions = root.getAsJsonObject("functions");
        return functions.keySet().stream()
            .collect(Collectors.toMap($ -> $, key -> CustomTask.Type.from(functions.getAsJsonObject(key))));
    }

    private static Map<String, Pipeline> loadPipelineMap(JsonObject root, Function<String, TaskType<?>> taskProvider, McpConfigHeader header) {
        JsonObject steps = root.getAsJsonObject("steps");
        return steps.keySet().stream()
            .collect(Collectors.toMap($ -> $, key -> Pipeline.from(key, steps.getAsJsonArray(key), taskProvider, header)));
    }
}
