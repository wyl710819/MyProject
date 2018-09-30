package com.routon.inforelease;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.TimeData;

public class HttpClientDownloader {
	/** Callback interface for delivering parsed responses. */
    public interface NetworkListener{
        /** Called when a response is received. */
        public void onResponse(String result,boolean success);
    }

	private static final String TAG = "HttpClientDownloader";
	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	private volatile static HttpClientDownloader instance;
	public static final String COOKIE = "cookie";
	public static final String SESSION_ID = "jsessionid";
	public static final int TIMEOUT = 16000;
	
	private HashMap<String,String> mCookieMap = new HashMap<String,String>();
	private String mCookieStr = null;
	public String getCookie(){
		return mCookieStr;
	}
	
	public void setCookie(String cookie){
		mCookieStr = cookie;
	}
	
//	public void setUserInfo(UserInfo userinfo){
//		mUserInfo = userinfo;
//	}
	
	protected HttpClientDownloader() {
	}
	 
	 
	 /** Returns singleton class instance */
	public static HttpClientDownloader getInstance() {
		if (instance == null) {
			synchronized (HttpClientDownloader.class) {
				if (instance == null) {
					instance = new HttpClientDownloader();
				}
			}
		}
		return instance;
	}
	
	public HttpURLConnection submitPostData(URL url, List<NameValuePair> params, String encode) throws IOException{
		  UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, encode);
		  ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
		  entity.writeTo(b);
//		  Log.v(TAG, b.toString());
//		  byte[] data = s.getBytes();// getRequestData(params, encode).toString().getBytes();//获得请求体
		  
		  HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
          httpURLConnection.setConnectTimeout(TIMEOUT);     //设置连接超时时间
          httpURLConnection.setReadTimeout(TIMEOUT);
          httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
          httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
          httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
          httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
          //设置请求体的类型是文本类型
          httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //设置请求体的长度
          httpURLConnection.setRequestProperty("Content-Length", String.valueOf(entity.getContentLength())); //data.length));
          
          if (mCookieStr != null) {
        	  httpURLConnection.addRequestProperty("Cookie", mCookieStr);
          }
          //获得输出流，向服务器写入数据
          OutputStream outputStream = httpURLConnection.getOutputStream();
          //outputStream.write(data);
          entity.writeTo(outputStream);
          
          return httpURLConnection;
	}
	
	/*
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     */
    public static String submitPostDataAndGetAnswer(String strUrlPath,Map<String, String> params, String encode) {
        
        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        HttpURLConnection httpURLConnection = null;
        try {                       
            URL url = new URL(strUrlPath);  
             
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(TIMEOUT);     //设置连接超时时间
            httpURLConnection.setReadTimeout(TIMEOUT);
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            
            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return "err: " + e.getMessage().toString();
        }finally {
            if(httpURLConnection!=null){
            	httpURLConnection.disconnect();
            }
        }
        return "-1";
    }
    
    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
   public static StringBuffer getRequestData(Map<String, String> params, String encode) {
      StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
      try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                      .append("=")
                      .append(URLEncoder.encode(entry.getValue(), encode))
                      .append("&");
            }
           stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
           e.printStackTrace();
       }
       return stringBuffer;
    }
   
   //获取当前网络状态
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
    
   /*
    * Function  :   处理服务器的响应结果（将输入流转化成字符串）
    * Param     :   inputStream服务器的响应输入流
    */
   public static String dealResponseResult(InputStream inputStream) {
      String resultData = null;      //存储处理结果
       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int len = 0;
       try {
          while((len = inputStream.read(data)) != -1) {
             byteArrayOutputStream.write(data, 0, len);
          }
     } catch (IOException e) {
         e.printStackTrace();
        }
       resultData = new String(byteArrayOutputStream.toByteArray());    
       return resultData;
   }    
	
   public void getResultFromUrlWithSession(final String urlStr,final Handler handle,final int msgArg){
		executorService.submit(new Runnable() {
            public void run() {
            HttpURLConnection con = null;
           	 try {
//           		    Log.d("download","urlStr:"+urlStr);
                    URL url = new URL(urlStr);               
                    
                    con=(HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(TIMEOUT);
                    con.setReadTimeout(TIMEOUT);
                    //注意，把存在本地的cookie值加在请求头上
                    con.addRequestProperty("Cookie", mCookieStr);
                    String res = dealResponseResult(con.getInputStream());
                    Message msg = handle.obtainMessage();
                    msg.arg1 = msgArg;
                    msg.obj = res;
                    msg.arg2 = 0;
                    handle.sendMessage(msg);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                	 e.printStackTrace();
//                	 Log.d("download",e.getMessage());
//                	 Log.d("download",e.getClass().getName());
                	 //also tell the activity 
                	 Message msg = handle.obtainMessage();
                     msg.arg1 = msgArg;
                     msg.arg2 = 1;
                     handle.sendMessage(msg); 
                }finally {
                    if(con!=null){
                    	con.disconnect();
                    }
                }
            }
		});
		return;
	}
	
	/**
	 * 注销接口
	 * @param activity
	 * @param handle
	 * @param msgArg
	 */
	public void loginOut(final Handler handle,final int msgArg){
		getResultFromUrlWithSession(UrlUtils.getLogoutUrl(),handle,msgArg);
	}
	
	public void submitPostDataWithDefaultSessionAsync(String urlStr, Map<String, String> params, Handler handle, int msgArg){
		List<NameValuePair> p = new ArrayList<NameValuePair>();
		Set<Entry<String, String>> set = params.entrySet();
		Iterator<Entry<String, String>> it = set.iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			p.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		submitPostDataWithDefaultSessionAsync(urlStr, p, handle, msgArg);
	}
	
	/**
	 * 异步发送Post请求到服务器，并带默认session会话,并处理数据输出
	 */
	public void submitPostDataWithDefaultSessionAsync(final String urlStr,final List<NameValuePair> params,final Handler handle,final int msgArg){
		executorService.submit(new Runnable() {
            public void run() {
             HttpURLConnection con = null;
           	 try {
           		 	URL url = new URL(urlStr);
           		 	con = submitPostData(url,params,HTTP.UTF_8);
           		 	con.setConnectTimeout(TIMEOUT);
           		 	con.setReadTimeout(TIMEOUT);
           		 	 
                    //处理应答数据                         
                    String res = dealResponseResult(con.getInputStream());          
                    
                    Message msg = handle.obtainMessage();
                    msg.arg1 = msgArg;
                    msg.obj = res;
                    handle.sendMessage(msg);             
	
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                	 e.printStackTrace();
                	 Message msg = handle.obtainMessage();
                     msg.arg1 = msgArg;
                     msg.obj = e.getMessage();
                     handle.sendMessage(msg); 
                }finally {
                    if(con!=null){
                    	con.disconnect();
                    }
                }
            }
		});
	}
	
	public static final int MSG_SUCCESS = 0;
	public static final int MSG_FAILED = 1;
	
	public void loginIn(final Activity activity,final String urlStr,final NetworkListener listener){
		executorService.submit(new Runnable() {
            public void run() {
           	 HttpURLConnection con = null;
           	 try {
	                    URL url = new URL(urlStr);
//	                    Log.d(TAG,"loginIn url:"+urlStr);
	                    con=(HttpURLConnection) url.openConnection();
	                    con.setConnectTimeout(TIMEOUT);
	                    con.setReadTimeout(TIMEOUT);
	                    con.setRequestMethod("GET");
	                    String cookie = mCookieMap.get(urlStr);
	                    if( cookie != null ){
	                    	con.addRequestProperty("Cookie", cookie);
	                    }
	                    //注意这里获取服务器返回的头部信息,获取JSESSIONID=XXXXXX的信息
	                    cookie = con.getHeaderField("set-cookie");
	                    if( cookie == null ){
	                    	mCookieStr = mCookieMap.get(urlStr);
	                    }else{
	                    	mCookieStr = cookie; 
	                    }
	                    mCookieMap.put(urlStr, mCookieStr);

	                    //处理应答数据                         
	                    final String res = dealResponseResult(con.getInputStream());
	                    Log.d(TAG,"loginIn cookie:"+mCookieStr+",urlStr:"+urlStr);
	                    activity.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								 if( listener != null ){
				                    	listener.onResponse(res, true);
				                    }
							}
	                    	
	                    });
	                   
	                } catch (Exception e) {
	                    // TODO Auto-generated catch block
	                	e.printStackTrace();
	                	activity.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								 if( listener != null ){
									 listener.onResponse(null, false);
				                    }
							}
	                    	
	                    });
	                }finally {
	                    if(con!=null){
	                    	con.disconnect();
	                    }
	                }
            }
		});
		return;
	}
	
	public void loginIn(final String urlStr,final Handler handle,final int msgArg){
		executorService.submit(new Runnable() {
             public void run() {
            	 HttpURLConnection con = null;
            	 try {
	                    URL url = new URL(urlStr);
//	                    Log.d(TAG,"loginIn url:"+urlStr);
	                    con =(HttpURLConnection) url.openConnection();
	                    con.setConnectTimeout(TIMEOUT);
	                    con.setReadTimeout(TIMEOUT);
	                    con.setRequestMethod("GET");
	                    String cookie = mCookieMap.get(urlStr);                 
	                    Log.d(TAG,"loginIn 222 cookie:"+mCookieStr+",urlStr:"+urlStr);
	                    if( cookie != null ){
	                    	con.addRequestProperty("Cookie", cookie);
	                    }
	                    //处理应答数据                         
	                    String res = dealResponseResult(con.getInputStream());    
	                    //注意这里获取服务器返回的头部信息,获取JSESSIONID=XXXXXX的信息
	                    cookie = con.getHeaderField("set-cookie");
	                    if( cookie == null ){
	                    	mCookieStr = mCookieMap.get(urlStr);
	                    }else{
	                    	mCookieStr = cookie; 
	                    }
	                    mCookieMap.put(urlStr, mCookieStr);
	                    Log.d(TAG,"loginIn 111 cookie:"+mCookieStr+",urlStr:"+urlStr);
	                    if( handle != null ){
		                    Message msg = handle.obtainMessage();
		                    msg.arg1 = msgArg;
		                    msg.arg2 = MSG_SUCCESS;
		                    msg.obj = res;
		                    handle.sendMessage(msg);   
	                    }
	
	                } catch (Exception e) {
	                    // TODO Auto-generated catch block
	                	e.printStackTrace();
	                	if( handle != null ){
		                	Message msg = handle.obtainMessage();
		                    msg.arg1 = msgArg;
		                    msg.arg2 = MSG_FAILED;
		                    handle.sendMessage(msg);   
	                	}
	                }finally {
	                    if(con!=null){
	                    	con.disconnect();
	                    }
	                }
             }
		});
		return;
	}
	 
	public void planDel(int id, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getPlanDelUrl();
//		Log.v(TAG, "planDel url: " + urlStr);
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", Integer.toString(id));
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);
	}
	
	public void findPlanAssign(int id, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getFindPlanAssignUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", Integer.toString(id));
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);
	}

	public void planEdit(int contractId, String name, List<String> resIds, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getPlanEditUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", Integer.toString(contractId)));
		params.add(new BasicNameValuePair("name", name));// + "_mobile");
		if (resIds != null) {
			for (String id : resIds) {
				params.add(new BasicNameValuePair("resIds", id));
			}
		}
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);
	}

	public void planAssign(int contractId, String groups, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getPlanAssignUrl();
//		Log.v(TAG, "planAssign id: " + contractId + " groups: " + groups);
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", Integer.toString(contractId));
		params.put("groupids", groups);
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);		
	}

	public void adParam(List<String> ids, List<PlanMaterialparamsBean> adParams, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getAdParamUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String id : ids) {
			params.add(new BasicNameValuePair("ids", id));
		}
		for (PlanMaterialparamsBean bean : adParams) {
			params.add(new BasicNameValuePair("paramIds", Integer.toString(bean.adParamId)));
			params.add(new BasicNameValuePair("paramValues", bean.adParamValue));
		}
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);				
	}

	public void findAdPeriods(String id, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getFindAdPeriodsUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", id));		
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);	
	}

	public void adPeriods(List<String> ids, List<TimeData> adPeriods, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getAdPeriodsUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String id : ids) {
			params.add(new BasicNameValuePair("ids", id));
		}
		for (TimeData bean : adPeriods) {
			params.add(new BasicNameValuePair("loops", Integer.toString(bean.repeat_time)));
			params.add(new BasicNameValuePair("max", Integer.toString(bean.max_repeat_time)));
			params.add(new BasicNameValuePair("sTimes", bean.start_time));
			params.add(new BasicNameValuePair("eTimes", bean.end_time));
		}
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);		
	}
	
	public void adDel(List<String> adIds, Handler handler, int msgArg) {
		String urlStr = UrlUtils.getAdDelUrl();
//		Log.v(TAG, "adDel: " + urlStr);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (String id : adIds) {
			params.add(new BasicNameValuePair("id", id));
		}
		submitPostDataWithDefaultSessionAsync(urlStr, params, handler, msgArg);
	}
}
