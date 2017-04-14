package zh.checksum;


public abstract class FileChecksum {
	
	public abstract String digest();
	public abstract void update(byte[] input, int offset, int len);
	public  String byteToHexString(byte[] tmp) {
		String s;
		// 用字节表示就是 16 个字节
		int len=tmp.length;
		char str[] = new char[len * 2]; 
		int k = 0;
		for (int i = 0; i < len; i++) {
			byte byte0 = tmp[i]; // 取第 i 个字节
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		s = new String(str); // 换后的结果转换为字符串
		return s;
	}
	static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  
	

}
