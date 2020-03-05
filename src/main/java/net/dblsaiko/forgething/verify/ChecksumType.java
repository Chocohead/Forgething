package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.io.InputStream;

public interface ChecksumType<T> {

    boolean isValid(T checksum);

    boolean matches(T a, T b);

    T compute(InputStream stream) throws IOException;

}
