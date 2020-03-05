package net.dblsaiko.forgething.verify;

import java.util.Arrays;

public abstract class ArrayChecksumType implements ChecksumType<byte[]> {

    public final int length;

    public ArrayChecksumType(int length) {
        this.length = length;
    }

    @Override
    public boolean isValid(byte[] checksum) {
        return checksum.length == length;
    }

    @Override
    public boolean matches(byte[] a, byte[] b) {
        return isValid(a) && isValid(b) && Arrays.equals(a, b);
    }

}
