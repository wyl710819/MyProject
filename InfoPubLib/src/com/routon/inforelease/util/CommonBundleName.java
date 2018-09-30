package com.routon.inforelease.util;

public class CommonBundleName {
	public static final String TEMPLATE_ID_TAG = "templateId";//string类型 模板原型id
	public static final String RES_ID_TAG = "redId";//string类型 素材id //模板编辑后生成的图片资源id
	public static final String PIC_PATH_TAG = "picPath";//string类型 图片路径 //模板编辑后生成的图片的文件路径
	public static final String SPECIES_TAG = "species";//int类型 素材模板的分类1欢迎；2表彰；3通知 模板编辑后生成的素材模板类型不变
	public static final String OFFLINE_TAG = "offline_tag";//boolean类型 是否离线状态
	public static final String DATA_TAG = "data";//data类型 传递class数据或者PlanListrowsBean数据
	public static final String TEMPLATE_INFO_TAG = "TemplateInfo";//Serializable类型,传递模板编辑数据 TemplateInfo数据类型
	
	public static final String TEMPLATE_DIR_PATH_TAG = "TemplateDirPath";//string类型，传递模板编辑包目录
	
	public static final String TEMPLATE_URL_TAG = "template_url";
	
	public static final String APP_TYPE_TAG = "app_type_tag";//int类型，数据如下
	public static final int CLASSINFO_APP_TYPE = 0;
	public static final int PLAN_APP_TYPE = 1;
	
	public static final String  CLASSINFO_TYPE_TAG = "classInfoType";//int类型
	
	public static final String SELECT_PIC_PARAM_TAG = "select_pic_param";//string arraylist
	
	public static final String SELECT_PIC_URL_TAG = "select_pic_url";//string arraylist
	
	public static final String FILE_TYPE_TAG = "fileType";//int类型 对应MaterialParams类，文件类型
	
	public static final String TITLEBAR_NEXT_BTN_VISIBLE_TAG = "titlebar_next_btn_visible";//bool类型，标题栏右侧按钮是否显示
	
	public static final String SHOW_SETTING_FRAGMENT = "show_setting_fragment";//bool类型，泛在教育主界面是否显示设置菜单项
	public static final String SHOW_EXIT_FRAGMENT = "show_ext_fragment";//bool类型，泛在教育主界面是否退出菜单项
	
	public static final String MAX_SEL_PIC_NUM = "max_sel_pic_num";//最多可选择图片数目
	
	public static String AuditClassInfoAuthority = "AuditClassInfoAuthority";//boolean classinfo audit authority
	public static String AuditSchoolNoticeAuthority = "AuditSchoolNoticeAuthority";
	//分组选择界面是否只选择班主任分组
	public static final String  GROUP_SEL_HEADTEACHERS = "GroupSelHeadTeachers";//boolean
}
