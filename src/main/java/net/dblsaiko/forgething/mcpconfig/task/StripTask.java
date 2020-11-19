package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.JsonObject;

import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.Side;

public class StripTask implements Task {
	private final ArgTemplate input;

	public StripTask(ArgTemplate input) {
		this.input = input;
	}

	@Override
	public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
		Path input = Paths.get(this.input.resolveTaskOutput(prevTaskOutputs).unwrap());

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(input));
				ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			byte[] buf = new byte[4096];

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && takeEntry(entry)) {
					zos.putNextEntry(entry);

					int len;
					while ((len = zis.read(buf)) != -1) {
						zos.write(buf, 0, len);
					}

					zos.closeEntry();
				}

				zis.closeEntry();
			}
		} catch (IOException e) {
			try {
				Files.deleteIfExists(output);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			throw e;
		}

		return output;
	}

	private boolean takeEntry(ZipEntry ze) {
		return
				!ze.getName().startsWith("data/") &&
				!ze.getName().startsWith("assets/") &&
				(
						!ze.getName().contains("/") ||
						ze.getName().startsWith("com/mojang/blaze3d/") ||
						ze.getName().startsWith("com/mojang/realmsclient/") ||
						ze.getName().startsWith("net/minecraft/")
				) &&
				ze.getName().endsWith(".class");
	}

	public enum Type implements TaskType<StripTask> {
		INSTANCE;

		@Override
		public StripTask create(String taskName, Side pipeline, Context context, JsonObject data, Function<String, ArgTemplate> paramResolver) {
			ArgTemplate input = paramResolver.apply("input");
			return new StripTask(input);
		}
	}
}
