package net.dblsaiko.forgething.dlutil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.dblsaiko.forgething.Main;

public class DownloadUtil {
	public static final DownloadUtil INSTANCE = new DownloadUtil(Paths.get("cache"));
	private final Path defaultDownloadDir;
	private final List<DownloadEntry> downloadHistory = new ArrayList<>();

	public DownloadUtil(Path defaultDownloadDir) {
		this.defaultDownloadDir = defaultDownloadDir;
	}

	public Path download(URL url, DownloadOption... options) {
		Path outputFile = getOutputFile(url);
		DownloadOptionData data = new DownloadOptionDataImpl(url, outputFile, Collections.unmodifiableList(downloadHistory));
		Optional<String> filename = Optional.empty();
		Optional<Path> outDir = Optional.empty();

		for (DownloadOption option : options) {
			Optional<String> filename1 = option.filename(data);
			if (filename1.isPresent()) {
				if (filename.isPresent())
					throw new IllegalStateException(String.format("conflicting filename overrides: '%s', '%s", filename.get(), filename1.get()));

				filename = filename1;
			}

			Optional<Path> outDir1 = option.outputDirectory(data);
			if (outDir1.isPresent()) {
				if (outDir.isPresent())
					throw new IllegalStateException(String.format("conflicting output directory overrides: '%s', '%s", outDir.get(), outDir1.get()));

				outDir = outDir1;
			}
		}

		Path outDir1 = defaultDownloadDir.resolve(outDir.orElse(Paths.get(".")));
		outputFile = outDir1.resolve(filename.orElse(outputFile.getFileName().toString()));
		DownloadOptionData data1 = new DownloadOptionDataImpl(url, outputFile, Collections.unmodifiableList(downloadHistory));
		List<CompletableFuture<Boolean>> wantsDownloadFutures = new ArrayList<>();
		List<CompletableFuture<Boolean>> blocksDownloadFutures = new ArrayList<>();

		for (DownloadOption option : options) {
			wantsDownloadFutures.add(CompletableFuture.supplyAsync(() -> option.wantsDownload(data1), Main.executor));
			blocksDownloadFutures.add(CompletableFuture.supplyAsync(() -> option.blocksDownload(data1), Main.executor));
		}

		boolean wantsDownload = wantsDownloadFutures.parallelStream().anyMatch(CompletableFuture::join);
		boolean blocksDownload = blocksDownloadFutures.parallelStream().anyMatch(CompletableFuture::join);
		boolean shouldDownload = Files.notExists(outputFile) || wantsDownload && !blocksDownload;
		if (shouldDownload) {
			download0(url, outputFile);
		}

		if (Files.notExists(outputFile))
			throw new IllegalStateException(String.format("Failed to download '%s'.", url));

		return outputFile;
	}

	private void download0(URL url, Path outputFile) {
		Path tempFile = null;
		try {
			tempFile = Files.createTempFile("filedl", null);

			try (InputStream inputStream = url.openStream(); OutputStream os = Files.newOutputStream(tempFile)) {
				byte[] buf = new byte[4096];

				int len;
				while ((len = inputStream.read(buf)) != -1) {
					os.write(buf, 0, len);
				}
			}

			Files.createDirectories(outputFile.getParent());
			Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING);

			downloadHistory.add(DownloadEntry.of(url, outputFile));
		} catch (IOException e) {
			if (tempFile != null) {
				try {
					Files.deleteIfExists(tempFile);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private Path getOutputFile(URL url) {
		String[] pathComponents = url.getPath().split("/");
		return defaultDownloadDir.resolve(pathComponents[pathComponents.length - 1]);
	}
}
