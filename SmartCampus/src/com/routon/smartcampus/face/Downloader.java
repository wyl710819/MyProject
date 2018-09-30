package com.routon.smartcampus.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Downloader {  
    private String urlstr;// 下载的地址  
    private String localfile;// 保存路径  
    private int threadcount;// 线程数  
    private DownloadListener mListener;
//    private Handler mHandler;// 消息处理器  
    private Dao dao;// 工具类  
    private int fileSize;// 所要下载的文件的大小  
    private List<DownloadInfo> infos;// 存放下载信息类的集合  
    private static final int INIT = 1;//定义三种下载的状态：初始化状态，正在下载状态，暂停状态  
    private static final int DOWNLOADING = 2;  
    private static final int PAUSE = 3;  
    private int state = INIT;  
 
    public Downloader(String urlstr, String localfile, int threadcount,  
            Context context, DownloadListener mListener) {  
        this.urlstr = urlstr;  
        this.localfile = localfile;  
        this.threadcount = threadcount;  
        this.mListener = mListener;  
        dao = new Dao(context);  
    }  
    
    /**  
     *判断是否正在下载  
     */  
    public boolean isdownloading() {  
        return state == DOWNLOADING;  
    }  
    /**  
     * 得到downloader里的信息  
     * 首先进行判断是否是第一次下载，如果是第一次就要进行初始化，并将下载器的信息保存到数据库中  
     * 如果不是第一次下载，那就要从数据库中读出之前下载的信息（起始位置，结束为止，文件大小等），并将下载信息返回给下载器  
     */  
    public LoadInfo getDownloaderInfors() {  
    	if( fileSize == 0 ){
    		return null;
    	}
        if (isFirst(urlstr)) {  
            Log.d("TAG", "isFirst");   
            try { 
	            File file = new File(localfile);  
	            if (!file.exists()) {  
	                file.createNewFile();  
	                this.delete(urlstr);
	            }  
	    
	            if( threadcount > 1 ){
		            // 本地访问文件  
		            RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");  
		            accessFile.setLength(fileSize);  
		            accessFile.close(); 
	            }
            } catch (Exception e) {  
                e.printStackTrace();  
            }   
            int range = fileSize / threadcount;  
            infos = new ArrayList<DownloadInfo>();  
            for (int i = 0; i < threadcount - 1; i++) {  
                DownloadInfo info = new DownloadInfo(i, i * range, (i + 1)* range - 1, 0, urlstr);  
                infos.add(info);  
            }  
            DownloadInfo info = new DownloadInfo(threadcount - 1,(threadcount - 1) * range, fileSize - 1, 0, urlstr);  
            infos.add(info);  
            //保存infos中的数据到数据库  
            dao.saveInfos(infos);  
            //创建一个LoadInfo对象记载下载器的具体信息  
            LoadInfo loadInfo = new LoadInfo(fileSize, 0, urlstr);  
            return loadInfo;  
        } else {  
            //得到数据库中已有的urlstr的下载器的具体信息  
            infos = dao.getInfos(urlstr);  
            Log.d("TAG", "not isFirst size=" + infos.size());  
            int size = 0;  
            int compeleteSize = 0;  
            for (DownloadInfo info : infos) {  
                compeleteSize += info.getCompeleteSize();  
                size += info.getEndPos() - info.getStartPos() + 1;  
            }  
            return new LoadInfo(size, compeleteSize, urlstr);  
        }  
    }  
    
    public interface AsyncLisenter{
    	void getFileReady();
    	void getFileFailed();
    }
 
    /**  
     * 初始化  
     */  
    public void getAsyncFileSize(final AsyncLisenter lisenter) {  
        	 new Thread(){
        	        public void run(){  	  
        	        	 try {  
        	        		 Log.d("Downloader","getAsyncFileSize urlstr:"+urlstr);
        	                 URL url = new URL(urlstr);  
        	                 HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
        	                 connection.setConnectTimeout(5000);  
        	                 connection.setRequestMethod("GET");  
        	                 fileSize = connection.getContentLength();  
        	                 Log.d("Downloader","getAsyncFileSize fileSize:"+fileSize);
        	      
        	   
        	                 connection.disconnect();  
        	                 
        	                 if( lisenter != null && fileSize > 0 ){
        	                	 lisenter.getFileReady();
        	                 }else{
        	                	 lisenter.getFileFailed();
        	                 }
        	             } catch (Exception e) {  
        	                 e.printStackTrace();  
        	                 lisenter.getFileFailed();
        	             }      	             
        	        }
        	 }.start();
    }  
 
    /**  
     * 判断是否是第一次 下载  
     */  
    private boolean isFirst(String urlstr) { 
    	File file = new File(localfile);  
    	//文件不存在，返回false
        if (!file.exists()) {  
           return true;
        } 
        return dao.isHasInfors(urlstr);  
    }  
 
    /**  
     * 利用线程开始下载数据  
     */  
    public void download() { 
    	if( mListener != null ){
        	mListener.start();
        }
        if (infos != null) {  
            if (state == DOWNLOADING)  
                return;  
            state = DOWNLOADING;  
            for (DownloadInfo info : infos) {  
                new MyThread(info.getThreadId(), info.getStartPos(),  
                        info.getEndPos(), info.getCompeleteSize(),  
                        info.getUrl()).start();  
            }  
        }  
    }  
    
    public interface DownloadListener{
    	void start();

		void progressUpdate(int increaseLength,int totalSize);

		void complete();

		void cancel();
    }
 
    public class MyThread extends Thread {  
        private int threadId;  
        private int startPos;  
        private int endPos;  
        private int compeleteSize;  
        private String urlstr;

 
        public MyThread(int threadId, int startPos, int endPos,  
                int compeleteSize, String urlstr) {  
            this.threadId = threadId;  
            this.startPos = startPos;  
            this.endPos = endPos;  
            this.compeleteSize = compeleteSize;  
            this.urlstr = urlstr;  
        }  
        @Override  
        public void run() {  
            HttpURLConnection connection = null;  
            FileOutputStream fos = null;
            FileChannel fc = null;
            InputStream is = null;  
            try {  
                 
                URL url = new URL(urlstr);  
                connection = (HttpURLConnection) url.openConnection();  
                connection.setConnectTimeout(5000);  
                connection.setRequestMethod("GET");  
                // 设置范围，格式为Range：bytes x-y;  
                String range = "bytes="+(startPos + compeleteSize) + "-" + endPos;
                connection.setRequestProperty("Range", range); 
                MappedByteBuffer out = null;
                if( threadcount == 1 ){
                	//单线程时采用这种方式比较快
                	fos = new FileOutputStream(localfile,true);
                }else{
                    // 为了以可读可写的方式打开文件，这里使用RandomAccessFile来创建文件。  
                	fc = new RandomAccessFile(localfile, "rwd").getChannel(); 
                	out = fc.map(FileChannel.MapMode.READ_WRITE, startPos + compeleteSize,
                  		endPos+1-startPos-compeleteSize);  
                } 
                Log.i("RG", "connection---range>>>"+range);
                Log.i("RG", "connection--->>>"+connection);  
                // 将要下载的文件写到保存在保存路径下的文件中  
                is = connection.getInputStream();  
                byte[] buffer = new byte[4096];  
                int length = -1;  
                while ((length = is.read(buffer)) != -1) {  
                	if( fos != null ){
                		fos.write(buffer, 0, length); 
                	}
                	if( fc != null ){
                		out.put(buffer, 0, length);
                	}
                    compeleteSize += length;  
                    // 更新数据库中的下载信息  
                    dao.updataInfos(threadId, compeleteSize, urlstr);  
                   
                    // 用消息将下载信息传给进度条，对进度条进行更新  
                    if( mListener != null ){
                    	mListener.progressUpdate(length, fileSize);
                    }
                    if (state == PAUSE) {  
                    	if( mListener != null ){
                    		mListener.cancel();
                    	}
                        break;  
                    }  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            } finally {  
            	 
                try { 
                	if( dao != null ){
                		dao.closeDb();  
                	}
                	
                	if( fos != null ){
                		fos.close();
                	}
                	if( fc != null ){
                		fc.close();  
                    }
                	if( is != null ){
                		is.close();  
                	}
                	if( connection != null ){
                		connection.disconnect();  
                	}
                	
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
 
        }  
    }  
    //删除数据库中urlstr对应的下载器信息  
    public void delete(String urlstr) {  
        dao.delete(urlstr);  
    }  
    //设置暂停  
    public void pause() {  
        state = PAUSE;  
    }  
    //重置下载状态  
    public void reset() {  
       state = INIT;  
    }  
}  
