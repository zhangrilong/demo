package com.example.demo.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

public class HttpUtil {

    private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static  CloseableHttpClient client ;


    /**
     */
     static {
        try {
            // 初始化poolConnManager
            LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
            PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            pool.setMaxTotal(300);
            pool.setDefaultMaxPerRoute(50);
            pool.setValidateAfterInactivity(10000);//10s
            SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).build();
            pool.setDefaultSocketConfig(socketConfig);

            // 初始化requestConfig
            RequestConfig  requestConfig = RequestConfig.custom().setConnectionRequestTimeout(3000)
                    .setConnectTimeout(5000).setSocketTimeout(5000)
                    .setCircularRedirectsAllowed(false)
                    .setContentCompressionEnabled(true).build();
            PoolingHttpClientConnectionManager poolConnManager = pool;
            client= HttpClients.custom().setConnectionManager(poolConnManager).setDefaultRequestConfig(requestConfig).build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("PoolingHttpClientConnectionManager初始化失败!");
        }
    }


    public static String  sendGet(String url ){
        HttpGet get = new HttpGet(url);

        try(CloseableHttpResponse res =client.execute(get)) {
            if (HttpStatus.SC_OK == res.getStatusLine().getStatusCode()){
                return handleResponse(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static  String  sendPost(String url , Map<String,Object> param, Map<String,String> headMap){
        HttpPost httpPost = new HttpPost(url);
        //params
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(params, Charset.defaultCharset()));
        //header
        if(headMap!=null&&!headMap.isEmpty()) {
            Set<Map.Entry<String, String>> entries = headMap.entrySet();
            Iterator<Map.Entry<String, String>> iter = entries.iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> next = iter.next();
                httpPost.addHeader(next.getKey(), next.getValue());
            }
        }
        try (CloseableHttpResponse res =client.execute(httpPost)){
            return  handleResponse(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String handleResponse(CloseableHttpResponse res) throws  Exception{
        HttpEntity entity = res.getEntity();
        if (entity!=null){
            return EntityUtils.toString(entity);
        }
        return  null;
    }

    public static void main(String[] args) {
        try {
            String s = sendGet("http://www.baidu.com");
            System.out.println(s);
            HashMap<String,Object> map = new HashMap<>();
            map.put("sms_enabled","1");
            map.put("topic_id","83");
            map.put("secret_key","9b77848c-a83b-4449-a497-1389e060569c");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
