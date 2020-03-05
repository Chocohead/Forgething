package net.dblsaiko.forgething.dlutil;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class DownloadEntry {

    private final URL url;
    private final Path target;

    private DownloadEntry(URL url, Path target) {
        this.url = url;
        this.target = target;
    }

    public URL getUrl() {
        return url;
    }

    public Path getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadEntry that = (DownloadEntry) o;
        return Objects.equals(url, that.url) &&
            Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, target);
    }

    public static DownloadEntry of(URL url, Path target) {
        return new DownloadEntry(url, target.normalize());
    }

}
