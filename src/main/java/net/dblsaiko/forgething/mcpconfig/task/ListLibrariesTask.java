package net.dblsaiko.forgething.mcpconfig.task;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.dblsaiko.forgething.ArtifactRules;
import net.dblsaiko.forgething.DatatypeUtil;
import net.dblsaiko.forgething.Main;
import net.dblsaiko.forgething.Os;
import net.dblsaiko.forgething.dlutil.DownloadOptions;
import net.dblsaiko.forgething.dlutil.DownloadUtil;
import net.dblsaiko.forgething.mcpconfig.ArgTemplate;
import net.dblsaiko.forgething.mcpconfig.McpConfigHeader;
import net.dblsaiko.forgething.verify.ChecksumTypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListLibrariesTask implements Task {

    private final String gameVersion;

    public ListLibrariesTask(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    @Override
    public Path execute(Path output, Map<String, Path> prevTaskOutputs) throws IOException {
        DownloadUtil du = DownloadUtil.INSTANCE;

        Path gameVersionsDir = Paths.get("game");
        Path target = gameVersionsDir.resolve(gameVersion);

        Path manifest = du.download(GameDownloader.VERSION_MANIFEST,
            DownloadOptions.downloadIfExists(),
            DownloadOptions.downloadOnce(),
            DownloadOptions.downloadInto(gameVersionsDir));

        Path versionManifest = du.download(GameDownloader.getVersionManifestUrl(manifest, gameVersion),
            DownloadOptions.downloadInto(target));

        List<LibraryDownloadData> ldd = getLibraryDownloadData(versionManifest);

        List<String> fileContent = ldd.stream()
            .filter($ -> $.rules.getAction(Os.getOs()) == ArtifactRules.Action.ALLOW)
            .map(data -> CompletableFuture.supplyAsync(() -> du.download(data.url,
                DownloadOptions.downloadInto(Paths.get("lib")),
                DownloadOptions.verify(ChecksumTypes.SHA1, data.sha1),
                DownloadOptions.verify(ChecksumTypes.SIZE, data.size)), Main.executor))
            .collect(Collectors.toList()).stream()
            .map(CompletableFuture::join)
            .map(path -> String.format("-e=%s", path.toAbsolutePath()))
            .collect(Collectors.toList());

        Files.write(output, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return output;
    }

    private static List<LibraryDownloadData> getLibraryDownloadData(Path versionManifest) {
        List<LibraryDownloadData> list = new ArrayList<>();
        try (InputStream is = Files.newInputStream(versionManifest)) {
            JsonReader jr = new JsonReader(new InputStreamReader(is));
            jr.beginObject();
            while (jr.hasNext()) {
                if ("libraries".equals(jr.nextName())) {
                    jr.beginArray();
                    while (jr.hasNext()) {
                        String sha1 = null;
                        long size = -1;
                        String url = null;
                        ArtifactRules rules = ArtifactRules.empty();
                        boolean hasArtifact = false;
                        jr.beginObject();
                        while (jr.hasNext()) {
                            switch (jr.nextName()) {
                                case "downloads":
                                    jr.beginObject();
                                    while (jr.hasNext()) {
                                        if ("artifact".equals(jr.nextName())) {
                                            hasArtifact = true;
                                            jr.beginObject();
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
                                            jr.endObject();
                                        } else {
                                            jr.skipValue();
                                        }
                                    }
                                    jr.endObject();
                                    break;
                                case "rules":
                                    rules = ArtifactRules.parse(jr);
                                    break;
                                default:
                                    jr.skipValue();
                                    break;
                            }
                        }
                        jr.endObject();
                        if (hasArtifact) {
                            list.add(new LibraryDownloadData(
                                new URL(url),
                                DatatypeUtil.parse(sha1),
                                size,
                                rules
                            ));
                        }
                    }
                    return list;
                } else {
                    jr.skipValue();
                }
            }
            throw new IllegalStateException("downloads object not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LibraryDownloadData {
        public final URL url;
        public final byte[] sha1;
        public final long size;
        public final ArtifactRules rules;

        private LibraryDownloadData(URL url, byte[] sha1, long size, ArtifactRules rules) {
            this.url = url;
            this.sha1 = sha1;
            this.size = size;
            this.rules = rules;
        }
    }


    public static class Type implements TaskType<ListLibrariesTask> {

        public static Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public ListLibrariesTask create(String taskName, String pipeline, McpConfigHeader header, JsonObject data, Function<String, ArgTemplate> paramResolver) {
            return new ListLibrariesTask(header.getGameVersion());
        }

    }

}
