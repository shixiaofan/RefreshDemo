package com.example.administrator.refreshdemo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.refreshdemo.adapter.ViewHolderAdapter;
import com.example.administrator.refreshdemo.minterface.OnRefreshListener;
import com.example.administrator.refreshdemo.myview.MyListView;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements OnRefreshListener {
    private Integer[] imageInteger={R.drawable.one,R.drawable.two,R.drawable.three,
            R.drawable.four,R.drawable.five,R.drawable.six,R.drawable.seven,R.drawable.eight,};
    private MyListView mMyListView;
    private List<String> mStringList;
    private List<Integer> mIntegerList;
    private ViewHolderAdapter mViewHolderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyListView= (MyListView) findViewById(R.id.myListView);
        mIntegerList=new ArrayList<>();
        mStringList=new ArrayList<>();
        mViewHolderAdapter=new ViewHolderAdapter(MainActivity.this,mStringList,mIntegerList);
        mMyListView.setAdapter(mViewHolderAdapter);
        mMyListView.setOnRefreshListener(this);
                mMyListView.refreshHeaderView(); //设置回调,刷新头部
        initData();
    }
    private void initData(){
        for (int i=0;i<8;i++){
            mIntegerList.add(imageInteger[i]);
            mStringList.add("items"+i);
        }
    }
    @Override
    public void onDownPullRefresh() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                for (int i=0;i<5;i++){
                    mStringList.add(0,"这是下拉新刷新出来的数据"+i);
                    mIntegerList.add(0,imageInteger[i]);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mViewHolderAdapter.notifyDataSetChanged();
                mMyListView.hideHeaderView();
            }
        }.execute();
    }

    @Override
    public void onLoadingMore() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                for (int i=0;i<5;i++){
                    mStringList.add("这是下滑新加载出来的数据"+i);
                    mIntegerList.add(imageInteger[i]);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mViewHolderAdapter.notifyDataSetChanged();
                mMyListView.hideFooterView();
            }
        }.execute();
    }
}
