package net.dblsaiko.forgething.verify;

public class ChecksumTypes {

    public static final MessageDigestChecksumType MD5 = new MessageDigestChecksumType("MD5");
    public static final MessageDigestChecksumType SHA1 = new MessageDigestChecksumType("SHA-1");
    public static final MessageDigestChecksumType SHA256 = new MessageDigestChecksumType("SHA-256");

    public static final SizeChecksumType SIZE = SizeChecksumType.INSTANCE;

}
