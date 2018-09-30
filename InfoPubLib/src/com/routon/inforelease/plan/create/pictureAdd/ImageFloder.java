package com.routon.inforelease.plan.create.pictureAdd;

public class ImageFloder
{
	/**
	 * 图片的文件夹路径
	 */
	private String dir = null;

	/**
	 * 第一张图片的路径
	 */
	private String firstImagePath = null;

	/**
	 * 文件夹的名称
	 */
	private String name;

	/**
	 * 图片的数量
	 */
	private int count;
	
	private boolean isAllImageFolder = false;

	public String getDir()
	{
		return dir;
	}

	public void setDir(String dir)
	{
		this.dir = dir;
		int lastIndexOf = this.dir.lastIndexOf("/");
		this.name = this.dir.substring(lastIndexOf);
	}

	public String getFirstImagePath()
	{
		return firstImagePath;
	}

	public void setFirstImagePath(String firstImagePath)
	{
		this.firstImagePath = firstImagePath;
	}

	public String getName()
	{
		return name;
	}
	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public boolean isAllImageFolder() {
		return isAllImageFolder;
	}

	public void setAllImageFolder(boolean isAllImageFolder) {
		this.isAllImageFolder = isAllImageFolder;
	}

	public void setName(String name) {
		this.name = name;
	}

}
