package com.routon.inforelease.plan.create;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflineReleaseTools;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.routon.widgets.Toast;

public class TextSelectActivity extends CustomTitleActivity {
	private static final String TAG = "TextSelectFragment";
	private PullToRefreshListView textsListView;
	private RadioButton minePicsBtn;
	private RadioButton otherPicsBtn;
	private Button addBtn;
	private TextSelAdapter textsAdapter = null;

	private ArrayList<MaterialItem> allMaterialDatas = new ArrayList<MaterialItem>();
	private ArrayList<MaterialItem> myMaterialDatas = new ArrayList<MaterialItem>();

	private int materialType;// 0:mine 1:all

	private int currentAllPage = -1;
	private int currentMyPage = -1;

	private int defaultPageSize = 30;
	private int textFileType = 120;
	private String picAddParams;

	private int ON_TEXT_ADD_FINISH = 1;
	private int ON_GROUP_SELECT_ACTIVITY_FINISH = 0;
	private RadioGroup switchGroup;

	private String mStartBy;
	private ArrayList<String> imageList;
	private String startType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_select_fragment);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mStartBy = bundle.getString("start_by");
			startType = bundle.getString("start_type");
			imageList = bundle.getStringArrayList("image_list");
		}

		currentAllPage = -1;
		currentMyPage = -1;

		initTitleBar(R.string.texts_select_title);
		setTitleNextImageBtnClickListener(R.drawable.ok,new OnClickListener() {

			private PlanListrowsBean mNewPlanBean;

			@Override
			public void onClick(View v) {

				if (mStartBy != null && mStartBy.equals("edit")) {
					ArrayList<String> paramStr = new ArrayList<String>();
					ArrayList<String> contentsList = new ArrayList<String>();
					int len = getSelectMaterialList().size();
					for (int i = 0; i < len; i++) {
						MaterialItem item = getSelectMaterialList().get(i);

						paramStr.add(Integer.toString(item.getId()));
						contentsList.add(item.getContent());
					}
					Intent data = new Intent();
					data.putStringArrayListExtra("select_pic_param", paramStr);
					data.putStringArrayListExtra("select_text_contents", contentsList);
					if (len>0) {
						data.putExtra("isChange", true);
					}
					
					setResult(Activity.RESULT_OK, data);
					finish();
				} else if (mStartBy != null && mStartBy.equals("plan_list")) {

					// if(getSelectMaterialList().size() == 0){
					// Toast.makeText(TextSelectActivity.this, "没有选中任何素材!",
					// Toast.LENGTH_SHORT).show();
					// return;
					// }

					ArrayList<String> textList = new ArrayList<String>();
					int len = getSelectMaterialList().size();
					for (int i = 0; i < len; i++) {
						MaterialItem item = getSelectMaterialList().get(i);
						textList.add(item.getContent());
					}

					mNewPlanBean = new OfflineReleaseTools(TextSelectActivity.this).startMakeOfflinePlan(imageList,
							textList, new OfflineReleaseTools.OnTaskFinishListener() {

								@Override
								public void onFinished(int errcode) {
									if (errcode == 0) {
										Intent intent = new Intent(TextSelectActivity.this, PublishActivity.class);
										intent.putExtra("plan", mNewPlanBean);
										intent.putExtra("start_by", "plan_list");
										intent.putExtra("start_type", startType);
										startActivity(intent);
										finish();
									}
								}
							});
				} else {
					addNewPlan();
				}
			}
		});

		picAddParams = getIntent().getExtras().getString("select_pic_param");

		textsListView = (PullToRefreshListView) findViewById(R.id.list_view);
		textsAdapter = new TextSelAdapter(this);
		textsListView.setAdapter(textsAdapter);
		setPushRefreshListener();

		minePicsBtn = (RadioButton) findViewById(R.id.mine_pics);
		otherPicsBtn = (RadioButton) findViewById(R.id.other_pics);
		addBtn = (Button) findViewById(R.id.add);

		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent addTextIntent = new Intent(TextSelectActivity.this, TextInfoAddActivity.class);
				TextSelectActivity.this.startActivityForResult(addTextIntent, ON_TEXT_ADD_FINISH);
			}
		});

		Button delBtn = (Button) findViewById(R.id.del);
		delBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				delSelectMaterials();
			}
		});

		switchGroup = (RadioGroup) findViewById(R.id.switch_group);
		switchGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.mine_pics) {
					materialType = 0;
					if (currentMyPage == -1) {
						currentMyPage = 1;
						myMaterialDatas.clear();
						getTextMaterials(0, currentMyPage, true);
					} else {
						textsAdapter.setDatas(myMaterialDatas);
						textsListView.setAdapter(textsAdapter);
						textsAdapter.notifyDataSetChanged();
					}
					switchGroup.setBackgroundResource(R.drawable.switch_l);
					minePicsBtn.setTextColor(Color.WHITE);
					otherPicsBtn.setTextColor(Color.rgb(44, 145, 225));
				} else if (checkedId == R.id.other_pics) {
					materialType = 1;
					if (currentAllPage == -1) {
						currentAllPage = 1;
						allMaterialDatas.clear();
						getTextMaterials(1, currentAllPage, true);
					} else {

						textsAdapter.setDatas(allMaterialDatas);
						textsListView.setAdapter(textsAdapter);
						textsAdapter.notifyDataSetChanged();
					}
					switchGroup.setBackgroundResource(R.drawable.switch_r);
					otherPicsBtn.setTextColor(Color.WHITE);
					minePicsBtn.setTextColor(Color.rgb(44, 145, 225));
				}

			}
		});

		switchGroup.check(R.id.other_pics);
	}

	private void delSelectMaterials() {
		String paramStr = new String();
		int len = getSelectMaterialList().size();
		if (len == 0) {
			Toast.makeText(TextSelectActivity.this, R.string.del_materials_1, Toast.LENGTH_LONG).show();
			return;
		}

		for (int i = 0; i < len; i++) {
			if (i == 0) {
				paramStr += "resIds=";
			}
			if (i != 0) {
				paramStr += ",";
			}
			MaterialItem item = getSelectMaterialList().get(i);
			paramStr += Integer.toString(item.getId());
		}

		String urlString = UrlUtils.getDelMaterialUrl();

		urlString += "?" + paramStr;
		Log.i(TAG, "URL:" + urlString);

		final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...Loading...");

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "delResponse=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

						int code = response.optInt("code");
						if ( code == 0) {
							Toast.makeText(TextSelectActivity.this, R.string.del_materials_susccess,
									Toast.LENGTH_LONG).show();

							clearSelectMaterialList();
							reUpdateMaterialsList();
						} else if ( code == -2) {
							InfoReleaseApplication.returnToLogin(TextSelectActivity.this);
						} else {// 失败

							clearSelectMaterialList();
							reUpdateMaterialsList();
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(TextSelectActivity.this, response.optString("msg"), Toast.LENGTH_LONG)
									.show();
							//
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(TextSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ON_TEXT_ADD_FINISH) {
			reUpdateMaterialsList();
		}

		if (requestCode == ON_GROUP_SELECT_ACTIVITY_FINISH) {
			finish();
		}
	}

	private void reUpdateMaterialsList() {
		if (materialType == 0) {
			currentMyPage = 1;
			currentAllPage = -1;
			myMaterialDatas.clear();
			getTextMaterials(0, currentMyPage, true);
			switchGroup.setBackgroundResource(R.drawable.switch_l);
			minePicsBtn.setTextColor(Color.WHITE);
			otherPicsBtn.setTextColor(Color.rgb(44, 145, 225));
		} else {
			currentAllPage = 1;
			currentMyPage = -1;
			allMaterialDatas.clear();
			getTextMaterials(1, currentAllPage, true);
			switchGroup.setBackgroundResource(R.drawable.switch_r);
			otherPicsBtn.setTextColor(Color.WHITE);
			minePicsBtn.setTextColor(Color.rgb(44, 145, 225));
		}
	}

	private void setPushRefreshListener() {
		textsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.i(TAG, "------onPullDownToRefresh-------");

			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

				Log.i(TAG, "------onPullUpToRefresh-------");
				if (materialType == 0) {
					getTextMaterials(0, ++currentMyPage, false);
				}

				if (materialType == 1) {
					getTextMaterials(1, ++currentAllPage, false);
				}
			}
		});
	}

	private void updateListView(final int type) {
		Log.i(TAG, "-------updateListView----------");
		switch (type) {
		case 0:// mine
			Log.i(TAG, "textSize:" + myMaterialDatas.size());
			textsAdapter.setDatas(myMaterialDatas);
			textsAdapter.notifyDataSetChanged();
			// textsListView.setAdapter(textsAdapter);
			break;
		case 1:

			Log.i(TAG, "textSize:" + allMaterialDatas.size());
			textsAdapter.setDatas(allMaterialDatas);
			textsAdapter.notifyDataSetChanged();
			// textsListView.setAdapter(textsAdapter);
			break;
		}
	}

	private void getTextMaterials(int type, int page, boolean isShowDialog) {
		materialType = type;
		String lastResId = null;
		switch (type) {
		case 0:// mine
			if (myMaterialDatas.size() != 0) {
				MaterialItem item = myMaterialDatas.get(myMaterialDatas.size() - 1);
				lastResId = Integer.toString(item.getId());
			}

			getMaterialLists(page, defaultPageSize, textFileType, "my", lastResId, isShowDialog);
			break;
		case 1:
			if (allMaterialDatas.size() != 0) {
				MaterialItem item = allMaterialDatas.get(allMaterialDatas.size() - 1);
				lastResId = Integer.toString(item.getId());
			}
			getMaterialLists(page, defaultPageSize, textFileType, "all", lastResId, isShowDialog);
			break;
		}

	}

	private ProgressDialog loadProgressDialog;

	private void getMaterialLists(final int page, int pageSize, int FileType, String flag, String flagId,
			boolean isShowDialog) {

		String urlString = UrlUtils.getResourceListUrl(page, pageSize, FileType, flag, flagId, null, null, null);
		Log.i(TAG, "URL:" + urlString);
		
		if (isShowDialog) {
			loadProgressDialog = ProgressDialog.show(TextSelectActivity.this, "", "...Loading...");
		} else {
			loadProgressDialog = new ProgressDialog(this);
		}

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (loadProgressDialog != null && loadProgressDialog.isShowing()) {
							loadProgressDialog.dismiss();
						}

						try {
							if (response.getInt("code") == 0) { // 返回成功
								MaterialRequestResult requestResult = new MaterialRequestResult();
								requestResult.info.code = response.getInt("code");
								requestResult.info.msg = response.getString("msg");
								requestResult.info.fullListSize = response.getInt("fullListSize");
								requestResult.info.page = response.getInt("page");
								requestResult.info.pageSize = response.getInt("pageSize");

								JSONArray jsonArray = response.getJSONArray("datas");
								if (jsonArray.length() == 0) {
									textsListView.onRefreshComplete();

									if (page == 1) {
										updateListView(materialType);
									} else {

										if (materialType == 0) {
											currentMyPage--;
										} else {
											currentAllPage--;
										}

										Toast.makeText(TextSelectActivity.this, "没有了!", Toast.LENGTH_LONG).show();
									}
									return;
								}

								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject obj = jsonArray.getJSONObject(i);
									MaterialItem item = new MaterialItem();
									item.setId(obj.getInt("resid"));
									item.setType(obj.getInt("filetypeid"));
									item.setContent(obj.getString("content"));
									item.setCreatetime(obj.getString("createtime"));
									if (materialType == 0) {
										myMaterialDatas.add(item);
									} else {
										allMaterialDatas.add(item);
									}

									Log.d(TAG, "resid:" + item.getId() + "  filetype:" + item.getType() + "  content:"
											+ item.getContent() + " createtime:" + item.getCreatetime());

								}

								updateListView(materialType);

							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(TextSelectActivity.this);
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(TextSelectActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();

							}

							textsListView.onRefreshComplete();
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(TextSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if (loadProgressDialog != null && loadProgressDialog.isShowing()) {
							loadProgressDialog.dismiss();
						}
						textsListView.onRefreshComplete();
					}

				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	public ArrayList<MaterialItem> getSelectMaterialList() {

		return textsAdapter.selectMaterails;
	}

	private void addNewPlan() {

		if ((picAddParams == null || picAddParams.length() == 0) && (getSelectMaterialList().size() == 0)) {
			Toast.makeText(this, "没有选中任何素材!", Toast.LENGTH_LONG).show();
			return;
		}

		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		final String name = df.format(new Date());// +"_mobile";

		String paramStr = new String();
		paramStr += "name=" + name + "_mobile";

		paramStr += picAddParams;// 选中图片素材

		int len = getSelectMaterialList().size();
		for (int i = 0; i < len; i++) {
			MaterialItem item = getSelectMaterialList().get(i);

			paramStr += "&";
			paramStr += "resIds=" + Integer.toString(item.getId());
		}

		String urlString = UrlUtils.getPlanAddUrl();

		urlString += "?" + paramStr;
		Log.i(TAG, "addNewPlan URL:" + urlString);

		final ProgressDialog progressDialog = ProgressDialog.show(TextSelectActivity.this, "", "...Loading...");

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

						try {
							if (response.getInt("code") == 1) {

								int currentPlanId = response.getInt("id");

								Intent intent = new Intent(TextSelectActivity.this, GroupSelectActivity.class);
								intent.putExtra("plan_id", currentPlanId);
								intent.putExtra("plan_name", name);
								intent.putExtra("startBy", "textSelect");
								TextSelectActivity.this.startActivityForResult(intent, ON_GROUP_SELECT_ACTIVITY_FINISH);

							} else if (response.getInt("code") == -2) {
								TextSelectActivity.this.returnToLogin();
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(TextSelectActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
								//
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(TextSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void clearSelectMaterialList() {
		textsAdapter.selectMaterails.clear();
	}
}
