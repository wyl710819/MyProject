package com.routon.inforelease.plan.create;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.routon.widgets.Toast;

public class TextInfoAddActivity extends CustomTitleActivity{

	private static final String TAG = "TextInfoAdd";
	private EditText title;
	private EditText info;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = this;
		
		setContentView(R.layout.activity_add_text);
				
		title  = (EditText) findViewById(R.id.add_text_title);
		
		info = (EditText) findViewById(R.id.add_text_info);
		
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String name = df.format(new Date());
		title.setHint(name);	
		
        
        this.initTitleBar(R.string.add_text);
        this.setTitleNextImageBtnClickListener(R.drawable.ok, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(info.getText() != null){

					String textInfo = info.getText().toString();
					textInfo=textInfo.replaceAll("\r|\n", "");
					if(textInfo.length() != 0){
						
						String titleText;
						titleText = title.getText().toString();
						
						if(titleText.length() == 0){
							titleText = title.getHint().toString();
						}

						Log.i(TAG, "textTitle:"+ titleText + "  info:"+textInfo);
						sendText(titleText, textInfo);
						finish();
						return;
					}
					
				}
				
				Toast.makeText(mContext, R.string.no_text_error, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private String encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }
	
	private void sendText(String title, String info){
  
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");  
  
        String urlString = UrlUtils.getResourceAddTxtUrl();
        
        Map<String, String> params = new HashMap<String, String>();
		params.put("txtResourceName", title);
		params.put("txtFlytextContent", info);
        
        Log.i(TAG, "URL:"+urlString);
        urlString += "?"+encodeParameters(params, HTTP.UTF_8);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                        if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }
                        int code = response.optInt("code");
						if( code == 0){ //返回成功
							
							JSONObject resultObj = response.optJSONObject("obj");
							if( resultObj != null ){
								MaterialItem materialItem = new MaterialItem();
								materialItem.setId(resultObj.optInt("fileId"));
								materialItem.setContent(resultObj.optString("resName"));
								Log.i(TAG, "fileId:"+materialItem.getId() + "  resName:"+materialItem.getContent());
							}
							
						
						}else if( code == -2){
							InfoReleaseApplication.returnToLogin(TextInfoAddActivity.this);
						}else{//失败
							Log.e(TAG, response.optString("msg"));  
							Toast.makeText(mContext, response.optString("msg"), Toast.LENGTH_LONG).show();
							
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {  
                    	Log.e(TAG, "sorry,Error"); 
                    	Toast.makeText(mContext, "网络连接失败!", Toast.LENGTH_LONG).show();
                    	if (progressDialog.isShowing()&&progressDialog!=null) {  
                            progressDialog.dismiss();  
                        }  
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
}
