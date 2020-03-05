package net.dblsaiko.forgething;

import net.dblsaiko.forgething.dlutil.DownloadOptions;
import net.dblsaiko.forgething.dlutil.DownloadUtil;
import net.dblsaiko.forgething.mcpconfig.McpConfig;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final ExecutorService executor = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() + 1);

    public static final String MCP_MAVEN = "https://files.minecraftforge.net/maven/";

    public static final Path LOG_FILE = Paths.get("log.txt");

    public static void main(String[] args) {
        String minecraftVersion = "1.15.2";
        Path mcpconfig = getMcpConfig(minecraftVersion);
        try (McpConfig mcpc = McpConfig.from(mcpconfig)) {
            assert mcpc != null;
            Path output = mcpc.execPipeline("joined");
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path getMcpConfig(String version) {
        URL mainArtifact = MavenArtifactPath.from(String.format("de.oceanlabs.mcp:mcp_config:%s@zip", version)).getUrlIn(MCP_MAVEN);
        return DownloadUtil.INSTANCE.download(mainArtifact, DownloadOptions.mavenVerify());
    }

}
