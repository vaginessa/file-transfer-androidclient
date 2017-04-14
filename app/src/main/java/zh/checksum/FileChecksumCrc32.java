package zh.checksum;

import java.util.zip.CRC32;

public class FileChecksumCrc32 extends FileChecksum{
	private CRC32 crc32;

	public FileChecksumCrc32() {
		super();
		this.crc32=new CRC32();
	}
	@Override
	public String digest(){
		byte[] bytes = long2bytes(crc32.getValue());
		return byteToHexString(bytes);
	}
	public  byte[] long2bytes(long num) {  
	    byte[] b = new byte[8];  
	    for (int i=0;i<8;i++) {  
	        b[i] = (byte)(num>>>(56-(i*8)));  
	    }  
	    return b;  
	}  
	@Override
	public void update(byte[] input, int offset, int len) {
		crc32.update(input,offset,len);
	}
}
