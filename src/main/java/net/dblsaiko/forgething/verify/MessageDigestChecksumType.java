package net.dblsaiko.forgething.verify;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestChecksumType extends ArrayChecksumType {

    private final String algorithm;

    public MessageDigestChecksumType(String algorithm) {
        super(create(algorithm).getDigestLength());
        this.algorithm = algorithm;
    }

    protected MessageDigest create() {
        return create(algorithm);
    }

    @Override
    public byte[] compute(InputStream stream) throws IOException {
        MessageDigest md = create();
        byte[] buf = new byte[4096];
        int len;
        while ((len = stream.read(buf)) != -1) {
            md.update(buf, 0, len);
        }
        return md.digest();
    }

    private static MessageDigest create(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
