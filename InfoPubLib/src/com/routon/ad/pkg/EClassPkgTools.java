package com.routon.ad.pkg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.os.Message;
import android.util.Log;

import com.routon.ad.element.StringUtils;
import com.routon.ad.element.Teacher;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.ClassInfoListfilesBean;

public class EClassPkgTools extends BasePkgTools {
	private static final String TAG = "EClassPkgTools";
	
	private static final int ACTION_CACHE_ONLY = 0;
	private static final int ACTION_MAKE_ZIP = 1;
	
	File mPkgCacheDir;
	String mDestFilePath;
	private int mAction = ACTION_MAKE_ZIP;
	
	private TaskExecutor mExecutor;
	
	private List<String> mImageList = new ArrayList<String>();

	private List<ClassInfoListdatasBean> mClassDataList;
	
	public EClassPkgTools() {
		mExecutor = new TaskExecutor();
	}
			
	public void startMakePkg(List<ClassInfoListdatasBean> dataList, File cache_dir, String destFilePath) {
		mPkgCacheDir = cache_dir;
		mClassDataList = dataList;
		mDestFilePath = destFilePath;
		mAction = ACTION_MAKE_ZIP;

		mNextStep = 0;

		mImageList.clear();
		mProgress = 0;

		deleteDir(mPkgCacheDir);
		mPkgCacheDir.mkdirs();
		mkdir(mPkgCacheDir, "imgs");
		
		notifyProgress(1);
		
		mNextStep = 0;
		postDoNextStep();
	}
	
	private int mCacheProgressBase = 100;
	private int mCurrentClassIndex = 0;
	private String mClassDirName;
	
	public void startOfflineCache(List<ClassInfoListdatasBean> dataList, String destCacheDir) {
		mPkgCacheDir = new File(destCacheDir);
		mClassDataList = dataList;
		mDestFilePath = destCacheDir;
		mAction = ACTION_CACHE_ONLY;

		mNextStep = 0;

		mImageList.clear();
		mProgress = 0;

		mkdir(mPkgCacheDir);
		
		mClassDirName = getCurrentTime();
		mCurrentClassIndex = 0;
		if (dataList.size() > 0) {
			mCacheProgressBase = 100 / dataList.size();
		}
		
		mNextStep = 1;
		postNextStep();
	}
	
	private float mImageProgressBase = 1.0f;
	
	private File mCurrentClassDir;
	
	@Override
	protected void doNextStep() {
		switch (mNextStep) {
		case 1: {
			if (mAction == ACTION_CACHE_ONLY) {
				mCurrentClassDir = new File(mPkgCacheDir, mClassDirName + "_" + mCurrentClassIndex);
				mkdir(mCurrentClassDir);
				mkdir(mCurrentClassDir, "imgs");
			} else {
				mCurrentClassDir = mPkgCacheDir;
			}

			saveClassData();
			
			notifyProgress(mProgress + 1);
			
			postDoNextStep();
		}
			break;
			
		case 2:	{			
				int left = (100 - mProgress);
				if (mAction == ACTION_MAKE_ZIP)
					left /= 2;
				else
					left = mCacheProgressBase - 1;
				mImageProgressBase = left;
				if (mImageList.size() > 0) {
					mImageProgressBase = (float) left / mImageList.size();
				} else {
					notifyProgress(mProgress + left);
				}

				postDoNextStep();
		}
			break;
			
		case 3: {
			if (mImageList.size() > 0) {
				String url = mImageList.get(0);
				String name = StringUtils.getFileName(url);
				File pic_dir = new File(mCurrentClassDir, "imgs");
				String path = new File(pic_dir, name).getPath();
				FileGetTask task = new FileGetTask(url, path, url);
				task.setOnHttpGetTaskListener(mOnImageGetTaskListener);
				mExecutor.execute(task);
			} else {
				postDoNextStep();
			}
		}
			break;
			
		case 4:
			if (mAction == ACTION_CACHE_ONLY) {
				mCurrentClassIndex++;
				
				if (mCurrentClassIndex >= mClassDataList.size()) {
					notifyFinished(0);					
				} else {
					mNextStep = 1;
					postNextStep();
				}
				return;
			}
			Log.v(TAG, "start zip");
			mExecutor.execute(new ZipRunnable(mProgress, mPkgCacheDir.getAbsolutePath(), mDestFilePath));
			break;
			
		default:		
			break;
		}
	}
	
	private void saveClassData() {
		if (mAction == ACTION_CACHE_ONLY) {
			ClassInfoListdatasBean curClassInfo = mClassDataList.get(mCurrentClassIndex);
			writeClassData(curClassInfo, mCurrentClassDir);
			
			for (ClassInfoListfilesBean bean : curClassInfo.files) {
				mImageList.add(bean.content);
			}
		} else {
			List<Teacher> list = new ArrayList<Teacher>();
			int id = 100;
			for (ClassInfoListdatasBean data : mClassDataList) {
				for (ClassInfoListfilesBean bean : data.files) {
					Teacher t = new Teacher();
					t.id = id;
					t.title = data.title;
					t.subTitle1 = data.subtitle1;
					t.subTitle2 = data.subtitle2;
					t.desc = data.desc;
					t.photo = bean.content;
					t.type = data.type;
					id++;
					
					mImageList.add(bean.content);
					
					list.add(t);
				}
			}
			writeTeacher(list, mPkgCacheDir);
		}
	}
		
	private static final int MSG_GET_IMAGE = MSG_CUSTOM_BEGIN + 1;
	
	@Override
	protected void onHandleMessage(Message msg) {
		switch (msg.what) {
		case MSG_GET_IMAGE: {
				onGetImage((String) msg.obj);
				break;
			}
		}
	}
	
	private void onGetImage(String url) {
		Log.v(TAG, "onGetImage: " + url);
		
		mImageList.remove(0);
		
		Log.v(TAG, "img list left size: " + mImageList.size());
		
		notifyProgress((int) (mProgress + mImageProgressBase));
		
		postNextStep();
	}
	
	public static ClassInfoListdatasBean readClassData(File eclass_dir) {
		File path = new File(eclass_dir, "classdata.xml");
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
			NodeList nodelist = document.getElementsByTagName("class");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element e = (Element) nodelist.item(i);
				
				ClassInfoListdatasBean bean = new ClassInfoListdatasBean();
				bean.id = StringUtils.toInteger(e.getAttribute("id"), 0);
				bean.title = e.getAttribute("title");
				bean.subtitle1 = e.getAttribute("subTitle1");
				bean.subtitle2 = e.getAttribute("subTitle2");
				bean.desc = e.getAttribute("desc");
				bean.duration = e.getAttribute("duration");
				bean.type = StringUtils.toInteger(e.getAttribute("type"), 1);
				bean.name = eclass_dir.getName();
				
				bean.files = new ArrayList<ClassInfoListfilesBean>();
				NodeList filenodelist = e.getElementsByTagName("file");
				for (int j = 0; j < filenodelist.getLength(); j++) {
					Element elm_file = (Element) filenodelist.item(j);
					
					ClassInfoListfilesBean file = new ClassInfoListfilesBean();
					String name = StringUtils.getFileName(elm_file.getAttribute("url"));
					file.content = "file://" + eclass_dir.getAbsolutePath() + "/imgs/" + name;
					file.resid = StringUtils.toInteger(elm_file.getAttribute("resId"), 0);
					
					bean.files.add(file);
				}
				
				return bean;
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static boolean writeClassData(ClassInfoListdatasBean bean, File eclass_dir) {
		File path = new File(eclass_dir, "classdata.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);

			Element e = doc.createElement("class");
			setAttribute(e, "id", Integer.toString(bean.id));
			setAttribute(e, "title", bean.title);
			setAttribute(e, "subTitle1", bean.subtitle1);
			setAttribute(e, "subTitle2", bean.subtitle2);
			setAttribute(e, "desc", bean.desc);
			setAttribute(e, "duration", bean.duration);
			setAttribute(e, "type", Integer.toString(bean.type));
				
			Element elm_files = doc.createElement("files");
			e.appendChild(elm_files);
			for (ClassInfoListfilesBean file : bean.files) {
				Element elm_file = doc.createElement("file");
				//String name = StringUtils.getFileName(file.content);
				setAttribute(elm_file, "url", file.content);
				setAttribute(elm_file, "resId", Integer.toString(file.resid));
				
				elm_files.appendChild(elm_file);
			}
				
			root.appendChild(e);
		
			saveXml(doc, path);
			
			return true;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static void writeTeacher(List<Teacher> list, File eclass_dir) {
		File path = new File(eclass_dir, "teacher.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("teachers");
			doc.appendChild(root);
			Element elm = doc.createElement("teachers");
			root.appendChild(elm);

			for (Teacher t : list) {
				Element e = t.toElement(doc);
				elm.appendChild(e);
			}
		
			saveXml(doc, path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private HttpGetTask.OnHttpGetTaskListener mOnImageGetTaskListener = new HttpGetTask.OnHttpGetTaskListener() {
		
		@Override
		public void onTaskStarted(HttpGetTask task) {
			
		}
		
		@Override
		public void onTaskFinished(HttpGetTask task, int code) {
			Message msg = mHandler.obtainMessage(MSG_GET_IMAGE, 0, 0, task.getContext());
			mHandler.sendMessage(msg);
		}
	};
	
	public void cancel() {
		
	}
	
	/*
	public ClassInfoListdatasBean makeNewOfflineClassInfo(List<String> imageList, File classInfoCacheDir) {
		String name = getCurrentTime() + "-0";
		mCurrentClassDir = new File(classInfoCacheDir, name);
		mAction = ACTION_CACHE_ONLY;

		mNextStep = 0;

		mImageList.clear();
		mProgress = 0;

		mkdir(mCurrentClassDir);
		mkdir(mCurrentClassDir, "imgs");
		
		ClassInfoListdatasBean bean = new ClassInfoListdatasBean();
		bean.name = name;
		bean.duration = "10";
		bean.files = new ArrayList<ClassInfoListfilesBean>();
		for (String url : imageList) {
			ClassInfoListfilesBean fileBean = new ClassInfoListfilesBean();
			fileBean.content = url;
			
			mImageList.add(url);
			
			bean.files.add(fileBean);
		}
		writeClassData(bean, mCurrentClassDir);
		
		mClassDataList = new ArrayList<ClassInfoListdatasBean>();
		mClassDataList.add(bean);
		mCurrentClassIndex = 0;
		
		int left = 100;
		mImageProgressBase = left;
		if (mImageList.size() > 0) {
			mImageProgressBase = (float) left / mImageList.size();
		}

		mNextStep = 3;
		postNextStep();

		return bean;
	}
	*/
	
	public static ClassInfoListdatasBean makeNewOfflineClassInfo(int type) {
		String name = getCurrentTime() + "-0";
		
		ClassInfoListdatasBean bean = new ClassInfoListdatasBean();
		bean.name = name;
		bean.duration = "10";
		bean.type = type;
		
		return bean;
	}
	
//	public static ClassInfoListdatasBean updateImages(ClassInfoListdatasBean bean, List<String> imageList) {
//		bean.files = new ArrayList<ClassInfoListfilesBean>();
//		for (String url : imageList) {
//			ClassInfoListfilesBean fileBean = new ClassInfoListfilesBean();
//			fileBean.content = url;
//			
//			bean.files.add(fileBean);
//		}
//
//		return bean;
//	}
	
	public ClassInfoListdatasBean saveClassInfo(ClassInfoListdatasBean bean, File classInfoCacheDir) {
		mCurrentClassDir = new File(classInfoCacheDir, bean.name);
		mAction = ACTION_CACHE_ONLY;

		mNextStep = 0;

		mImageList.clear();
		mProgress = 0;

		mkdir(mCurrentClassDir);
		mkdir(mCurrentClassDir, "imgs");
		
		for (ClassInfoListfilesBean fileBean : bean.files) {
			String url = fileBean.content;
			String name = StringUtils.getFileName(url);
			
			File pic_dir = new File(mCurrentClassDir, "imgs");
			File file = new File(pic_dir, name);
			if (!file.exists()) {
				mImageList.add(url);
			}
		}
		writeClassData(bean, mCurrentClassDir);
		
		mClassDataList = new ArrayList<ClassInfoListdatasBean>();
		mClassDataList.add(bean);
		mCurrentClassIndex = 0;
		
		int left = 100;
		mImageProgressBase = left;
		if (mImageList.size() > 0) {
			mImageProgressBase = (float) left / mImageList.size();
		}

		mNextStep = 3;
		postNextStep();

		return bean;
	}
}
