package com.routon.inforelease.widget.treeView;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class TreeListViewAdapter<T> extends BaseAdapter
{

	protected Context mContext;
	/**
	 * 存储所有可见的Node
	 */
	protected List<Node> mNodes;
	protected LayoutInflater mInflater;
	/**
	 * 存储所有的Node
	 */
	protected List<Node> mAllNodes;

	/**
	 * 点击的回调接口
	 */
	private OnTreeNodeClickListener onTreeNodeClickListener;

	public interface OnTreeNodeClickListener
	{
		void onClick(Node node, int position);
	}

	public void setOnTreeNodeClickListener(
			OnTreeNodeClickListener onTreeNodeClickListener)
	{
		this.onTreeNodeClickListener = onTreeNodeClickListener;
	}

	/**
	 * 
	 * @param mTree
	 * @param context
	 * @param datas
	 * @param defaultExpandLevel
	 *            默认展开几级树
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public TreeListViewAdapter(ListView mTree, Context context, List<T> datas,
			int defaultExpandLevel) throws IllegalArgumentException,
			IllegalAccessException
	{
		mContext = context;
		/**
		 * 对所有的Node进行排序
		 */
		mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
		/**
		 * 过滤出可见的Node
		 */
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);
		mInflater = LayoutInflater.from(context);

		/**
		 * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
		 */
		mTree.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				expandOrCollapse(position);

				if (onTreeNodeClickListener != null)
				{
					onTreeNodeClickListener.onClick(mNodes.get(position),
							position);
				}
			}

		});

	}
	
	public void removeNode(Node node){
		node.getParent().getChildren().remove(node);
		mAllNodes.remove(node);
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);
	}
	
	public void addNode(String name,int id,Node parentNode){
		Node node = new Node();
		node.setName(name);
		node.setId(id);
		node.setpId(parentNode.getId());
		node.setParent(parentNode);
		
		parentNode.getChildren().add(node);
		parentNode.setExpand(true);
		
		mAllNodes.add(mAllNodes.indexOf(parentNode)+1, node);
//		Log.d("TreeListViewAdapter","before addNode nodes length:"+mNodes.size());
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);	
//		Log.d("TreeListViewAdapter","after addNode nodes length:"+mNodes.size());
	}
	
	public void reloadData(List<T> datas,int defaultExpandLevel){
		/**
		 * 对所有的Node进行排序
		 */
		try {
			mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**
		 * 过滤出可见的Node
		 */
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);
	}

	/**
	 * 相应ListView的点击事件 展开或关闭某节点
	 * 
	 * @param position
	 */
	public void expandOrCollapse(int position)
	{
		Node n = mNodes.get(position);

		if (n != null)// 排除传入参数错误异常
		{
			if (!n.isLeaf())
			{
				n.setExpand(!n.isExpand());
				mNodes = TreeHelper.filterVisibleNode(mAllNodes);
				notifyDataSetChanged();// 刷新视图
			}
		}
	}

	@Override
	public int getCount()
	{
		return mNodes.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mNodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Node node = mNodes.get(position);
		convertView = getConvertView(node, position, convertView, parent);
		// 设置内边距
		convertView.setPadding(node.getLevel() * 30, 3, 3, 3);
		return convertView;
	}

	public abstract View getConvertView(Node node, int position,
			View convertView, ViewGroup parent);

}
