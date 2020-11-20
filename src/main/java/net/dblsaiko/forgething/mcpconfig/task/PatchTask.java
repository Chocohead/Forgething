package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonObject;

import com.cloudbees.diff.ContextualPatch;
import com.cloudbees.diff.ContextualPatch.PatchReport;
import com.cloudbees.diff.HunkReport;
import com.cloudbees.diff.PatchException;
import com.cloudbees.diff.PatchFile;

import net.dblsaiko.forgething.FileUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

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

		Files.walkFileTree(patchesDir, new SimpleFileVisitor<Path>() {
			private final boolean log = false;

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				assert !attrs.isDirectory();
				if (!file.toString().endsWith(".patch")) return FileVisitResult.CONTINUE;

				ContextualPatch patch = ContextualPatch.create(PatchFile.from(file.toFile()), tmpDir.toFile());
				patch.setCanonialization(true, false); //Allow the access to be wrong but not the whitespacing
				patch.setMaxFuzz(0);

				List<PatchReport> result;
				try {
					if (log) System.out.printf("Patching with %s%n", file);
					result = patch.patch(false);
				} catch (PatchException e) {//A sign that the patch itself is invalid
					throw new IllegalStateException("Patch from " + file + " crashed whilst applying", e);
				}

				for (PatchReport report : result) {
					if (report.getStatus().isSuccess()) continue; //This is fine

					System.err.printf("\tPatch failed to apply, %d hunks missed%n", report.hunkReports().size());
					for (HunkReport hunk : report.hunkReports()) {
						if (hunk.hasFailed()) {
							if (hunk.failure != null) {
								System.err.printf("\tHunk %d crashed: %s%n", hunk.hunkID, hunk.failure);
							} else {
								System.err.printf("\tHunk %d missed: %d with %d fuzz%n", hunk.hunkID, hunk.index, hunk.fuzz);
							}
						}
					}
				}

				return FileVisitResult.CONTINUE;
			}
		});

		try (FileSystem zipfs = FileSystems.newFileSystem(new URI("jar:file:" + output.toAbsolutePath()), Collections.singletonMap("create", "true"))) {
			FileUtil.copyAll(tmpDir, zipfs.getPath("/"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		FileUtil.deleteDirectories(tmpDir);
		return output;
	}

	@Override
	public Set<String> getDependencies() {
		return input.taskDependencies();
	}

	public enum Type implements TaskType<PatchTask> {
		INSTANCE;

		@Override
		public PatchTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			ArgTemplate input = paramResolver.apply("input");
			return new PatchTask(Paths.get(context.getData("patches").getAsJsonObject().get(pipeline.getName()).getAsString()), input);
		}
	}
}
