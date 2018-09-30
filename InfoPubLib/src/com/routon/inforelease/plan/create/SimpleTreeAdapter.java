package com.routon.inforelease.plan.create;

import java.util.ArrayList;
import java.util.List;

import com.routon.inforelease.R;
import com.routon.inforelease.widget.treeView.Node;
import com.routon.inforelease.widget.treeView.TreeListViewAdapter;
import com.routon.utils.UtilHelper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SimpleTreeAdapter<T> extends TreeListViewAdapter<T>
{
	private String TAG = "SimpleTreeAdapter";

	public ArrayList<Node> selectList = new ArrayList<Node>();
	
	public SimpleTreeAdapter(ListView mTree, Context context, List<T> datas,
			int defaultExpandLevel) throws IllegalArgumentException,
			IllegalAccessException
	{
		super(mTree, context, datas, defaultExpandLevel);
	}

	@Override
	public View getConvertView(final Node node , int position, View convertView, ViewGroup parent)
	{
		
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.group_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.id_treenode_icon);
			viewHolder.label = (TextView) convertView
					.findViewById(R.id.id_treenode_label);
			viewHolder.checkbox = (ImageView) convertView.findViewById(R.id.item_select);
			convertView.setTag(viewHolder);

		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (node.getIcon() == -1)
		{
			viewHolder.icon.setVisibility(View.INVISIBLE);
		} else
		{
			viewHolder.icon.setVisibility(View.VISIBLE);
			viewHolder.icon.setImageResource(node.getIcon());
		}

		viewHolder.label.setText(node.getName());
		
		if(isSelectListContains(node)){
			viewHolder.checkbox.setImageResource(R.drawable.checkbox_pressed);
		}else{
			viewHolder.checkbox.setImageResource(R.drawable.checkbox_normal);
		}
		
		viewHolder.checkbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				ImageView view = (ImageView)v;
				if(!isSelectListContains(node)){
					selectList.add(node);
					view.setImageResource(R.drawable.checkbox_pressed);
					addChildNodeSelect(node, true);
				}else{
					removeSelectParent(node.getId());
					view.setImageResource(R.drawable.checkbox_normal);
					addChildNodeSelect(node, false);
				}
			}
		});					
		
		return convertView;
	}

	private void addChildNodeSelect(Node node, boolean flag){

		if(node.getChildren() == null)
			return;
		
		List<Node> childrens = node.getChildren();
		int len = childrens.size();
		for(int i=0; i<len; i++){
			
			Node child = childrens.get(i);
			
			if(flag){//选中
				if(!isSelectListContains(child)){
					selectList.add(child);
				}
				
			}else{//非选中
				if(isSelectListContains(child))
					removeSelectId(child.getId());
			}
			
			if(child.getChildren() != null && child.getChildren().size() != 0){
				addChildNodeSelect(child, flag);
			}
				
		}
		notifyDataSetChanged();
	}
	
	private void removeSelectId(int id){
		int len = selectList.size();
		for(int i=0; i<len; i++){
			Node selectItem = selectList.get(i);
			if(selectItem.getId() == id){
				selectList.remove(selectItem);
				return;
			}
		}
	}
	
	private void removeSelectParent(int id){
		int len = selectList.size();
		for(int i=0; i<len; i++){
			Node selectItem = selectList.get(i);
			if(selectItem.getId() == id){
				selectList.remove(selectItem);
				if(!selectItem.isRoot()){
					removeSelectParent(selectItem.getpId());
				}
				return;
			}
		}
	}
	
	private boolean isSelectListContains(Node node){
		int selectSize = selectList.size();
		for(int i= 0; i<selectSize; i++){
			Node selectItem = selectList.get(i);
			if(selectItem.getId() == node.getId()){
				return true;
			}
		}
		return false;
	}
	
	public void setSelectList(String idString){
		if(selectList != null)
			selectList.clear();
		
		if( idString == null || idString.trim().length() == 0 ) return;
		
		String[] idList = idString.split(",");
		int[] ids = new int[idList.length];
		for (int i = 0; i < idList.length; i++) {
			if(UtilHelper.isInteger(idList[i]) == false ){
				continue; 
			}
			ids[i]=Integer.parseInt(idList[i]);
//			Log.i("SimpleTreeAdapter", "----------id:"+ids[i]);
			
			int len = getCount();
			for(int j = 0; j< len; j++){
				Node node = (Node)getItem(j);
				if(node.getId() == ids[i]){
					selectList.add(node);
				}
			}
		}
		notifyDataSetChanged();
	}
	
	private final class ViewHolder
	{
		ImageView icon;
		TextView label;
		ImageView checkbox;
	}

}
