package com.routon.inforelease.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

public class PublishStateUtils {
	
	public static final String NOTICE_SAVE_IDS_FILE = "notice_save_ids.txt";
	
	public static List<String> getSavedNoticeIDs(Context context) {
		List<String> idList = new ArrayList<String>();
		File file = context.getDir(NOTICE_SAVE_IDS_FILE, Context.MODE_PRIVATE);
		if( file == null || file.getPath() == null ){
			return idList;
		}
		String idString = PublishStateUtils.readTxtFile(context,file.getPath());
		String [] ids = null;
		
		if (idString!=null&&!idString.isEmpty()) {
			
			if (idString.contains(",")) {
				ids=idString.split(",");
			}else {
				ids=new String []{idString};
			}
			for (int i = 0; i < ids.length; i++) {
				idList.add(ids[i]);
			}
		}
		
		return idList;
		
	}
	
	public static String readTxtFile(Context context,String filePath){
		String string = "";
		
//		try {
//            File file=new File(filePath);
//            if(file.exists()){ 
//            	
//                InputStreamReader read = new InputStreamReader(
//                new FileInputStream(file),"utf_8");
//                BufferedReader bufferedReader = new BufferedReader(read);
//                String lineTxt = null;
//                while((lineTxt = bufferedReader.readLine()) != null){
//                    string+=lineTxt;
//                }
//                read.close();
//            }else{
//    	          File dir = new File(file.getParent());  
//                  dir.mkdirs();  
//                  file.createNewFile();
//            }
//    } catch (Exception e) {
//        System.out.println("读取文件内容出错");
//        e.printStackTrace();
//    }
		
		 
		try {
			String  fileName=filePath.substring(filePath.lastIndexOf("_")+1,filePath.length());
			FileInputStream inStream;
			inStream = context.openFileInput(fileName);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream(); 
		      
		    int len=0;  
		    byte[] buffer = new byte[1024];  
		    while((len=inStream.read(buffer))!=-1){  
		        outStream.write(buffer, 0, len);
		    }  
		      
		    byte[] content_byte = outStream.toByteArray();  
		    string= new String(content_byte);  
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		    
		    
		return string;
	}
	
	public static void removeData(Context context,String filePath,String id){
		File file = new File(filePath);
		String str = readTxtFile(context,file.getPath());
		String[] strs;
		List<String> idList=new ArrayList<String>();
		if (str!=null && !str.equals("")) {
			
			if (str.contains(",")) {
				strs=str.split(",");
			}else {
				strs=new String[]{str};
			}
			for (int i = 0; i < strs.length; i++) {
				idList.add(strs[i]);
			}
			for (int i = 0; i < idList.size(); i++) {
				if (idList.get(i).equals(id)) {
					idList.remove(i);
				}
			}
			String string = "";
			for (int i = 0; i < idList.size(); i++) {
				if (i==0) {
					string+=idList.get(i);
				}else {
					string+=","+idList.get(i);
				}
			}
//			file.delete();
			modifyData(context,filePath,string);
		}
		
	}
	private static void modifyData(Context context,String filePath,String idString){
		
		File file = new File(filePath);
		
		String  fileName=filePath.substring(filePath.lastIndexOf("_")+1,filePath.length());
        FileOutputStream outfile = null;
        
        try {
           
            outfile = context.openFileOutput(fileName, context.MODE_PRIVATE);
            DataOutputStream dout = null ;
             dout = new DataOutputStream(outfile );
            dout.write(idString.getBytes());  
            dout.close();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	
//	public static String idFilePath=Environment.getExternalStorageDirectory().getPath()+"/infoRelease_publishState";
	
	public static void writeData(Context context,String filePath,String idString){
//		idString="20170517172712,20170517161839,20170516101459,20170515090657,20170511163145,20170510094124,20170510085629";
		
			File file = new File(filePath);
			String str = readTxtFile(context,file.getPath());
			String[] ids;
			String[] strs;
			if (idString.contains(",")) {
				ids=idString.split(",");
			}else {
				ids=new String[]{idString};
			}
			
			if (str!=null && !str.equals("")) {
				
				if (str.contains(",")) {
					strs=str.split(",");
				}else {
					strs=new String[]{str};
				}
				
				 for (int i = 0; i < ids.length; i++) {
			            if (!Arrays.asList(strs).contains(ids[i])) {
			            		str+=","+ids[i];
						}
				}
				
			}else {
				str=idString;
			}
			
			
//            FileWriter fw = null;
//           
//				try {
//					if (!file.exists()) {  
//			            File dir = new File(file.getParent());  
//			            dir.mkdirs();  
//			            file.createNewFile();  
//			        }  
//			        FileOutputStream outStream = new FileOutputStream(file);  
//			        outStream.write(str.getBytes());  
//			        outStream.close();  
//					 
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			
			String  fileName=filePath.substring(filePath.lastIndexOf("_")+1,filePath.length());
	        FileOutputStream outfile = null;
	        
	        try {
	           
	            outfile = context.openFileOutput(fileName, context.MODE_PRIVATE);
	            DataOutputStream dout = null ;
	             dout = new DataOutputStream(outfile );
	            dout.write(str.getBytes());  
	            dout.close();
	        } catch (Exception e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
	       
          
	}
}
