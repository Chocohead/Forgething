package net.dblsaiko.forgething;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.dblsaiko.forgething.dlutil.DownloadOptions;
import net.dblsaiko.forgething.dlutil.DownloadUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfig;
import net.dblsaiko.forgething.mcpconfig.Side;

public class Main {
	public static final ExecutorService executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() + 1);
	public static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(MavenArtifactPath.class, new JsonDeserializer<MavenArtifactPath>() {
		@Override
		public MavenArtifactPath deserialize(JsonElement json, Type T, JsonDeserializationContext context) throws JsonParseException {
			return MavenArtifactPath.from(json.getAsString());
		}
	}).registerTypeHierarchyAdapter(ArgTemplate.class, new JsonDeserializer<ArgTemplate>() {
		@Override
		public ArgTemplate deserialize(JsonElement json, Type T, JsonDeserializationContext context) throws JsonParseException {
			return ArgTemplate.parse(json.getAsString());
		}
	}).create();

	public static final String MCP_MAVEN = "https://files.minecraftforge.net/maven/";
	public static final Path LOG_FILE = Paths.get("log.txt");

	public static void main(String[] args) {
		String minecraftVersion = "1.15.2";

		Path mcpconfig = getMcpConfig(minecraftVersion);
		try (McpConfig mcpc = McpConfig.from(mcpconfig)) {
			assert mcpc != null;

			Path output = mcpc.execPipeline(Side.JOINED);
			System.out.println(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Path getMcpConfig(String version) {
		URL mainArtifact = MavenArtifactPath.of("de.oceanlabs.mcp", "mcp_config", version, "", "zip").getUrlIn(MCP_MAVEN);
		return DownloadUtil.INSTANCE.download(mainArtifact, DownloadOptions.mavenVerify());
	}
}
