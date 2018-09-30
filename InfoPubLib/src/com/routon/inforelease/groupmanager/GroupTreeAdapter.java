package com.routon.inforelease.groupmanager;

import java.util.List;

import com.routon.inforelease.R;
import com.routon.inforelease.widget.treeView.Node;
import com.routon.inforelease.widget.treeView.TreeListViewAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class GroupTreeAdapter<T> extends TreeListViewAdapter<T>
{
//	private String TAG = "GroupTreeAdapter";

//	public ArrayList<Node> selectList = new ArrayList<Node>();
	
	public GroupTreeAdapter(ListView mTree, Context context, List<T> datas,
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
			convertView = mInflater.inflate(R.layout.group_list_uncheck_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.id_treenode_icon);
			viewHolder.label = (TextView) convertView
					.findViewById(R.id.id_treenode_label);		
			viewHolder.rightIcon = (ImageView) convertView.findViewById(R.id.infoicon);
			convertView.setTag(viewHolder);

		} else
		{
			viewHolder = (ViewHolder) (convertView.getTag());
		}

		if (node.getIcon() == -1)
		{
			viewHolder.icon.setVisibility(View.INVISIBLE);
		} else
		{
			viewHolder.icon.setVisibility(View.VISIBLE);
			viewHolder.icon.setImageResource(node.getIcon());
		}
		
		if( node.getChildrenCout() > 0 ){
			viewHolder.rightIcon.setVisibility(View.INVISIBLE);
		}else{
			viewHolder.rightIcon.setVisibility(View.VISIBLE);
		}

		viewHolder.label.setText(node.getName());				
		
		return convertView;
	}	
	
	private final class ViewHolder
	{
		ImageView icon;
		TextView label;
		ImageView rightIcon;
	}

}
