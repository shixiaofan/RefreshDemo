package com.example.administrator.refreshdemo.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.administrator.refreshdemo.R;
import java.util.List;
public class ViewHolderAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mStringList;
    private List<Integer> mIntegerList;
    private LayoutInflater mLayoutInflater;
    public ViewHolderAdapter(Context context,List<String> stringList,List<Integer> integerList){
        this.mContext=context;
        this.mStringList=stringList;
        this.mIntegerList=integerList;
        mLayoutInflater=LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mStringList.size();
    }
    @Override
    public Object getItem(int position) {
        return mStringList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if (convertView==null){//初始化viewHolder
            viewHolder=new ViewHolder();
            convertView=mLayoutInflater.inflate(R.layout.simple_item_listview,parent,false);
            viewHolder.mImageView= (ImageView) convertView.findViewById(R.id.item_imageView);
            viewHolder.mTextView= (TextView) convertView.findViewById(R.id.item_textView);
            convertView.setTag(viewHolder);

        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        //设置布局中控件要显示的视图
        Glide.with(mContext).load(mIntegerList.get(position)).centerCrop().into(viewHolder.mImageView);
        viewHolder.mTextView.setText(mStringList.get(position));
        return convertView;//显示item布局视图
    }

    class ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;
    }
}
