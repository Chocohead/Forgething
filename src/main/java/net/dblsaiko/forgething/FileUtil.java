package net.dblsaiko.forgething;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileUtil {
	private FileUtil() {
	}

	public static void deleteDirectories(Path dir) throws IOException {
		List<Path> files = Files.walk(dir).collect(Collectors.toList());
		Collections.reverse(files);
		for (Path file : files) {
			Files.delete(file);
		}
	}

	public static void copyAll(Path source, Path dest) throws IOException {
		copyAll(source, dest, _path -> true);
	}

	public static void copyAll(Path source, Path dest, Predicate<Path> filter) throws IOException {
		if (!Files.isDirectory(source)) {
			Files.copy(source, dest);
			return;
		}

		for (Path path : (Iterable<Path>) Files.walk(source).filter(path -> !Files.isDirectory(path))::iterator) {
			if (filter.test(path)) {
				Path destFile = dest.resolve(source.toAbsolutePath().relativize(path).toString());

				Files.createDirectories(destFile.getParent());
				Files.copy(path, destFile);
			}
		}
	}
}
