package net.dblsaiko.forgething.dlutil;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class DownloadOptionDataImpl implements DownloadOptionData {

    private final URL url;
    private final Path targetPath;
    private final List<DownloadEntry> downloadHistory;

    public DownloadOptionDataImpl(URL url, Path targetPath, List<DownloadEntry> downloadHistory) {
        this.url = url;
        this.targetPath = targetPath;
        this.downloadHistory = downloadHistory;
    }

    @Override
    public URL url() {
        return url;
    }

    @Override
    public Path targetPath() {
        return targetPath;
    }

    @Override
    public List<DownloadEntry> downloadHistory() {
        return downloadHistory;
    }

}
