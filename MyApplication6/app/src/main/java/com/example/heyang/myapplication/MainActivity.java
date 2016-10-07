package com.example.heyang.myapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;

    private static String URL = "http://www.imooc.com/api/teacher?type=4&num=30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取xml上的ListView对象
        mListView = (ListView) findViewById(R.id.lv_main);

        new NewsAsyncTask().execute(URL);
    }

    // 通过输入输出流获取整个网页格式的字符串数据
    private String readStream(InputStream is){
        InputStreamReader isr;
        String result = "";

        try {
            String line = "";
            // 1、输入流对象 2、输入流读取对象 3、字节读取对象
            isr = new InputStreamReader(is,"utf-8");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null){
                result += line;
            }
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private List<NewsBean> getJsonData(String url){
        // 创建存储NewsBean的集合对象
        List<NewsBean> newsBeanList = new ArrayList<>();
        try {
            // 取出网络的json字符串的格式之后
            String jsonStr = readStream(new URL(url).openStream());
            // 就要用JSONObject对象进行解析
            JSONObject jsonObject;
            // 然后需要NewsBean,其实相当于IOS的模型对象
            NewsBean newsBean;
            try {
                // jsonObject的对象,创建该对象的同时传入json字符串格式的对象
                jsonObject = new JSONObject(jsonStr);
                // 拿到jsonObject对象之后,就需要通过key值来拿到数组
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                // 然后开始遍历数组,获取模型数组
                for (int i = 0;i<jsonArray.length();i++){
                    // 数组里每一个元素又是jsonObject
                    jsonObject = jsonArray.getJSONObject(i);

                    // 开始创建模型对象
                    newsBean = new NewsBean();
                    newsBean.newsIconURL = jsonObject.getString("picSmall");
                    newsBean.newsTitle = jsonObject.getString("name");
                    newsBean.newsContent = jsonObject.getString("description");

                    // 创建的一个模型对象,就要添加到集合当中去
                    newsBeanList.add(newsBean);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("heyang",jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newsBeanList;
    }

    // 创建一个内部类来实现 ,在实现下面内部类之前,需要自定义的Bean对象来封装处理Josn格式的数据
    class  NewsAsyncTask extends AsyncTask<String,Void,List<NewsBean>>{
        @Override
        protected List<NewsBean> doInBackground(String... strings) {
            return getJsonData(strings[0]);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBeen) {
            super.onPostExecute(newsBeen);
            NewsAdapter newsAdapter = new NewsAdapter(MainActivity.this,newsBeen,mListView);
            mListView.setAdapter(newsAdapter);

        }
    }
}
