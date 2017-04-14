package zh;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class Connection {
	private InputStream in;
	private OutputStream out;

	public Connection(InputStream inReal, OutputStream out) {
		super();
		this.in =inReal; 
		this.out = out;
	}
	private byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	private long bytes2Long(byte[] byteNum) {
		long num = 0;
		for (int ix = 0; ix < 8; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	private byte[] int2Bytes(int num) {
		byte[] byteNum = new byte[4];
		for (int ix = 0; ix < 4; ++ix) {
			int offset = 32 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	private int bytes2Int(byte[] byteNum) {
		int num = 0;
		for (int ix = 0; ix < 4; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	public String readString() throws IOException {
		int len = bytes2Int(readBytesByLen(4));
		String result = bytes2String(readBytesByLen(len));
		return result;
	}

	public long readLong() throws IOException {
		long result = bytes2Long(readBytesByLen(8));
		return result;
	}
	public void writeString(String value) throws IOException {
		byte[] bytes = value.getBytes(charSet);
		byte[] lenBytes = int2Bytes(bytes.length);
		out.write(lenBytes);
		out.write(bytes);
	}

	public void writeLong(long v) throws IOException {
		byte[] result = long2Bytes(v);
		out.write(result);
	}

	private static final String charSet = "UTF-8";

	private String bytes2String(byte[] readBytesByLen) {
		try {
			return new String(readBytesByLen, charSet);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] readBytesByLen(int len) throws IOException, EOFException {
		byte[] bytes = new byte[len];
		int readSize;
		int off = 0;
		int readLen = len;
		while ((readSize = in.read(bytes, off, readLen)) != -1) {
			readLen -= readSize;
			if (readLen == 0) {
				break;
			}
			off += readSize;
		}
		if (readLen != 0) {
			throw new EOFException("len " + len);
		}
		return bytes;
	}
	

	
 
	
	public void close() {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void writeBytes(byte[] buf, int off, int len) throws IOException {
		out.write(buf, off, len);
	}

	public int readBytes(byte[] buf,int off, int len) throws IOException {
		return in.read(buf,off,len);
	}

	public void flush() throws IOException {
		out.flush();
	}

	

}
