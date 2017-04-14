package zh.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileChecksumMd5 extends FileChecksum{
	private MessageDigest messageDigest;

	public FileChecksumMd5() {
		super();
		try {
			this.messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String digest(){
		byte[] bytes = messageDigest.digest();
		return byteToHexString(bytes);
	}

	@Override
	public void update(byte[] input, int offset, int len) {
		messageDigest.update(input,offset,len);
	}
}
