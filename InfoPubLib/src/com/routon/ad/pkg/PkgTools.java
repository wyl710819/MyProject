package com.routon.ad.pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.Message;
import android.util.Log;

import com.routon.ad.element.Ad;
import com.routon.ad.element.Form;
import com.routon.ad.element.Image;
import com.routon.ad.element.Label;
import com.routon.ad.element.Period;
import com.routon.ad.element.Playbill;
import com.routon.ad.element.Playlist;
import com.routon.ad.element.StringUtils;
import com.routon.ad.element.Style;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.json.FindAdPeriodsBean;
import com.routon.inforelease.json.FindAdPeriodsBeanParser;
import com.routon.inforelease.json.FindAdPeriodsperiodsBean;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.PlanMaterialBean;
import com.routon.inforelease.json.PlanMaterialBeanParser;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.AdParams;
import com.routon.inforelease.util.TimeUtils;

public class PkgTools extends BasePkgTools {
	private static final String TAG = "PkgTools";
	
	private static final int ACTION_CACHE_ONLY = 0;
	private static final int ACTION_MAKE_ZIP = 1;
	
	private static String default_theme = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
"<root>\n" +
"<theme_policy>\n" +
"<theme name=\"Theme_offline\" url=\"http://172.16.51.80/theme/theme_offline.xml\" id=\"49\" period=\"00:00~24:00\" act=\"50:1:1\" rev=\"6\" Priority=\"1\" begindate=\"19700101\" enddate=\"20380402\">\n" +
"<event>\n" +
"<evt act=\"50:1:1:65535\" time=\"00002400;\" ></evt>\n" +
"</event>\n" +
"</theme>\n" +
"</theme_policy>\n" +
"</root>\n";
	
	File mPkgCacheDir;
	private String mDestFilePath;
	
	private TaskExecutor mExecutor;
	
	private List<String> mImageList = new ArrayList<String>();
	
	private int mAction = ACTION_MAKE_ZIP;
	
	private String subfix = "";
	public PkgTools() {
		mExecutor = new TaskExecutor();
		
		DateFormat df = new SimpleDateFormat("-yyyyMMdd-HHmmss");
		subfix = df.format(new Date());
	}
	
	private String getThemeName() {
		return "theme_offline" + subfix + ".xml";
	}
	
	private String getPicPlaybillName() {
		return "playbill_picture" + subfix + ".xml";
	}
	
	private String getTextPlaybillName() {
		return "playbill_text" + subfix + ".xml";
	}
	
	private String firstThemeName(File dir) {
		return BasePkgTools.firstFileName(dir, "theme_offline.*\\.xml", getThemeName());
	}
	
	private String firstPicPlaybillName(File dir) {
		return BasePkgTools.firstFileName(dir, "playbill_picture.*\\.xml", getPicPlaybillName());
	}
	
	private String firstTextPlaybillName(File dir) {
		return BasePkgTools.firstFileName(dir, "playbill_text.*\\.xml", getTextPlaybillName());
	}
	
	private int mContractId;
	private boolean mHasEClassPriviledge;
	
	public void startMakePkg(int contractId, File cacheDir, String destFilePath, boolean hasEClassPriviledge) {
		mNextStep = 0;
		mContractId = contractId;
		mHasEClassPriviledge = hasEClassPriviledge;
		mPkgCacheDir = cacheDir;
		mDestFilePath = destFilePath;
		mAction = ACTION_MAKE_ZIP;

		mImageList.clear();
//		mImageLoaded = false;
//		mTextLoaded = false;
		mProgress = 0;
		mTextPlaybill = null;
		mImagePlaybill = null;
		mAdIds = null;

		deleteDir(mPkgCacheDir);
		mkdir(mPkgCacheDir);
		mkdir(mPkgCacheDir, "picture");		
		mkdir(mPkgCacheDir, "video");		
		mkdir(mPkgCacheDir, "theme");
		mkdir(mPkgCacheDir, "playbill");
		
		writeThemePolicy(mPkgCacheDir);
		
		notifyProgress(1);
		
		mNextStep = 1;
		postNextStep();
	}
	
	public void startMakePkg(File ad_dir, String destFilePath, boolean hasEClassPriviledge) {
		mPkgCacheDir = ad_dir;
		mDestFilePath = destFilePath;
		mAction = ACTION_MAKE_ZIP;
		
		if (!hasEClassPriviledge){
			writeForm(true);
		}else {
			writeForm(false);
		}
			
		
		mNextStep = 6;
		postNextStep();
	}
	
	public void startOfflineCache(PlanListrowsBean bean, File adCacheDir) {
		mNextStep = 0;
		mContractId = bean.contractId;
		mAction = ACTION_CACHE_ONLY;
		
		String name = getCurrentTime();
		mPkgCacheDir = new File(adCacheDir, name);
		mDestFilePath = mPkgCacheDir.getAbsolutePath();

		mImageList.clear();
		mProgress = 0;
		mTextPlaybill = null;
		mImagePlaybill = null;
		mAdIds = null;

		mkdir(mPkgCacheDir);
		mkdir(mPkgCacheDir, "picture");		
		mkdir(mPkgCacheDir, "video");		
		mkdir(mPkgCacheDir, "theme");
		mkdir(mPkgCacheDir, "playbill");
		
		writePlanListrowsBean(bean, mPkgCacheDir);
		writeThemePolicy(mPkgCacheDir);
		
		notifyProgress(1);
		
		mNextStep = 1;
		postNextStep();
	}
	
	public static PlanListrowsBean readPlanListrowsBean(File ad_dir) {
		File path = new File(ad_dir, "plan_info.xml");
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
			NodeList nodelist = document.getElementsByTagName("plan");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element e = (Element) nodelist.item(i);
				
				PlanListrowsBean bean = new PlanListrowsBean();
				bean.contractId = StringUtils.toInteger(e.getAttribute("id"), 0);
				bean.contractName = e.getAttribute("name");
				bean.beginTime = e.getAttribute("beginTime");
				bean.endTime = e.getAttribute("endTime");
				bean.terminalIDs = e.getAttribute("terminalIDs");
				if( bean.terminalIDs != null && bean.terminalIDs.trim().isEmpty() == false ){
					bean.published = true;
				}
				bean.templateId=e.getAttribute("templateId");
				bean.name = ad_dir.getName();
				
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
	
	public static void writePlanListrowsBean(PlanListrowsBean bean, File ad_dir) {
		File path = new File(ad_dir, "plan_info.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);

			Element e = doc.createElement("plan");
			setAttribute(e, "id", Integer.toString(bean.contractId));
			setAttribute(e, "name", bean.contractName);
			setAttribute(e, "beginTime", bean.beginTime);
			setAttribute(e, "endTime", bean.endTime);
			setAttribute(e, "terminalIDs", bean.terminalIDs);
			//添加模板素材Id
			setAttribute(e, "templateId", bean.templateId);
						
			root.appendChild(e);
		
			saveXml(doc, path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeForm(boolean hasText) {
		File playbill_dir = new File(mPkgCacheDir, "playbill");
		Form form = new Form(106, 1440, 900);
		form.addAdElement(new Image(50, 0, 0, 1440, 900, "ftp://xxx/playbill/" + firstPicPlaybillName(playbill_dir)));
		if (hasText) {
			form.addAdElement(new Label(51, 0, 828, 1440, 72, "ftp://xxx/playbill/" + firstTextPlaybillName(playbill_dir)));
		}
		writeTheme(form, mPkgCacheDir);
	}
	
	private float mImageProgressBase = 1.0f;
	
	@Override
	protected void doNextStep() {
		switch (mNextStep) {
		case 1:
			HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getTextListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_TEXT_LIST);
			break;
			
		case 2:
			
			if (mAdIds != null && mAdIds.size() > 0) {
				HttpClientDownloader.getInstance().findAdPeriods(mAdIds.get(0), mHandler, MSG_FIND_AD_PERIODS);
			} else {
				writePlaybill(mTextPlaybill, mPkgCacheDir, getTextPlaybillName());
				Log.v(TAG, "write playbill_text");

				postDoNextStep();
			}
			break;
			
		case 3:
			HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getImageListUrl(null, 1, 100, mContractId), mHandler, MSG_GET_IMAGE_LIST);
			break;
			
		case 4:
			if (mAdIds != null && mAdIds.size() > 0) {
				HttpClientDownloader.getInstance().findAdPeriods(mAdIds.get(0), mHandler, MSG_FIND_AD_PERIODS);
			} else {
				writePlaybill(mImagePlaybill, mPkgCacheDir, getPicPlaybillName());
				Log.v(TAG, "write playbill_picture");
				
				int left = (100 - mProgress);
				if (mAction == ACTION_MAKE_ZIP)
					left /= 2;
				mImageProgressBase = left;
				if (mImageList.size() > 0) {
					mImageProgressBase = (float) left / mImageList.size();
				} else {
					notifyProgress(mProgress + left);
				}

				postDoNextStep();
			}
			break;
			
		case 5: {
			if (mImageList.size() > 0) {
				String url = mImageList.get(0);
				String name = StringUtils.getFileName(url);
				File pic_dir = new File(mPkgCacheDir, "picture");
				File file = new File(pic_dir, name);
				if (file.exists()) {
					mImageList.remove(0);
					Log.v(TAG, "image exist, img list left size: " + mImageList.size());
					notifyProgress((int) (mProgress + mImageProgressBase));
					postNextStep();
					return;
				}
				String path = file.getPath();
				FileGetTask task = new FileGetTask(url, path, url);
				task.setOnHttpGetTaskListener(mOnImageGetTaskListener);
				mExecutor.execute(task);
			} else {
				postDoNextStep();
			}
		}
			break;
			
		case 6:
			if (mAction == ACTION_CACHE_ONLY) {
				notifyFinished(0);
				return;
			}
			Log.v(TAG, "start zip");
			mExecutor.execute(new ZipRunnable(mProgress, mPkgCacheDir.getAbsolutePath(), mDestFilePath));
			break;
			
		default:		
			break;
		}
	}
		
	private static final int MSG_GET_IMAGE_LIST = MSG_CUSTOM_BEGIN + 1;
	private static final int MSG_GET_TEXT_LIST = MSG_CUSTOM_BEGIN + 2;
	private static final int MSG_GET_IMAGE = MSG_CUSTOM_BEGIN + 3;
	private static final int MSG_FIND_AD_PERIODS = MSG_CUSTOM_BEGIN + 4;
	
	@Override
	protected void onHandleMessage(Message msg) {
		switch (msg.what) {
		case 0: {
			switch (msg.arg1) {
			case MSG_GET_IMAGE_LIST:
				onGetImageList((String) msg.obj);
				break;
	
			case MSG_GET_TEXT_LIST:
				onGetTextList((String) msg.obj);
				break;
				
			case MSG_GET_IMAGE:
				onGetImage((String) msg.obj);
				break;
				
			case MSG_FIND_AD_PERIODS:
				onFindAdPeriods((String) msg.obj);
				break;
			}
		}
		break;
		}
	}
	
	private Playbill mTextPlaybill;
	private List<String> mAdIds;
	
	private void onGetTextList(String text) {
		if (text == null) {
			notifyFinished(1);
			return;
		}
		
		PlanMaterialBean bean = PlanMaterialBeanParser.parsePlanMaterialBean(text);
		if (bean == null) {
			notifyFinished(1);
			return;
		}
		
		if (bean.rows == null) {
			notifyFinished(1);
			return;
		}
		
		Playbill pb = new Playbill();
		mTextPlaybill = pb;
		mAdIds = new ArrayList<String>();
		int orderno = 300;
		for (PlanMaterialrowsBean row : bean.rows) {
			Playlist pl = new Playlist();
			pl.orderno = orderno++;
			pb.addPlaylist(pl);
			Ad ad = new Ad();
			ad.id = row.adId;
			ad.url = row.thumbnail;
			
			mAdIds.add(Integer.toString(ad.id));
						
			if (row.params != null) {
				ad.style = new Style();
				for (PlanMaterialparamsBean param : row.params) {
					switch (param.adParamId) {
					case AdParams.BEGIN_TIME:
						pl.datebegin = TimeUtils.getDate(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
						break;
						
					case AdParams.END_TIME:
						pl.dateend = TimeUtils.getDate(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
						break;
						
					case AdParams.TEXT_COLOR:
						ad.style.color = param.adParamValue;
						break;
						
					case AdParams.TEXT_BG_COLOR:
						ad.style.bgcolor = param.adParamValue;
						break;
						
					case AdParams.TEXT_BG_ALPHA:
						int alpha = StringUtils.toInteger(param.adParamValue, 0);
						ad.style.alpha = Integer.toHexString(alpha);
						break;
					}
				}
			} 
			
			pl.addAd(ad);
		}
		
//		mTextLoaded = true;
//		writePlaybill(pb, mAdDir, getTextPlaybillName());
//		Log.v(TAG, "write playbill_text");
		
		notifyProgress(mProgress + 1);
		
//		doIfDownloadFinished();
		
		boolean hasText = true;
//		Log.d("PKgTools","mHasEClassPriviledge:"+mHasEClassPriviledge);
		if (mHasEClassPriviledge) {
			hasText = false;
		} else {
//			Log.d("PKgTools","mAdIds:"+mAdIds);
			if (mAdIds != null && mAdIds.size() > 0) {
//				Log.d("PKgTools","mAdIds.size():"+mAdIds.size());
				hasText = true;
			} else {
				hasText = false;
			}
		}
		Log.d("PKgTools","hasText:"+hasText);
		writeForm(hasText);
		
		postDoNextStep();
	}
	
	private Playbill mImagePlaybill;
	
	private void onGetImageList(String text) {
		if (text == null) {
			notifyFinished(1);
			return;
		}
		
		PlanMaterialBean bean = PlanMaterialBeanParser.parsePlanMaterialBean(text);
		if (bean == null) {
			notifyFinished(1);
			return;
		}
		
		if (bean.rows == null) {
			notifyFinished(1);
			return;
		}
		
		Playbill pb = new Playbill();
		mImagePlaybill = pb;
		mAdIds = new ArrayList<String>();
		int orderno = 100;
		for (PlanMaterialrowsBean row : bean.rows) {
			Playlist pl = new Playlist();
			pl.orderno = orderno++;
			pb.addPlaylist(pl);
			Ad ad = new Ad();
			ad.id = row.adId;
			String name = StringUtils.getFileName(row.thumbnail);
			// 直接使用row.thnumbnail作为url离线发布到终端后，终端计算出的图片路径不对，导致找不到图片，所以在此将url重新改一下
			ad.url = "ftp://xxx/picture/" + name;
			
			mAdIds.add(Integer.toString(ad.id));			
			mImageList.add(row.thumbnail);
			
//			File pic_dir = new File(mAdDir, "picture");
//			String path = new File(pic_dir, name).getPath();
//			FileGetTask task = new FileGetTask(row.thumbnail, path, row.thumbnail);
//			task.setOnHttpGetTaskListener(mOnImageGetTaskListener);
//			mExecutor.execute(task);
			
			if (row.params != null) {
				ad.style = new Style();
				for (PlanMaterialparamsBean param : row.params) {
					switch (param.adParamId) {
					case AdParams.BEGIN_TIME:
						pl.datebegin = TimeUtils.getDate(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
						break;
						
					case AdParams.END_TIME:
						pl.dateend = TimeUtils.getDate(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
						break;
						
					case AdParams.ELAPSE_TIME:
						ad.length = param.adParamValue;
						break;
						
					case AdParams.IMAGE_EFFECT:
						ad.style.effect = param.adParamValue;
						break;
					}
				}
			} 
			
			pl.addAd(ad);
		}
		
//		mImageLoaded = true;
//		writePlaybill(pb, mAdDir, getPicPlaybillName());
//		Log.v(TAG, "write playbill_picture");
		
		notifyProgress(mProgress + 1);
		
		postDoNextStep();
	}

	private void updatePeriods(Playbill playbill, int id, List<FindAdPeriodsperiodsBean> periods) {
		Log.v(TAG, "updatePeriods: " + id);
		for (FindAdPeriodsperiodsBean bean : periods) {
			Log.v(TAG, "period: " + bean.adId + " maxloop: " + bean.max + " begin: " + bean.beginTime);
		}
		List<Playlist> playlist = playbill.getPlaylist();
		for (Playlist pl : playlist) {
			List<Ad> adlist = pl.getAdList();
			for (Ad ad : adlist) {
				if (ad.id == id && periods != null && periods.size() > 0) {
					ad.periods = new ArrayList<Period>();
					for (FindAdPeriodsperiodsBean bean : periods) {
						ad.periods.add(new Period(bean.loops, bean.max, TimeUtils.getTime(bean.beginTime, TimeUtils.FORMAT_HHmmss), TimeUtils.getTime(bean.endTime, TimeUtils.FORMAT_HHmmss)));
					}
				}
			}
		}
	}
	
	private void onFindAdPeriods(String text) {
		if (text == null) {
			notifyFinished(1);
			return;
		}

		FindAdPeriodsBean bean = FindAdPeriodsBeanParser.parseFindAdPeriodsBean(text);
		if (bean == null) {
			notifyFinished(1);
			return;
		}
		
		if (bean.code == 1) {
			String id = mAdIds.remove(0);
			if (mNextStep == 2) {
				updatePeriods(mTextPlaybill, StringUtils.toInteger(id, 0), bean.periods);
			} else if (mNextStep == 4) {
				updatePeriods(mImagePlaybill, StringUtils.toInteger(id, 0), bean.periods);
			}
			
			notifyProgress(mProgress + 1);
			postNextStep();
		} else {
			notifyFinished(1);
			return;
		}
	}
	
	private void onGetImage(String url) {
		Log.v(TAG, "onGetImage: " + url);
		
		mImageList.remove(0);
		
		Log.v(TAG, "img list left size: " + mImageList.size());
		
		notifyProgress((int) (mProgress + mImageProgressBase));
		
		postNextStep();
	}
	
	private void writeThemePolicy(File ad_dir) {
		File theme_policy = new File(ad_dir, "theme_policy.xml");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(theme_policy);
			String theme = default_theme.replace("theme_offline.xml", firstThemeName(new File(ad_dir, "theme")));
			fos.write(theme.getBytes());			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeTheme(Form form, File ad_dir) {
		File theme_dir = new File(ad_dir, "theme");
		File theme_path = new File(theme_dir, firstThemeName(theme_dir));
		saveXml(form.toXml(), theme_path);
	}
	
	private static void writePlaybill(Playbill playbill, File ad_dir, String name) {
		File playbill_dir = new File(ad_dir, "playbill");
		File playbill_path = new File(playbill_dir, name);
		saveXml(playbill.toXml(), playbill_path);
	}
	
	private HttpGetTask.OnHttpGetTaskListener mOnImageGetTaskListener = new HttpGetTask.OnHttpGetTaskListener() {
		
		@Override
		public void onTaskStarted(HttpGetTask task) {
			
		}
		
		@Override
		public void onTaskFinished(HttpGetTask task, int code) {
			Message msg = mHandler.obtainMessage(0, MSG_GET_IMAGE, 0, task.getContext());
			mHandler.sendMessage(msg);
		}
	};
	
	public void cancel() {
		
	}

	/*
	 * type: 0 - playbill_picture.xml    1 - playbill_text.xml
	 */
	public static boolean writePlanMaterial(PlanMaterialBean bean, File material_file, int type) {
		Playbill pb = new Playbill();
		int orderno = 100;
		for (PlanMaterialrowsBean row : bean.rows) {
			Playlist pl = new Playlist();
			pl.orderno = orderno++;
			pb.addPlaylist(pl);
			Ad ad = new Ad();
			ad.id = row.adId;
			if (type == 0) {
				String name = StringUtils.getFileName(row.thumbnail);
				// 直接使用row.thnumbnail作为url离线发布到终端后，终端计算出的图片路径不对，导致找不到图片，所以在此将url重新改一下
				ad.url = "ftp://xxx/picture/" + name;				
			} else {
				ad.url = row.thumbnail;
			}
			
			if (row.params != null) {
				ad.style = new Style();
				for (PlanMaterialparamsBean param : row.params) {
					switch (param.adParamId) {
					case AdParams.BEGIN_TIME:
						pl.datebegin = TimeUtils.getFormatCalendarStr(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
					//	Log.d("pkgtool","pl.datebegin:"+pl.datebegin);
						break;
						
					case AdParams.END_TIME:
						pl.dateend = TimeUtils.getFormatCalendarStr(param.adParamValue, TimeUtils.FORMAT_yyyyMMdd);
						break;
						
					case AdParams.ELAPSE_TIME:
						ad.length = param.adParamValue;
						break;
						
					case AdParams.IMAGE_EFFECT:
						ad.style.effect = param.adParamValue;
						break;
						
					case AdParams.TEXT_COLOR:
						ad.style.color = param.adParamValue;
						break;
						
					case AdParams.TEXT_BG_COLOR:
						ad.style.bgcolor = param.adParamValue;
						break;
						
					case AdParams.TEXT_BG_ALPHA:
						int alpha = StringUtils.toInteger(param.adParamValue, 0);
						ad.style.alpha = Integer.toHexString(alpha);
						break;
					}
				}
			} 
			
			if (row.periods != null) {
				ad.periods = new ArrayList<Period>();
				for (FindAdPeriodsperiodsBean peroid_bean : row.periods) {
					ad.periods.add(new Period(peroid_bean.loops, peroid_bean.max, 
							TimeUtils.getTime(peroid_bean.beginTime, TimeUtils.FORMAT_HHmmss), TimeUtils.getTime(peroid_bean.endTime, TimeUtils.FORMAT_HHmmss)));
				}
			}
			
			pl.addAd(ad);
		}
		saveXml(pb.toXml(), material_file);
		
		return false;
	}

	/*
	 * dirPath : the value is null while read playbill_text, or is directory path while read playbill_picture.xml 
	 */
	public static PlanMaterialBean readPlanMaterial(File material_file, String dirPath) {		
		PlanMaterialBean bean = new PlanMaterialBean();
		try {
			bean.rows = new ArrayList<PlanMaterialrowsBean>();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(material_file);
			NodeList nodelist = document.getElementsByTagName("playlist");
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element e = (Element) nodelist.item(i);
				
				PlanMaterialrowsBean rowsbean = parsePlaylistNode(e, dirPath);
				bean.rows.add(rowsbean);
			}
			bean.total = bean.rows.size();
			return bean;
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
		
		return bean;
	}

	private static PlanMaterialrowsBean parsePlaylistNode(Element e, String dirPath) {
		PlanMaterialrowsBean bean = new PlanMaterialrowsBean();
		NodeList nodelist = e.getElementsByTagName("ad");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Element ad_elm = (Element) nodelist.item(i);
			parseAdNode(ad_elm, bean, e.getAttribute("datebegin"), e.getAttribute("dateend"), dirPath);
		}
		return bean;
	}

	private static void parseAdNode(Element ad_elm, PlanMaterialrowsBean bean, String beginTime, String endTime, String dirPath) {
		bean.params = new ArrayList<PlanMaterialparamsBean>();
		bean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, TimeUtils.convertDate(beginTime, TimeUtils.FORMAT_yyyyMMdd, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss)));
		bean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, TimeUtils.convertDate(endTime, TimeUtils.FORMAT_yyyyMMdd, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss)));
		NodeList nodelist = ad_elm.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
//				Element elm = (Element) node;
				if (node.getNodeName().equals("id")) {
					bean.adId = StringUtils.toInteger(node.getTextContent(), 0);
				} else if (node.getNodeName().equals("url")) {
					if (dirPath != null) {
						String name = StringUtils.getFileName(node.getTextContent());
						bean.thumbnail = "file://" + dirPath + "/picture/"+ name;
					} else {
						bean.thumbnail = node.getTextContent();
					}
				} else if (node.getNodeName().equals("name")) {
					bean.name = node.getTextContent();
				} else if (node.getNodeName().equals("length")) {
					bean.params.add(new PlanMaterialparamsBean(AdParams.ELAPSE_TIME, node.getTextContent()));
				} else if (node.getNodeName().equals("style")) {
					parseStyleNode(node, bean);
				} else if (node.getNodeName().equals("periods")) {
					parsePeriodsNode(node, bean);
				}
			}
		}
	}

	private static void parsePeriodsNode(Node parent, PlanMaterialrowsBean bean) {
		bean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
		
		NodeList nodelist = parent.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element elm = (Element) node;
				if (node.getNodeName().equals("p")) {
					FindAdPeriodsperiodsBean period = new FindAdPeriodsperiodsBean();
					period.loops = StringUtils.toInteger(elm.getAttribute("loop"), 1);
					period.max = StringUtils.toInteger(elm.getTextContent(), 100);
					String time = elm.getAttribute("time");
					if (time.length() == 12) {
						period.beginTime = TimeUtils.convertDate(time.substring(0, 2) + ":" + time.substring(4, 6) + ":" + time.substring(8, 10),
								TimeUtils.FORMAT_HH_mm_ss, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
						period.endTime = TimeUtils.convertDate(time.substring(2, 4) + ":" + time.substring(6, 8) + ":" + time.substring(10, 12),
								TimeUtils.FORMAT_HH_mm_ss, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
					}
					bean.periods.add(period);
				}
			}
		}
	}

	private static void parseStyleNode(Node parent, PlanMaterialrowsBean bean) {
		NodeList nodelist = parent.getChildNodes();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equals("color")) {
					bean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_COLOR, node.getTextContent()));
				} else if (node.getNodeName().equals("bgcolor")) {
					bean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_COLOR, node.getTextContent()));
				} else if (node.getNodeName().equals("alpha")) {
					String v = "80";
					try {
						int value = Integer.parseInt(node.getTextContent(), 16);
						v = Integer.toString(value);
					} catch (Exception e) {						
					}
					bean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_ALPHA, v));
				} else if (node.getNodeName().equals("effect")) {
					bean.params.add(new PlanMaterialparamsBean(AdParams.IMAGE_EFFECT, node.getTextContent()));
				}
			}
		}
	}

	public PlanListrowsBean makeNewOfflinePlan(List<String> imageList, List<String> textList, File adCacheDir) {
		mAction = ACTION_CACHE_ONLY;

		String name = getCurrentTime();
		mPkgCacheDir = new File(adCacheDir, name);
		mDestFilePath = mPkgCacheDir.getAbsolutePath();
		mkdir(mPkgCacheDir);
		mkdir(mPkgCacheDir, "picture");		
		mkdir(mPkgCacheDir, "video");		
		mkdir(mPkgCacheDir, "theme");
		mkdir(mPkgCacheDir, "playbill");
		
		writeThemePolicy(mPkgCacheDir);
		boolean hasText = textList != null && textList.size() > 0 ? true : false;
		writeForm(hasText);
		
		PlanListrowsBean bean = new PlanListrowsBean();
		bean.name = name;
		bean.contractName = name;
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		bean.beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		bean.endTime = sdf.format(calendar.getTime());		
		writePlanListrowsBean(bean, mPkgCacheDir);
		
		File playbill_dir = new File(mPkgCacheDir, "playbill");
		if (imageList != null) {
			PlanMaterialBean imageMaterial = new PlanMaterialBean();
			imageMaterial.total = imageList.size();
			imageMaterial.rows = new ArrayList<PlanMaterialrowsBean>();
			for (String url : imageList) {
				PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
				rowsBean.thumbnail = url;
				
				Log.v(TAG, "makeNewOfflinePlan url: " + url);
				mImageList.add(url);
				
				rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, bean.beginTime));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, bean.endTime));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.ELAPSE_TIME, "30"));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.IMAGE_EFFECT, "1"));
				
				rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
				FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
				default_period.beginTime = "2017-01-01 00:00:00";
				default_period.endTime = "2038-01-01 00:00:00";
				default_period.loops = 1;
				default_period.max = 65535;
				rowsBean.periods.add(default_period);
				
				imageMaterial.rows.add(rowsBean);
			}
			File material_file = new File(playbill_dir, getPicPlaybillName());
			writePlanMaterial(imageMaterial, material_file, 0);
		}

		if (textList != null) {
			PlanMaterialBean textMaterial = new PlanMaterialBean();
			textMaterial.total = textList.size();
			textMaterial.rows = new ArrayList<PlanMaterialrowsBean>();
			for (String url : textList) {
				PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
				rowsBean.thumbnail = url;
				rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, bean.beginTime));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, bean.endTime));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_COLOR, "#042398"));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_COLOR, "#ffffff"));
				rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_ALPHA, "80"));
				
				rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
				FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
				default_period.beginTime = "2017-01-01 00:00:00";
				default_period.endTime = "2038-01-01 00:00:00";
				default_period.loops = 1;
				default_period.max = 65535;
				rowsBean.periods.add(default_period);
				
				textMaterial.rows.add(rowsBean);
			}
			File material_file = new File(playbill_dir, getTextPlaybillName());
			writePlanMaterial(textMaterial, material_file, 1);
		}
		
		mProgress = 0;
		int left = (100 - mProgress);
		mImageProgressBase = left;
		if (mImageList.size() > 0) {
			mImageProgressBase = (float) left / mImageList.size();
		} else {
			notifyProgress(mProgress + left);
		}

		mNextStep = 5;
		postNextStep();

		return bean;
	}

	public void addOfflinePlanMaterial(PlanMaterialBean materialBean, List<String> materialList, int type, File ad_dir) {
		mAction = ACTION_CACHE_ONLY;
		mPkgCacheDir = ad_dir;
		
		mImageList.clear();
		
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		String beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		String endTime = sdf.format(calendar.getTime());		
		
		File playbill_dir = new File(mPkgCacheDir, "playbill");
		if (type == 0) {
			if (materialList != null) {
				if (materialBean.rows == null)
					materialBean.rows = new ArrayList<PlanMaterialrowsBean>();
				for (String url : materialList) {
					PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
					rowsBean.thumbnail = url;
					
					Log.v(TAG, "makeNewOfflinePlan url: " + url);
					mImageList.add(url);
					
					rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, beginTime));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, endTime));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.ELAPSE_TIME, "30"));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.IMAGE_EFFECT, "1"));
					
					rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
					FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
					default_period.beginTime = "2017-01-01 00:00:00";
					default_period.endTime = "2038-01-01 00:00:00";
					default_period.loops = 1;
					default_period.max = 65535;
					rowsBean.periods.add(default_period);
					
					materialBean.rows.add(rowsBean);
				}
				materialBean.total = materialBean.rows.size();
	
				File material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_picture.*\\.xml", getPicPlaybillName()));
				writePlanMaterial(materialBean, material_file, 0);
			}
		} else {
			if (materialList != null) {
				PlanMaterialBean textMaterial = materialBean;
				if (materialBean.rows == null)
					textMaterial.rows = new ArrayList<PlanMaterialrowsBean>();
				for (String url : materialList) {
					PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
					rowsBean.thumbnail = url;
					rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, beginTime));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, endTime));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_COLOR, "#042398"));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_COLOR, "#ffffff"));
					rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_ALPHA, "80"));
					
					rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
					FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
					default_period.beginTime = "2017-01-01 00:00:00";
					default_period.endTime = "2038-01-01 00:00:00";
					default_period.loops = 1;
					default_period.max = 65535;
					rowsBean.periods.add(default_period);
					
					textMaterial.rows.add(rowsBean);
				}
				textMaterial.total = materialBean.rows.size();

				File material_file = new File(playbill_dir, BasePkgTools.firstFileName(playbill_dir, "playbill_text.*\\.xml", getTextPlaybillName()));
				writePlanMaterial(textMaterial, material_file, 1);
			}
		}
		
		mProgress = 0;
		int left = (100 - mProgress);
		mImageProgressBase = left;
		if (mImageList.size() > 0) {
			mImageProgressBase = (float) left / mImageList.size();
		} else {
			notifyProgress(mProgress + left);
		}

		mNextStep = 5;
		postNextStep();
	}

	public static PlanMaterialrowsBean makeTextAd(String text,boolean isOffline) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;
		if(isOffline)
			sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		else
			sdf=new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);	
		String beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		String endTime = sdf.format(calendar.getTime());		

		PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
		rowsBean.thumbnail = text;
		rowsBean.modify =  Calendar.getInstance().getTimeInMillis();
		rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, beginTime));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, endTime));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_COLOR, "#042398"));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_COLOR, "#ffffff"));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_ALPHA, "80"));
		
		rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
		FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
		if(isOffline)
		{
			default_period.beginTime = "2017-01-01 00:00:00";
			default_period.endTime = "2038-01-01 00:00:00";
		}
		else {
			default_period.beginTime = "00:00";
			default_period.endTime = "00:00";
		}
		default_period.loops = 1;
		default_period.max = 65535;
		rowsBean.periods.add(default_period);
		
		return rowsBean;
	}
	

	public static PlanMaterialrowsBean makeImageAd(String url,boolean isOffline) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;
		if(isOffline)
			sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		else
			sdf=new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);	
		String beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		String endTime = sdf.format(calendar.getTime());		

		PlanMaterialrowsBean rowsBean = new PlanMaterialrowsBean();
		rowsBean.thumbnail = url;
		rowsBean.modify =  Calendar.getInstance().getTimeInMillis();
		
		rowsBean.params = new ArrayList<PlanMaterialparamsBean>();
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, beginTime));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.END_TIME, endTime));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.ELAPSE_TIME, "30"));
		rowsBean.params.add(new PlanMaterialparamsBean(AdParams.IMAGE_EFFECT, "1"));
		
		rowsBean.periods = new ArrayList<FindAdPeriodsperiodsBean>();
		FindAdPeriodsperiodsBean default_period = new FindAdPeriodsperiodsBean();
		if(isOffline)
		{
			default_period.beginTime = "2017-01-01 00:00:00";
			default_period.endTime = "2038-01-01 00:00:00";
		}
		else {
			default_period.beginTime = "00:00";
			default_period.endTime = "00:00";
		}
		default_period.loops = 1;
		default_period.max = 65535;
		rowsBean.periods.add(default_period);
		
		return rowsBean;
	}
	
	
	public static PlanListrowsBean makePlanListrowsBean(boolean isOffLine) {
		String name = getCurrentTime();
		PlanListrowsBean bean = new PlanListrowsBean();
		if(isOffLine)
			bean.name=name;
		else bean.name = name+"_mobile";
		bean.contractName = name;
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;
		if(isOffLine)
			sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		else sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
		bean.beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		bean.endTime = sdf.format(calendar.getTime());
		if(!isOffLine)
			bean.contractId=-1;
		return bean;
	}
	public static PlanListrowsBean makePlanListrowsBean(String templateId,boolean isOffLine) {
		String name = getCurrentTime();
		PlanListrowsBean bean = new PlanListrowsBean();
		if(isOffLine)
			bean.name=name;
		else bean.name = name+"_mobile";
		bean.contractName = name;
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf;
		if(isOffLine){
			sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		}else {
			sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
		}
		bean.beginTime = sdf.format(calendar.getTime());
		calendar.add(Calendar.YEAR, 1);
		bean.endTime = sdf.format(calendar.getTime());
		bean.templateId=templateId;
		if(!isOffLine)
			bean.contractId=-1;
		return bean;
	}
		
	
	private void removeFiles(File ad_dir) {
		File theme_dir = new File(ad_dir, "theme");
		File playbill_dir = new File(ad_dir, "playbill");
		
		String themeName = firstFileName(theme_dir, "theme_offline.*\\.xml", null);
		if (themeName != null) {
			deleteFile(new File(theme_dir, themeName));
		}
		
		String picPlaybillName = firstFileName(playbill_dir, "playbill_picture.*\\.xml", null);
		if (picPlaybillName != null) {
			deleteFile(new File(playbill_dir, picPlaybillName));
		}
		
		String textPlaybillName = firstFileName(playbill_dir, "playbill_text.*\\.xml", null);
		if (textPlaybillName != null) {
			deleteFile(new File(playbill_dir, textPlaybillName));
		}

	}

	public void saveOfflinePlanMaterial(PlanListrowsBean planBean,
			List<PlanMaterialrowsBean> imageMaterials,
			List<PlanMaterialrowsBean> textMaterials, File ad_dir) {
		mAction = ACTION_CACHE_ONLY;

//		String name = planBean.name;
		mPkgCacheDir = ad_dir;
		mDestFilePath = mPkgCacheDir.getAbsolutePath();
		mkdir(mPkgCacheDir);
		mkdir(mPkgCacheDir, "picture");		
		mkdir(mPkgCacheDir, "video");		
		mkdir(mPkgCacheDir, "theme");
		mkdir(mPkgCacheDir, "playbill");
		
		removeFiles(mPkgCacheDir);
		
		writeThemePolicy(mPkgCacheDir);
		boolean hasText = textMaterials != null && textMaterials.size() > 0 ? true : false;
		writeForm(hasText);
		
		writePlanListrowsBean(planBean, mPkgCacheDir);
		
		File playbill_dir = new File(mPkgCacheDir, "playbill");
		if (imageMaterials != null) {
			for (PlanMaterialrowsBean bean : imageMaterials) {
				mImageList.add(bean.thumbnail);
			}
			File material_file = new File(playbill_dir, firstPicPlaybillName(playbill_dir));
			PlanMaterialBean pmBean = new PlanMaterialBean();
			pmBean.rows = imageMaterials;
			writePlanMaterial(pmBean, material_file, 0);
		}

		if (textMaterials != null) {
			File material_file = new File(playbill_dir, firstTextPlaybillName(playbill_dir));
			PlanMaterialBean pmBean = new PlanMaterialBean();
			pmBean.rows = textMaterials;
			writePlanMaterial(pmBean, material_file, 1);
		}
		
		mProgress = 0;
		int left = (100 - mProgress);
		mImageProgressBase = left;
		if (mImageList.size() > 0) {
			mImageProgressBase = (float) left / mImageList.size();
		} else {
			notifyProgress(mProgress + left);
		}

		mNextStep = 5;
		postNextStep();
	}

}
