package com.routon.ad.pkg;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.routon.ad.element.StringUtils;
import com.routon.inforelease.json.SNoticeListrowsBean;

public class SNoticePkgTools extends BasePkgTools {
	private static final String TAG = "EClassPkgTools";
	
	File mPkgCacheDir;
	String mDestFilePath;
	
	private TaskExecutor mExecutor;
	
	public SNoticePkgTools() {
		mExecutor = new TaskExecutor();
	}
	
	private List<String> mDataList;
		
	public void startMakePkg(List<String> dataList, File cache_dir, String destFilePath) {
		mPkgCacheDir = cache_dir;
		mDestFilePath = destFilePath;
		
		mNextStep = 0;
		mDataList = dataList;

		mProgress = 0;

		deleteDir(mPkgCacheDir);
		mkdir(mPkgCacheDir);
		
		notifyProgress(1);
		
		mNextStep = 1;
		doNextStep();
	}

	public void startOfflineCache(List<SNoticeListrowsBean> dataList, String destCacheDir) {
		mPkgCacheDir = new File(destCacheDir);
		mDestFilePath = destCacheDir;
		
		mNextStep = 0;

		mProgress = 0;

		mkdir(destCacheDir);
		
		notifyProgress(1);

		String name = getCurrentTime();
		int index = 0;
		for (SNoticeListrowsBean data : dataList) {
			File destDir = new File(destCacheDir, name + "-" + index);
			mkdir(destDir);
			writeSNotice(data, destDir);
			index++;
		}
		
		notifyFinished(0);
	}
	
	private static void writeSNotice(SNoticeListrowsBean data, File destDir) {
		File path = new File(destDir, "classdata.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);

			Element e = doc.createElement("snotice");
			setAttribute(e, "id", Integer.toString(data.id));
			e.setTextContent(data.notice);
			
			root.appendChild(e);
		
			saveXml(doc, path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static SNoticeListrowsBean readSNotice(File srcDir) {
		File path = new File(srcDir, "classdata.xml");
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
			NodeList nodelist = document.getElementsByTagName("snotice");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element e = (Element) nodelist.item(i);
				
				SNoticeListrowsBean bean = new SNoticeListrowsBean();
				bean.id = StringUtils.toInteger(e.getAttribute("id"), 0);
				bean.notice = e.getTextContent();
				bean.name = srcDir.getName();
				
				return bean;
			}
		} catch (Exception e) {
			
		}
		
		return null;
	}

	private float mImageProgressBase = 1.0f;
	
	@Override
	protected void doNextStep() {
		switch (mNextStep) {
		case 1: {			
			writeSNotice(mDataList, mPkgCacheDir);
			
			notifyProgress(mProgress + 1);
			
			mNextStep++;
			doNextStep();
		}
			break;
			
		case 2:
			Log.v(TAG, "start zip");
			mExecutor.execute(new ZipRunnable(mProgress, mPkgCacheDir.getAbsolutePath(), mDestFilePath));
			break;
			
		default:		
			break;
		}
	}
	
	private static void writeSNotice(List<String> list, File eclass_dir) {
		File path = new File(eclass_dir, "notice.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("notices");
			doc.appendChild(root);

			int id = 300;
			for (String s : list) {
				Element e = doc.createElement("notice");
				e.setAttribute("id", Integer.toString(id));
				e.setTextContent(s);
				
				root.appendChild(e);
				id++;
			}
		
			saveXml(doc, path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cancel() {
		
	}
	
	public static void startMakeNewOfflineSNotice(String text, File snoticeCacheDir) {
		String name = getCurrentTime() + "-0";

		mkdir(snoticeCacheDir, name);
		
		SNoticeListrowsBean data = new SNoticeListrowsBean();
		data.notice = text;
		File destDir = new File(snoticeCacheDir, name);
		writeSNotice(data, destDir);
	}
}
