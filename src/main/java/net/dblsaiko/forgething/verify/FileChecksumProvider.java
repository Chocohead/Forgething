package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileChecksumProvider extends InputStreamChecksumProvider {

    private final Path path;

    private FileChecksumProvider(Path path) {
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getChecksum(ChecksumType<T> type) throws IOException {
        if (type == ChecksumTypes.SIZE) {
            return Optional.of((T) (Long) Files.size(path));
        }
        return super.getChecksum(type);
    }

    @Override
    protected InputStream openStream() throws IOException {
        return Files.newInputStream(path);
    }

    public static FileChecksumProvider of(Path path) {
        return new FileChecksumProvider(path);
    }

}
