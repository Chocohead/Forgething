package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.util.Optional;

public interface ChecksumProvider {

    <T> Optional<T> getChecksum(ChecksumType<T> type) throws IOException;

}
