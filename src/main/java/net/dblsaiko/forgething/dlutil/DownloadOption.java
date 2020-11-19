package net.dblsaiko.forgething.dlutil;

import java.nio.file.Path;
import java.util.Optional;

public interface DownloadOption {
	default boolean wantsDownload(DownloadOptionData data) {
		return false;
	}

	default boolean blocksDownload(DownloadOptionData data) {
		return false;
	}

	default Optional<Path> outputDirectory(DownloadOptionData data) {
		return Optional.empty();
	}

	default Optional<String> filename(DownloadOptionData data) {
		return Optional.empty();
	}
}
