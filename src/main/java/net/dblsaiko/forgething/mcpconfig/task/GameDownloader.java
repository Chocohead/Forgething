package net.dblsaiko.forgething.mcpconfig.task;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.google.gson.stream.JsonReader;

import net.dblsaiko.forgething.DatatypeUtil;
import net.dblsaiko.forgething.Urls;
import net.dblsaiko.forgething.dlutil.DownloadOptions;
import net.dblsaiko.forgething.dlutil.DownloadUtil;
import net.dblsaiko.forgething.verify.ChecksumTypes;

public class GameDownloader {
	public static final URL VERSION_MANIFEST = Urls.get("https://launchermeta.mojang.com/mc/game/version_manifest.json");

	private GameDownloader() {
	}

	public static Path download(String dist, String gameVersion) throws IOException {
		DownloadUtil du = DownloadUtil.INSTANCE;

		Path gameVersionsDir = Paths.get("game");
		Path target = gameVersionsDir.resolve(gameVersion);

		Path manifest = du.download(VERSION_MANIFEST,
				DownloadOptions.downloadIfExists(),
				DownloadOptions.downloadOnce(),
				DownloadOptions.downloadInto(gameVersionsDir));

		Path versionManifest = du.download(getVersionManifestUrl(manifest, gameVersion),
				DownloadOptions.downloadInto(target));

		DistDownloadData data = getDistDownloadData(versionManifest, dist);
		Path gameJar = du.download(data.url,
				DownloadOptions.downloadInto(target),
				DownloadOptions.verify(ChecksumTypes.SHA1, data.sha1),
				DownloadOptions.verify(ChecksumTypes.SIZE, data.size));

		return gameJar;
	}

	public static URL getVersionManifestUrl(Path manifest, String version) throws IOException {
		try (JsonReader jr = new JsonReader(Files.newBufferedReader(manifest))) {
			jr.beginObject();

			while (jr.hasNext() && !"versions".equals(jr.nextName())) {
				jr.skipValue();
			}
			if (!jr.hasNext()) throw new IllegalStateException("versions key not found");

			jr.beginArray();
			outer: while (jr.hasNext()) {
				jr.beginObject();

				String url = null;
				while (jr.hasNext()) {
					switch (jr.nextName()) {
					case "id":
						if (!version.equals(jr.nextString())) {
							while (jr.hasNext()) jr.skipValue();
							jr.endObject();
							continue outer;
						}
						break;

					case "url":
						url = jr.nextString();
						break;

					default:
						jr.skipValue();
						break;
					}
				}

				return new URL(Objects.requireNonNull(url));
			}

			throw new IllegalStateException(String.format("version %s not found", version));
		}
	}

	private static DistDownloadData getDistDownloadData(Path versionManifest, String dist) throws IOException {
		try (JsonReader jr = new JsonReader(Files.newBufferedReader(versionManifest))) {
			jr.beginObject();

			while (jr.hasNext()) {
				if ("downloads".equals(jr.nextName())) {
					jr.beginObject();

					while (jr.hasNext()) {
						if (dist.equals(jr.nextName())) {
							jr.beginObject();

							String sha1 = null;
							long size = -1;
							String url = null;
							while (jr.hasNext()) {
								switch (jr.nextName()) {
								case "url":
									url = jr.nextString();
									break;

								case "sha1":
									sha1 = jr.nextString();
									break;

								case "size":
									size = jr.nextLong();
									break;

								default:
									jr.skipValue();
								}
							}

							if (url == null) throw new IllegalStateException("url not set");
							if (sha1 == null) throw new IllegalStateException("sha1 not set");
							if (size < 0) throw new IllegalStateException("size not set");

							return new DistDownloadData(
									new URL(url),
									DatatypeUtil.parse(sha1),
									size
							);
						} else {
							jr.skipValue();
						}
					}

					throw new IllegalStateException(String.format("download for dist %s not found", dist));
				} else {
					jr.skipValue();
				}
			}

			throw new IllegalStateException("downloads object not found");
		}
	}

	private static class DistDownloadData {
		public final URL url;
		public final byte[] sha1;
		public final long size;

		DistDownloadData(URL url, byte[] sha1, long size) {
			this.url = url;
			this.sha1 = sha1;
			this.size = size;
		}
	}
}
