package net.dblsaiko.forgething.dlutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import net.dblsaiko.forgething.verify.ChecksumType;
import net.dblsaiko.forgething.verify.FileChecksumProvider;

public class DownloadOptions {
	private static final DownloadOption DOWNLOAD_IF_EXISTS = new DownloadOption() {
		@Override
		public boolean wantsDownload(DownloadOptionData data) {
			return true;
		}

	};
	private static final DownloadOption DOWNLOAD_ONCE = new DownloadOption() {
		@Override
		public boolean blocksDownload(DownloadOptionData data) {
			return data.downloadHistory().contains(DownloadEntry.of(data.url(), data.targetPath()));
		}

	};

	public static DownloadOption downloadIfExists() {
		return DOWNLOAD_IF_EXISTS;
	}

	public static DownloadOption downloadOnce() {
		return DOWNLOAD_ONCE;
	}

	public static <T> DownloadOption verify(ChecksumType<T> type, T reference) {
		return new DownloadOption() {
			@Override
			public boolean wantsDownload(DownloadOptionData data) {
				if (Files.notExists(data.targetPath())) return true;

				FileChecksumProvider fcp = FileChecksumProvider.of(data.targetPath());
				try {
					T cs = fcp.getChecksum(type).get();
					return !type.matches(cs, reference);
				} catch (IOException e) {
					e.printStackTrace();
					return true;
				}
			}
		};
	}

	public static DownloadOption mavenVerify() {
		return MavenVerifyDownloadOption.INSTANCE;
	}

	public static DownloadOption downloadInto(Path directory) {
		return new DownloadOption() {
			@Override
			public Optional<Path> outputDirectory(DownloadOptionData data) {
				return Optional.of(directory);
			}
		};
	}

	public static DownloadOption withFileName(String fileName) {
		return new DownloadOption() {
			@Override
			public Optional<String> filename(DownloadOptionData data) {
				return Optional.of(fileName);
			}
		};
	}
}
