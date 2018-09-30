package com.routon.smartcampus.answerrelease;

public class OptionContentBean {
	
		
		public  String optionNameStr;
		public  String selCountStr;
		
		public  String optionName=null;
		public  int selCount=0;
		public  int viewPosition;
		public int type;
		public String sid;
		public boolean isClick=false;
		public boolean isExistThisAnswer=false;
        public String getOptionName() {
			return optionName;
		}



		public void setOptionName(String optionName) {
			this.optionName = optionName;
		}

		public OptionContentBean(){}

		public OptionContentBean(String _name , int _count , int _position,int _type){
        	if (!_name.equals("")) {
        		type=_type;
        		optionName=_name;
        		selCount=_count;
        		viewPosition=_position;
        		optionNameStr=_name+"";
            	selCountStr=_count+"位同学选择";
			}else {
				type=_type;
				optionName=_name;
        		selCount=_count;
        		viewPosition=_position;
				optionNameStr=_name;
            	selCountStr=_count+"位同学未选择";
			}
        	
		}

		

		public void setSelCount(int selCount) {
			this.selCount = selCount;
			this.selCountStr = selCount+"位同学选择";
		}
        
        
		

}
