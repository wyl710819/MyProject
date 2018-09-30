package com.routon.smartcampus.family;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.routon.widgets.Toast;

public class ColligateOpinionActivity extends CustomTitleActivity {
    private ProgressBar mLoadingProgress;
    private WebView webView;
    private String mstrLoginUrl = "http://172.16.41.212:8080/eMedia/easyad/cmd/client/query/class/getstudentremark.htm?studentId=";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colligate_opinion);

        StudentBean studentBean=(StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
        
		initTitleBar("综合评价");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
        
        webView = (WebView)findViewById(R.id.webView1);
        mLoadingProgress = (ProgressBar)findViewById(R.id.progressBarLoading);
        mLoadingProgress.setMax(100);
        
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);   
        webView.getSettings().setSupportZoom(true); //设置可以支持缩放
        webView.getSettings().setDefaultZoom(ZoomDensity.FAR); 
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        if (studentBean!=null) {
//        	webView.loadUrl("http://xp.wanlogin.com:8083/tmp/zhpj.html");
        	webView.loadUrl(SmartCampusUrlUtils.getStudentOpinionWebUrl(String.valueOf(studentBean.sid)));
		}else {
			Toast.makeText(this, "获取学生信息失败", Toast.LENGTH_SHORT).show();
		}
        
        
        // 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //设置加载进度条
                view.setWebChromeClient(new WebChromeClientProgress());
                return true;
            }

        });
    }
    
    private class WebChromeClientProgress extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mLoadingProgress != null) {
                mLoadingProgress.setProgress(progress);
                if (progress == 100) mLoadingProgress.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, progress);
        }
    }
    
    /**
     * 按键响应，在WebView中查看网页时，检查是否有可以前进的历史记录。
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
        {
            
            // 返回键退回
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up
        // to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
