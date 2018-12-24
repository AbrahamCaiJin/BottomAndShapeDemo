package cn.gzzgtech.bottomandshapedemo.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.gzzgtech.bottomandshapedemo.R;

import java.util.List;


public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    protected List<String> lists;
    private OnItemClickListener mOnItemClickListener;
    String string;

    public MainAdapter(List<String> lists){
        this.lists = lists;
    }


    //创建Viewholder（找到布局控件）
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =  layoutInflater.inflate(R.layout.item_main,parent,false);
        MainViewHolder mainViewHolder = new MainViewHolder(view);
        return mainViewHolder;
    }

    //绑定viewHolder(数据)
    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        string = lists.get(position);
        //设置item点击事件
        if(mOnItemClickListener != null){
            holder.item_mainCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(position);
                }
            });
        }
        //绑定数据
        holder.item_mainTv.setText(string);
    }



    //得到recyclerview 条目的数量
    @Override
    public int getItemCount() {
        return lists.isEmpty()  ? 0 : lists.size();
    }

    //找到item里的控件
    public class MainViewHolder extends RecyclerView.ViewHolder{
        CardView item_mainCardview;
        TextView item_mainTv;

        public MainViewHolder(View itemView) {
            super(itemView);
            item_mainCardview = itemView.findViewById(R.id.item_mainCardview);
            item_mainTv = itemView.findViewById(R.id.item_mainTv);
        }
    }
    /**
     *  刷新
     */
    public void setData(List<String> lists){
        this.lists.clear();
        this.lists.addAll(lists);
        notifyDataSetChanged();
    }
    /**
     *  加载更多
     */
    public void loadMore(List<String>  lists){
        this.lists.addAll(lists);
        notifyDataSetChanged();
    }

    //item点击接口
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    //向外暴露点击事件
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }


}
