package com.routon.smartcampus.swtchCtrl.treeAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.routon.edurelease.R;
import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.smartcampus.swtchCtrl.ListDetailAdapter;
import com.routon.smartcampus.swtchCtrl.SwtchCtrlDataRequest;
import com.routon.smartcampus.swtchCtrl.SwtchParm;
import com.routon.smartcampus.swtchCtrl.ListDetailAdapter.ListDetailListener;
public class RecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private Context context;
    private List<DataBean> dataBeanList;
//	private List<TerminalGroup> mGroups;
    private LayoutInflater mInflater;
    private OnScrollListener mOnScrollListener;

    public RecyclerAdapter(Context context, List<DataBean> dataBeanList) {
        this.context = context;
        this.dataBeanList = dataBeanList;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case DataBean.PARENT_ITEM:
                view = mInflater.inflate(R.layout.item_swtch_ctrl_toplist, parent, false);
                return new ParentViewHolder(context, view);
            case DataBean.CHILD_ITEM:
                view = mInflater.inflate(R.layout.item_swtch_ctrl_detail, parent, false);
                return new ChildViewHolder(context, view);
            default:
                view = mInflater.inflate(R.layout.item_swtch_ctrl_toplist, parent, false);
                return new ParentViewHolder(context, view);
        }
    }

    /**
     * 根据不同的类型绑定View
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case DataBean.PARENT_ITEM:
                ParentViewHolder parentViewHolder = (ParentViewHolder) holder;
                parentViewHolder.bindView(dataBeanList.get(position), position, itemClickListener);
                break;
            case DataBean.CHILD_ITEM:
                ChildViewHolder childViewHolder = (ChildViewHolder) holder;
                childViewHolder.bindView(dataBeanList.get(position), position,childClickLister);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataBeanList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dataBeanList.get(position).type;
    }

private ListDetailListener childClickLister = new ListDetailListener() {
		
		@Override
		public void onSwtchClick(TerminalListdatasBean terminal, int swtchValue) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			List<SwtchParm> swtchs = new ArrayList<SwtchParm>();
			String id = terminal.terminalid;
			int swtch = swtchValue;
			int status = 0;
			for (TerminalListSwtchBean item:terminal.mswtchs)
			{
				if (swtchValue == item.swtch)
				{
					status = item.status;
					
				}
			}
			SwtchParm parm =  new SwtchParm(id, swtch, status);
			swtchs.add(parm);
//			SwtchCtrlDataRequest.baseAcitivity = this;
			SwtchCtrlDataRequest.sendSwtchCtrl(swtchs);
		}
		
		@Override
		public void onItemClick(View view, int position) {
			// TODO Auto-generated method stub
			
		}
	};
    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onExpandChildren(DataBean bean) {
            int position = getCurrentPosition(bean.ID);//确定当前点击的item位置
            List<DataBean> children = getChildDataBean(bean);//获取要展示的子布局数据对象，注意区分onHideChildren方法中的getChildBean()。
            if (children == null) {
                return;
            }
            add(children, position + 1);//在当前的item下方插入
            if (position == dataBeanList.size() - 2 && mOnScrollListener != null) { //如果点击的item为最后一个
                mOnScrollListener.scrollTo(position + 1);//向下滚动，使子布局能够完全展示
            }
        }

        @Override
        public void onHideChildren(DataBean bean) {
            int position = getCurrentPosition(bean.ID);//确定当前点击的item位置
            List<DataBean> children = bean.childBean;//获取子布局对象
            if (children == null) {
                return;
            }
            remove(position + 1,children.size());//删除
            if (mOnScrollListener != null) {
                mOnScrollListener.scrollTo(position);
            }
        }
    };

    /**
     * 在父布局下方插入一条数据
     * @param bean
     * @param position
     */
    public void add(List<DataBean> bean, int position) {
        dataBeanList.addAll(position, bean);
//        notifyItemInserted(position);
        notifyItemRangeInserted(position, bean.size());
        
    }

    /**
     *移除子布局数据
     * @param position
     */
    protected void remove(int position,int count) {
    	
	   ListIterator<DataBean> iter = dataBeanList.listIterator();
       while (iter.hasNext()) {
    	   int index = iter.nextIndex();
    	   DataBean item = iter.next();
    	   if (index >= position && index <= position+count-1)
    	   {
    		   iter.remove();
    	   }

       }
    	
    	
//        dataBeanList.re
//        notifyItemRemoved(position);
        notifyItemRangeRemoved(position, count);
       
    }

    /**
     * 确定当前点击的item位置并返回
     * @param uuid
     * @return
     */
    protected int getCurrentPosition(String uuid) {
        for (int i = 0; i < dataBeanList.size(); i++) {
            if (uuid.equalsIgnoreCase(dataBeanList.get(i).ID)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 封装子布局数据对象并返回
     * 注意，此处只是重新封装一个DataBean对象，为了标注Type为子布局数据，进而展开，展示数据
     * 要和onHideChildren方法里的getChildBean()区分开来
     * @param bean
     * @return
     */
    private List<DataBean> getChildDataBean(DataBean bean){
       return bean.childBean;
//        return child;
    }

    /**
     * 滚动监听接口
     */
    public interface OnScrollListener{
        void scrollTo(int pos);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener){
        this.mOnScrollListener = onScrollListener;
    }
}
