package net.dblsaiko.forgething.dlutil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import net.dblsaiko.forgething.DatatypeUtil;
import net.dblsaiko.forgething.Main;
import net.dblsaiko.forgething.verify.ChecksumType;
import net.dblsaiko.forgething.verify.ChecksumTypes;
import net.dblsaiko.forgething.verify.FileChecksumProvider;

public enum MavenVerifyDownloadOption implements DownloadOption {
	INSTANCE;

	@Override
	public boolean wantsDownload(DownloadOptionData data) {
		if (Files.notExists(data.targetPath())) return true;
		FileChecksumProvider fcp = FileChecksumProvider.of(data.targetPath());
		return CompletableFuture.supplyAsync(() -> failsVerify(data.url(), "md5", ChecksumTypes.MD5, fcp), Main.executor)
				.thenCombineAsync(CompletableFuture.supplyAsync(() -> failsVerify(data.url(), "sha1", ChecksumTypes.SHA1, fcp), Main.executor), Boolean::logicalOr)
				.thenCombineAsync(CompletableFuture.supplyAsync(() -> failsVerify(data.url(), "sha256", ChecksumTypes.SHA256, fcp), Main.executor), Boolean::logicalOr).join();
	}

	private boolean failsVerify(URL url, String ext, ChecksumType<byte[]> type, FileChecksumProvider fcp) {
		try {
			URL checksumUrl = new URL(String.format("%s.%s", url.toExternalForm(), ext));
			byte[] checksum = fcp.getChecksum(type).get();
			URLConnection conn = checksumUrl.openConnection();
			conn.connect();
			try (InputStream is = conn.getInputStream()) {
				byte[] buf = new byte[4096];
				int total = 0;
				int len;
				while ((len = is.read(buf, total, buf.length - total)) != -1) {
					total += len;
				}
				byte[] reference = DatatypeUtil.parse(new String(buf, 0, total, StandardCharsets.UTF_8));
				if (reference.length != checksum.length) {
					throw new IllegalStateException(String.format("Expected %d bytes of checksum data, got %d for %s", checksum.length, total, checksumUrl));
				}
				return !type.matches(reference, checksum);
			}
		} catch (IOException e) {
			return false;
		}
	}

}
