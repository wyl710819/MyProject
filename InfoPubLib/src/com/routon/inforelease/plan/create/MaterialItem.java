package com.routon.inforelease.plan.create;

public class MaterialItem{
	private int resid;
	private int filetypeid;
	private String content;
	private boolean checked;
	private String createtime;
	private int species;
	
	public MaterialItem(){
	}
	
	public int getSpecies() {
		return species;
	}

	public void setSpecies(int species) {
		this.species = species;
	}

	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public int getId() {
		return resid;
	}
	public void setId(int id) {
		this.resid = id;
	}
	public int getType() {
		return filetypeid;
	}
	public void setType(int type) {
		this.filetypeid = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean equals(MaterialItem item){
		if(this.resid != item.resid)
			return false;
		if(this.filetypeid != item.filetypeid)
			return false;
		
		return (this.content.equals(item.content)&& (this.createtime.equals(item.createtime)));
		
	}
}