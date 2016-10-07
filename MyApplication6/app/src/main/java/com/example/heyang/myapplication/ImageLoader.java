package com.example.heyang.myapplication;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by HeYang on 16/10/6.
 */

public class ImageLoader {

    private ImageView mImageView;
    private String mURLStr;
    // 创建缓存对象
    // 第一个参数是需要缓存对象的名字或者ID,这里我们传输url作为唯一名字即可
    // 第二个参数是Bitmap对象
    private LruCache<String,Bitmap> lruCache;

    private ListView mListView;
    private Set<NewsAsyncTask> mTasks;

    // 然后我们需要在构造方法中初始化这个缓存对象
    // 另外,我们不可能把所有的缓存空间拿来用
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public ImageLoader(ListView listView){

        mListView = listView;
        mTasks = new HashSet<>();

        // 获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cachaSize =  maxMemory / 4;
        // 创建LruCache对象,同时用匿名内部类的方式重写方法
        lruCache = new LruCache<String,Bitmap>(cachaSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // 我们需要直接返回Bitmap value的实际大小
                //return super.sizeOf(key, value);
                // 在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }

    // 然后我们要写俩个方法:1、将bitmap存入缓存中 2、从缓存中取出bitmap

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void addBitmapToCache(String url, Bitmap bitmap){
        if (getBitMapFromCache(url) == null){
            lruCache.put(url,bitmap);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Bitmap getBitMapFromCache(String url){
        return lruCache.get(url);
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mImageView.getTag().equals(mURLStr)){
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }


        }
    };

    // 根据可见项起止位置加载可见项
    public void loadImages(int startIndex,int endIndex){
        for (int i = startIndex; i < endIndex; i++) {
            String url = NewsAdapter.URLS[i];
            // 在异步请求之前,先判断缓存中是否有,有的话就取出直接加载
            Bitmap bitmap = getBitMapFromCache(url);
            if (bitmap == null){
                // 如果没有,就异步任务加重
//                new NewsAsyncTask(imageView, (String) imageView.getTag()).execute(urlString);// 这两个参数分别传递的目的地可以理解一下
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTasks.add(task);



            }else{
                ImageView loadImageView = (ImageView) mListView.findViewWithTag(url);
                loadImageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTask(){
        if (mTasks != null){
            for (NewsAsyncTask newsTask: mTasks) {
                newsTask.cancel(false);
            }
        }
    }





    public Bitmap getBitmapFromURL(String urlString){
        Bitmap bitmap = null;
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            // 最后要关闭http连接
            connection.disconnect();
//            Thread.sleep(1000);// 睡眠1秒
        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        finally {

            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }


    // ==================使用AsyncTask====================
    public void showImageByAsyncTask(ImageView imageView, final String urlString){
        // 在异步请求之前,先判断缓存中是否有,有的话就取出直接加载
        Bitmap bitmap = getBitMapFromCache(urlString);
        if (bitmap == null){
            // 如果没有,就异步任务加重
//            new NewsAsyncTask( urlString).execute(urlString);// 这两个参数分别传递的目的地可以理解一下
            imageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            imageView.setImageBitmap(bitmap);
        }

    }

    private class NewsAsyncTask extends AsyncTask<String,Void,Bitmap>{

        // 需要私有的ImageView对象和构造方法来传递ImageView对象
//        private ImageView mImageView;
        private String mURlString;

//        public NewsAsyncTask(ImageView imageView,String urlString){
//            mImageView = imageView;
//            mURlString = urlString;
//        }

        public NewsAsyncTask(String urlString){
            mURlString = urlString;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            // 在这个方法中,完成异步下载的任务
            String url = strings[0];
            // 从网络中获取图片
            Bitmap bitmap = getBitmapFromURL(strings[0]);
            if (bitmap != null){
                // 将网络加载出来的图片存储缓存
                addBitmapToCache(url,bitmap);
            }
            return bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // 然后在这个方法中设置imageViewd

            ImageView executeImageView = (ImageView) mListView.findViewWithTag(mURlString);
            if (bitmap != null && executeImageView != null){
                executeImageView.setImageBitmap(bitmap);
            }
            // 执行完当前任务,自然要把当前Task任务remove
            mTasks.remove(this);

//            if (mImageView.getTag().equals(mURlString)){
//                mImageView.setImageBitmap(bitmap);
//            }

        }
    }

    // ==================使用多线程====================
    public void showImageByThread(ImageView imageView, final String urlString){

        mImageView = imageView;
        mURLStr = urlString;

        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURL(urlString);
                // 当前线程是子线程,并不是UI主线程
                // 不是Message message = new Message();
                Message message = Message.obtain();
                message.obj = bitmap;
                handler.sendMessage(message);

            }
        }.start();
    }






}
