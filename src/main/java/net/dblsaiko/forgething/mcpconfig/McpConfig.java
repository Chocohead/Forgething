package net.dblsaiko.forgething.mcpconfig;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.Main;
import net.dblsaiko.forgething.Utils;
import net.dblsaiko.forgething.mcpconfig.task.DownloadClientTask;
import net.dblsaiko.forgething.mcpconfig.task.DownloadServerTask;
import net.dblsaiko.forgething.mcpconfig.task.InjectTask;
import net.dblsaiko.forgething.mcpconfig.task.ListLibrariesTask;
import net.dblsaiko.forgething.mcpconfig.task.NoopTask;
import net.dblsaiko.forgething.mcpconfig.task.PatchTask;
import net.dblsaiko.forgething.mcpconfig.task.StripTask;
import net.dblsaiko.forgething.mcpconfig.task.TaskType;
import net.dblsaiko.forgething.mcpconfig.task.TaskType.Context;

public class McpConfig implements Closeable {
	public static final Path WORK_DIR = Paths.get("work");

	//private final Path mcpConfigFile;
	private final Path dataDir;
	private final FileSystem zipfs;

	//private final Context context;
	//private final Map<String, TaskType<?>> tasks;
	private final Map<Side, Pipeline> pipelines;

	private McpConfig(Path mcpConfigFile,
			Path dataDir,
			FileSystem zipfs,
			Context context,
			Map<String, TaskType<?>> tasks,
			Map<Side, Pipeline> pipelines) {
		//this.mcpConfigFile = mcpConfigFile;
		this.dataDir = dataDir;
		this.zipfs = zipfs;
		//this.context = context;
		//this.tasks = tasks;
		this.pipelines = pipelines;
	}

	@Override
	public void close() throws IOException {
		zipfs.close();
		FileUtil.deleteDirectories(dataDir);
	}

	public static McpConfig from(Path path) {
		FileSystem zipfs = null;
		try {
			zipfs = FileSystems.newFileSystem(path, null);

			V1Template template;
			try (Reader in = Files.newBufferedReader(zipfs.getPath("config.json"))) {
				template = Main.GSON.fromJson(in, V1Template.class);
			}

			//We only know about V1 so shouldn't really be attempting to process anything else
			if (template.spec != 1) throw new IllegalStateException("Unexpected MCPConfig spec: " + template.spec);

			Files.createDirectories(WORK_DIR);
			Path tempDirectory = Files.createTempDirectory(WORK_DIR, "mcp-config-data");
			JsonObject data = template.extractData(zipfs, tempDirectory);

			Map<String, TaskType<?>> tasks = new HashMap<>(template.functions);
			tasks.put("downloadManifest", NoopTask.Type.INSTANCE);
			tasks.put("downloadJson", NoopTask.Type.INSTANCE);
			tasks.put("downloadClient", DownloadClientTask.Type.INSTANCE);
			tasks.put("downloadServer", DownloadServerTask.Type.INSTANCE);
			tasks.put("strip", StripTask.Type.INSTANCE);
			tasks.put("listLibraries", ListLibrariesTask.Type.INSTANCE);
			tasks.put("inject", InjectTask.Type.INSTANCE);
			tasks.put("patch", PatchTask.Type.INSTANCE);

			Context context = new Context() {
				@Override
				public String getGameVersion() {
					return template.version;
				}

				@Override
				public boolean hasData(String key) {
					return data.has(key);
				}

				@Override
				public JsonElement getData(String key) {
					if (!hasData(key)) throw new IllegalArgumentException("Missing data key: ".concat(key));
					return data.get(key);
				}
			};
			Map<Side, Pipeline> pipelines = template.steps.entrySet().stream()
					.collect(Collectors.toMap(Entry::getKey, entry -> Pipeline.from(entry.getKey(), entry.getValue(), tasks::get, context)));

			return new McpConfig(path, tempDirectory, zipfs, context, tasks, new EnumMap<>(pipelines));
		} catch (IOException e) {
			Utils.closeQuietly(e, zipfs);
			e.printStackTrace();
			return null;
		}
	}

	public Pipeline getPipeline(Side name) {
		return pipelines.get(name);
	}

	public Path execPipeline(Side name) throws IOException {
		return getPipeline(name).exec(WORK_DIR.resolve(name.getName()));
	}
}
