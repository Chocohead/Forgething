package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import net.dblsaiko.forgething.MavenArtifactPath;
import net.dblsaiko.forgething.dlutil.DownloadOptions;
import net.dblsaiko.forgething.dlutil.DownloadUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public class CustomTask implements Task {
	private final MavenArtifactPath artifact;
	private final List<ArgTemplate> args;
	private final List<String> jvmArgs;
	private final String repo;

	public CustomTask(MavenArtifactPath artifact, List<ArgTemplate> args, List<String> jvmArgs, String repo) {
		this.artifact = artifact;
		this.args = args;
		this.jvmArgs = jvmArgs;
		this.repo = repo;
	}

	@Override
	public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
		Path path = DownloadUtil.INSTANCE.download(artifact.getUrlIn(repo),
				DownloadOptions.downloadInto(Paths.get("lib")),
				DownloadOptions.mavenVerify());

		List<String> commandList = Stream.of(
				Stream.of("java"),
				jvmArgs.stream(),
				Stream.of("-jar", path.toAbsolutePath().toString()),
				args.stream().map($ -> $.resolveSelfOutput(output).resolveTaskOutput(prevTaskOutputs).unwrap())
				).flatMap(Function.identity()).collect(Collectors.toList());

		Process process = new ProcessBuilder().command(commandList).inheritIO().start();
		try {
			if (process.waitFor() != 0) throw new RuntimeException("process exited with status " + process.exitValue());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return output;
	}

	@Override
	public Set<String> getDependencies() {
		return args.stream().flatMap($ -> $.taskDependencies().stream()).collect(Collectors.toSet());
	}

	public static class Type implements TaskType<CustomTask> {
		@SerializedName("version") //An odd choice of name really
		private final MavenArtifactPath artifact;
		private final List<ArgTemplate> args;
		private final List<String> jvmArgs;
		private final String repo;

		public Type(MavenArtifactPath artifact, List<ArgTemplate> args, List<String> jvmArgs, String repo) {
			this.artifact = artifact;
			this.args = args;
			this.jvmArgs = jvmArgs;
			this.repo = repo;
		}

		@Override
		public CustomTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			return new CustomTask(
					artifact,
					args.stream().map($ -> $.resolveVariables(context, paramResolver)).collect(Collectors.toList()),
					jvmArgs == null || jvmArgs.isEmpty() ? Collections.emptyList() : jvmArgs,
					repo
			);
		}
	}
}
