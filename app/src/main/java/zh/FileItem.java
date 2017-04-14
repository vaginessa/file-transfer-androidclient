package zh;

public class FileItem {
	
    private String name;
    private String path;
    private long size;
    private byte[] crc32;
    private byte[] md5;
    private byte[] sha1;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public byte[] getCrc32() {
		return crc32;
	}
	public void setCrc32(byte[] crc32) {
		this.crc32 = crc32;
	}
	public byte[] getMd5() {
		return md5;
	}
	public void setMd5(byte[] md5) {
		this.md5 = md5;
	}
	public byte[] getSha1() {
		return sha1;
	}
	public void setSha1(byte[] sha1) {
		this.sha1 = sha1;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
    
     
    
}
