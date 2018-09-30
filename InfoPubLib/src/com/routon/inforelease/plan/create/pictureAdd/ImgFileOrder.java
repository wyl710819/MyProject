package com.routon.inforelease.plan.create.pictureAdd;

import java.io.File;
import java.util.Comparator;

public class ImgFileOrder implements Comparator<File>{

	@Override
	public int compare(File lhs, File rhs) {
		 if(lhs.lastModified() > rhs.lastModified())  
         {  
             return -1;  
         }else  
         {  
             return 1;  
         }  
	}

	

}
