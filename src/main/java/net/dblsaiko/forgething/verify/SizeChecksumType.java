package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.io.InputStream;

public class SizeChecksumType implements ChecksumType<Long> {

    public static final SizeChecksumType INSTANCE = new SizeChecksumType();

    private SizeChecksumType() {
    }

    @Override
    public boolean isValid(Long checksum) {
        return checksum >= 0;
    }

    @Override
    public boolean matches(Long a, Long b) {
        return isValid(a) && isValid(b) && a.equals(b);
    }

    @Override
    public Long compute(InputStream stream) throws IOException {
        long total = 0;
        while (stream.available() > 0) {
            total += stream.skip(stream.available());
        }
        return total;
    }

}
