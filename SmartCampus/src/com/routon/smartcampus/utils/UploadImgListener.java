package com.routon.smartcampus.utils;

import java.util.List;


public interface UploadImgListener {
	public void uploadImgSuccessListener(List<Integer> imgFileIdList);
	public void uploadImgErrorListener(String errorStr);
}
