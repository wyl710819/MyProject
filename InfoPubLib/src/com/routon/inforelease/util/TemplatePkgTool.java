package com.routon.inforelease.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.routon.ad.element.TemplateEditInfo;
import com.routon.ad.element.TemplateInfo;
import com.routon.ad.pkg.FileGetTask;

import android.R.integer;
import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class TemplatePkgTool {
	private static final String TAG = TemplatePkgTool.class.getName();
	public static final String TEMPLATE_EDIT_DIR_NAME = "template_edit";
	public static final String TEMPLATE_EDIT_INFO_FILE_NAME = "template_edit_info.xml";
	//Add生成模板编辑目录
	public  static String createTmpTemplateDir(Context context)
	{
		String currentPath = context.getFilesDir().getAbsolutePath();
		String dirPath = currentPath + "/template_edit/" + "tmp/";
		File directory = new File(dirPath);
		//如果原目录存在，清空原目录
		directory.deleteOnExit();
		directory.mkdirs();
		return dirPath;
	}
	
	// 压缩指定文件夹文件
	public static File fileToZip(String sourceFilePath, String zipFilePath,String fileName) {
			File file=new File(sourceFilePath, fileName+".zip");
			if(file.exists())
				file.delete();
			File sourceFile = new File(sourceFilePath);
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			FileOutputStream fos = null;
			ZipOutputStream zos = null;

			if (sourceFile.exists() == false) {
				Log.d(TAG, "待压缩的文件目录：" + sourceFilePath + "不存在.");
			} else {
				try {
					File zipFile = new File(zipFilePath + "/" + fileName + ".zip");
						File[] sourceFiles = sourceFile.listFiles();
						if (null == sourceFiles || sourceFiles.length < 1) {
							Log.d(TAG, "待压缩的文件目录：" + sourceFilePath+ "里面不存在文件，无需压缩.");
						} else {
							fos = new FileOutputStream(zipFile);
							zos = new ZipOutputStream(new BufferedOutputStream(fos));
							byte[] bufs = new byte[1024 * 10];
							for (int i = 0; i < sourceFiles.length; i++) {
								if(sourceFiles[i].getName().endsWith(".zip"))
								{
									sourceFiles[i].delete();
									continue;
								}
								// 创建ZIP实体，并添加进压缩包
								ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
								zos.putNextEntry(zipEntry);
								// 读取待压缩的文件并写进压缩包里
								fis = new FileInputStream(sourceFiles[i]);
								bis = new BufferedInputStream(fis, 1024 * 10);
								int read = 0;
								while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
									zos.write(bufs, 0, read);
									sourceFiles[i].delete();
								}

							}
							return zipFile;
						}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					// 关闭流
					try {
						if (null != bis)
							bis.close();
						if (null != zos)
							zos.close();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}
			return null;
		}
	
	public static void deleteTmpTemplateDir(Context context){
		String currentPath = context.getFilesDir().getAbsolutePath();
		String dirPath = currentPath +"/"+TEMPLATE_EDIT_DIR_NAME+"/tmp";
		File directory = new File(dirPath);
		directory.deleteOnExit();
	}
	
	public static String getNormalTmeplateDir(Context context,String resId,String name){
		String normalDir = context.getFilesDir().getAbsolutePath()+"/"+TEMPLATE_EDIT_DIR_NAME+"/template_"+resId+"_"+name;
		Log.d("TemplatePkgTool","getNormalTmeplateDir normalDir:"+normalDir);
		return normalDir;
	}
	
	public static void mvOldDirToNew(String oldDir,String newDir){
		File oldFile = new File(oldDir);
		File newFile = new File(newDir);
			
		if( oldFile.exists() && oldFile.isDirectory() && !newFile.exists())
		{
			oldFile.renameTo(newFile);
		}
	}
	
	//解析编辑包的xml信息
	public static TemplateInfo parseTemplateEditXml(String templateEditXmlPath) {
		
		TemplateInfo templateinfo = new TemplateInfo();
		ArrayList<TemplateEditInfo> templateEditInfos = new ArrayList<TemplateEditInfo>();
		templateinfo.mTemplateEditInfos = templateEditInfos;
		TemplateEditInfo templateEditInfo = null;
		File file = new File(templateEditXmlPath);
		String templateUrl = null;
		if (file.exists()) {
//			StringBuffer sb = new StringBuffer();
//			FileInputStream fis = null;
//			BufferedReader reader = null;
//			try {
//				fis = new FileInputStream(file);
//				reader = new BufferedReader(new InputStreamReader(fis));
//				String line = null;
//				while ((line = reader.readLine()) != null) {
//					sb.append(line);
//				}
//				fis.close();
//				reader.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			FileReader xmlReader = null;
			try {
			      xmlReader = new FileReader(file);
		    } catch (FileNotFoundException e) {

		    }
			if( xmlReader == null ){
				return templateinfo;
			}
//			Log.d("templatepkgtool","parseTemplateEditXml:"+sb);
			try {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlPullParser = factory.newPullParser();
//				StringReader stringReader = new StringReader(sb.toString());
				xmlPullParser.setInput(xmlReader);
				int eventType = xmlPullParser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						String tagName = xmlPullParser.getName();
						if(tagName.equals("edit_items"))
							templateinfo.mTemplate = xmlPullParser.getAttributeValue(0);
						else if (tagName.equals("edit_item")) {
							templateEditInfo = new TemplateEditInfo();
							templateEditInfo.setType(xmlPullParser.getAttributeValue(0));
							templateEditInfo.setX(Float.parseFloat(xmlPullParser.getAttributeValue(1)));
							templateEditInfo.setY(Float.parseFloat(xmlPullParser.getAttributeValue(2)));
						} else if (tagName.equals("content")) {						
							templateEditInfo.setContent(xmlPullParser.nextText());
						} else if (tagName.equals("scale"))
							templateEditInfo.setScale(Float.parseFloat(xmlPullParser.nextText()));
						else if (tagName.equals("color"))
						{
							if(templateEditInfo.getType().equals("text"))
								templateEditInfo.setColor(xmlPullParser.nextText());	
						}
						else if (tagName.equals("rotate"))
						{
							if(templateEditInfo.getType().equals("pic"))
								templateEditInfo.setRotate(Float.parseFloat(xmlPullParser.nextText()));
						}
						break;
					case XmlPullParser.END_TAG:
						if (xmlPullParser.getName().equals("edit_item")) {
							templateEditInfos.add(templateEditInfo);
							templateEditInfo = null;
						}
						break;
					default:
						break;
					}
					eventType = xmlPullParser.next();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return templateinfo;
	}
	
	//解压ZIP包
	public  static void unzip(Context context,String filePath) {
        File source = new File(filePath);
        int dot=source.getName().lastIndexOf(".");
        String fileNameString="";
        if(dot>0&&dot<source.getName().length())
        fileNameString=source.getName().substring(0, dot);
        if (source.exists()) {
            ZipInputStream zis = null;
            BufferedOutputStream bos = null;
            try {
                zis = new ZipInputStream(new FileInputStream(source));
                ZipEntry entry = null;
                while ((entry = zis.getNextEntry()) != null
                        && !entry.isDirectory()) {
                    File target = new File(context.getFilesDir().getAbsolutePath()+"/template_edit/"+fileNameString, entry.getName());
                    if(target.exists())
                    {
                    	target.delete();
                    	target.createNewFile();
                    }
                    if (!target.getParentFile().exists()) {
                        // 创建文件父目录
                        target.getParentFile().mkdirs();
                    }
                    // 写入文件
                    bos = new BufferedOutputStream(new FileOutputStream(target));
                    int read = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                }
                zis.closeEntry();
                zis.close();
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
               source.delete();
            }
        }
	}
	
	// Add生成模板编辑信息XML文件
	public static String generateEditXml(List<TemplateEditInfo> editInfos,String template) {
		StringWriter xmlWriter = new StringWriter();
		XmlSerializer xmlSerializer = Xml.newSerializer();
		try {
			xmlSerializer.setOutput(xmlWriter);
			xmlSerializer.startDocument("utf-8", null);
			xmlSerializer.startTag("", "edit_items");	
			xmlSerializer.attribute("", "template", template);
			for (int i = 0; i < editInfos.size(); i++) {
				String type = editInfos.get(i).getType();
				xmlSerializer.startTag("", "edit_item");
				xmlSerializer.attribute("", "type", editInfos.get(i).getType());
				xmlSerializer.attribute("", "x",String.valueOf(editInfos.get(i).getX()));
				xmlSerializer.attribute("", "y",String.valueOf(editInfos.get(i).getY()));
				xmlSerializer.startTag("", "content");
				xmlSerializer.text(editInfos.get(i).getContent());
				xmlSerializer.endTag("", "content");
				xmlSerializer.startTag("", "scale");
				xmlSerializer.text(String.valueOf(editInfos.get(i).getScale()));
				xmlSerializer.endTag("", "scale");
				xmlSerializer.startTag("", "color");
				if (type.equals("text")) {
					xmlSerializer.text(String.valueOf(editInfos.get(i).getColor()));
				}
				xmlSerializer.endTag("", "color");
				xmlSerializer.startTag("", "rotate");
				if (type.equals("pic")) {
					xmlSerializer.text(String.valueOf(editInfos.get(i).getRotate()));
				}
				xmlSerializer.endTag("", "rotate");
				xmlSerializer.endTag("", "edit_item");
			}
			xmlSerializer.endTag("", "edit_items");
			xmlSerializer.endDocument();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xmlWriter.toString();
	}
	
	//Add在指定目录生成模板原图
	public static void saveTemplateOriginalPic(String templatePath,String dirPath,String templateName)
	{
		String newPath = null;
		if( dirPath.endsWith("/")){
			newPath = dirPath + templateName;
		}else{
			newPath = dirPath + "/" + templateName;
		}
		File tf = new File(newPath);
		if( tf.exists() == true ){//目标文件存在，则不复制
			return;
		}
		try {
			FileGetTask.copyFile(templatePath, newPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Add在指定目录生成模板编辑Xml文件
	public static void saveTmplateEditInfoFile(String dirPath,List<TemplateEditInfo> editInfos,String template) {
		File xmlFile = new File(dirPath,TEMPLATE_EDIT_INFO_FILE_NAME);
		
		if( xmlFile.exists() == true ){
			xmlFile.delete();
		}

		try {
			xmlFile.createNewFile();
			RandomAccessFile file = new RandomAccessFile(xmlFile, "rwd");
			file.seek(file.length());
			file.write(generateEditXml(editInfos,template).getBytes());
			file.close();
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void deleteUnUsedPic(String templateEditPath,List<TemplateEditInfo> mTemplateEditInfos)
	{
		File templateEditDir = new File(templateEditPath);
		if(templateEditDir.exists()&&templateEditDir.isDirectory())
		{
			File[] files = templateEditDir.listFiles();
			if(files.length>0)
			{
				for(int i=0;i<files.length;i++)
				{
					String fileName=files[i].getName();
					if(fileName.endsWith(".png")&&fileName.startsWith("pic"))
					{
						boolean flag = false;
						for(int j = 0;j < mTemplateEditInfos.size();j++)
						{
							if( mTemplateEditInfos.get(j).getContent().equals(fileName))
								flag = true;
						}
						if(!flag)
							files[i].delete();
					}
				}
			}
		}
	}
	
	public static String getTemplateId(String templateName)
	{
		int index=templateName.lastIndexOf(".");
		String templateId=templateName.substring(9, index);
		return templateId;
	}
}
