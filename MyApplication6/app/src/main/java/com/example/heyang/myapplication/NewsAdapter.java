package com.example.heyang.myapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by HeYang on 16/10/6.
 */

public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    // 适配器对象需要传入Bean数据集合对象,类似IOS的模型数组集合
    private List<NewsBean> beanList;
    // 然后要传入LayoutInflater对象,用来获取xml文件的视图控件
    private LayoutInflater layoutInflater;

    private ImageLoader imageLoader;

    private boolean isFirstLoadImage;

    // 可见项的起止index
    private int mStart,mEnd;
    // 因为我们需要存储起止项所有的url地址
    public static String[] URLS;

    // 创建构造方法
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public NewsAdapter(MainActivity context, List<NewsBean> data, ListView listView){

        isFirstLoadImage = true;

        beanList = data;
        layoutInflater = LayoutInflater.from(context);// 这个context对象就是Activity对象
        imageLoader = new ImageLoader(listView);

        // 将模型数组中的url字符串单独存储在静态数组中
        int dataSize = data.size();
        URLS = new String[dataSize];
        for (int i = 0; i < dataSize; i++) {
            URLS[i] = data.get(i).newsIconURL;
        }

        // 已经要记得注册
        listView.setOnScrollListener(this);
    }

    @Override
    public int getCount() {
        return beanList.size();
    }

    @Override
    public Object getItem(int i) {
        // 因为beanList是数组,通过get访问对应index的元素
        return beanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null){
            viewHolder = new ViewHolder();
            // 每一个View都要和layout关联
            view = layoutInflater.inflate(R.layout.item_layout,null);
            // 在R.layout.item_layout中有三个控件对象
            // 现在全部传递给view对象了
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) view.findViewById(R.id.tv_content);
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_icon);

            view.setTag(viewHolder);

        }else {

            // 重复利用,但是由于里面的View展示的数据显然需要重新赋值
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvTitle.setText(beanList.get(i).newsTitle);
        viewHolder.tvContent.setText(beanList.get(i).newsContent);
        // 先默认加载系统图片
        viewHolder.ivIcon.setImageResource(R.mipmap.ic_launcher);  // 类似加载占位图片
        viewHolder.ivIcon.setTag(beanList.get(i).newsIconURL);
        // 将ImageView对象和URLSting对象传入进去
//        new ImageLoader().showImageByThread(viewHolder.ivIcon,beanList.get(i).newsIconURL);
        imageLoader.showImageByAsyncTask(viewHolder.ivIcon,beanList.get(i).newsIconURL);
        return view;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        // SCROLL_STATE_IDLE :  滚动结束
        if (i == SCROLL_STATE_IDLE){
            // 加载可见项
            imageLoader.loadImages(mStart,mEnd);
        }else{
            // 停止所有任务
            imageLoader.cancelAllTask();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        // i是第一个可见元素 i1是当前可见元素的长度
        mStart = i;
        mEnd = i + i1;
        // 第一次预加载可见项
        if (isFirstLoadImage && i1 > 0){
            imageLoader.loadImages(mStart,mEnd);
            isFirstLoadImage = false;
        }
    }

    // 最后需要一个匿名内部类来创建一个临时缓存View的对象
    class ViewHolder{
        public TextView tvContent,tvTitle;
        public ImageView ivIcon;
    }
}
