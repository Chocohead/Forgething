package net.dblsaiko.forgething.dlutil;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface DownloadOptionData {

    URL url();

    Path targetPath();

    List<DownloadEntry> downloadHistory();

}
