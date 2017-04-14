package zh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import zh.checksum.FileChecksum;
import zh.checksum.FileChecksumCrc32;
import zh.checksum.FileChecksumMd5;
import zh.checksum.FileChecksumSha1;


public class Transfer {
	private Config config;
	private LogUtils log;
	public Transfer(Config config,LogUtils log){
		this.config=config;
		this.log=log;
	}
	
	
	
	public void exitFile(Connection conn) throws IOException{
		conn.writeString(RESULT_EXIT);
		conn.flush();
	}
	public void clientSend(FileItem file,Connection conn) throws IOException{
		BufferedInputStream bf=null;
		String result=null;
		try {
			
			String path=file.getPath();
			bf=new BufferedInputStream(new FileInputStream(path));
			byte buf[]=new byte[1024*10];
			int readSize=0;
			
			conn.writeString(FILE_HEADER);
			conn.writeString(file.getName());
			conn.writeLong(file.getSize());
			
			
			
			FileChecksum cs[]=new FileChecksum[]{new FileChecksumCrc32(),new FileChecksumMd5(),new FileChecksumSha1() };
			while((readSize = bf.read(buf)) != -1){
				conn.writeBytes(buf,0,readSize);
				for(FileChecksum fc:cs){
					fc.update(buf, 0, readSize);
				}
			}
			for(FileChecksum fc:cs){
				conn.writeString(fc.digest());;
			}
			conn.flush();
			result=conn.readString();
		    log.log("发送文件完成:"+file.getName()+" "+(result.equals(RESULT_SUCCESS)?"成功":"失败"));
		} catch (IOException e) {
			throw e;
		}finally {
			if(bf!=null){
			   try {
				bf.close();
			} catch (IOException e) {
			
				e.printStackTrace();
			}
			}
			if(result!=null&&result.equals(RESULT_SUCCESS)){
				if(config.delAfterSuccess()){
					log.log("校验成功,删除源文件 "+file.getName());
					File fileDel=new File(file.getPath());
					fileDel.delete();
				}
			}
		}
    }
	public boolean serverRecive(Connection conn,String localDir) throws IOException{
		FileItem fileItem=new FileItem();
		BufferedOutputStream out =null;
		String header=conn.readString();
		if(header.equals(RESULT_EXIT)){
			log.log("客户端完成全部文件发送");
			return true;
		}
		if(!header.equals(FILE_HEADER)){
			 throw new ChecksumException(" 校验失败 文件头失败");
		}
		try {
			String name=conn.readString();
			fileItem.setName(name);
			
			int fileSize= (int) conn.readLong();
			fileItem.setSize(fileSize);
			log.log("接收文件:"+name+" 大小:"+fileSize);
			File outFile=new File(localDir,name); 
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			int bufSize=1024*10;
			byte buf[]=new byte[bufSize];
			int readSize=0;
			FileChecksum cs[]=new FileChecksum[]{new FileChecksumCrc32(),new FileChecksumMd5(),new FileChecksumSha1() };
			int fileRead=0;
			int needLen= fileSize-fileRead ;
			if(needLen>bufSize){
				needLen=bufSize;
			}
			while((readSize = conn.readBytes(buf,0,needLen)) != -1){
				fileRead+=readSize;
				needLen= fileSize-fileRead ;
				if(needLen>bufSize){
					needLen=bufSize;
				}
				
				out.write(buf,0,readSize);
				for(FileChecksum fc:cs){
					fc.update(buf, 0, readSize);
				}
				if(fileRead==fileSize){
					break;
				}else if(fileRead>fileSize){
					throw new RuntimeException();
				}
			}
			
			
			out.flush();
			for(FileChecksum fc:cs){
			  String expect=fc.digest();
			  String read=conn.readString();
			  if(!expect.equals(read)){
				  conn.writeString(RESULT_ERROR);
				  log.log("接收文件失败:"+name);
				  throw new ChecksumException(" 校验失败"+name+" ;"+ expect +" !="+read);
			  }
			}
			conn.writeString(RESULT_SUCCESS);
			conn.flush();
			log.log("接收文件成功:"+name);
		} catch (IOException e) {
		     throw e;
		}finally {
			if(out!=null){
			   try {
				   out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		}
		return false;
    }
	private static final String RESULT_SUCCESS="SUCCESS";
	private static final String RESULT_ERROR="ERROR";
	private static final String RESULT_EXIT="EXIT_7892_12367XS";
	private static final String FILE_HEADER="HEADER_36780DG";
	
	
}
