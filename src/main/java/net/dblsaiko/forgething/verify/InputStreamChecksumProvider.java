package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InputStreamChecksumProvider implements ChecksumProvider {

    private final Map<ChecksumType<?>, Object> checksums = new ConcurrentHashMap<>();

    protected abstract InputStream openStream() throws IOException;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getChecksum(ChecksumType<T> type) throws IOException {
        if (!checksums.containsKey(type)) {
            try (InputStream is = openStream()) {
                checksums.put(type, type.compute(is));
            }
        }
        return Optional.of((T) checksums.get(type));
    }

}
