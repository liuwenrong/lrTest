package com.coolyota.logreport.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.coolyota.logreport.CYLogReporterApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static java.lang.String.valueOf;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/16
 */
public class UploadFileUtil {

    private static final String TAG = "UploadFileUtil";
    private static final int TIME_OUT = 10 * 1000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static final String PREFIX = "--";
    private static final String LINE_END = "\n";
    static OkHttpClient mOkHttpClient = new OkHttpClient();
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void uploadFile(final String url, final Map<String, Object> params, File file) {
        OkHttpClient client = new OkHttpClient();
        // form 表单形式上传
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(null, file);
//            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);//图片文件
//            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("file", file.getName(), body);
        }

        // 生成带token的url,并在params中加入3个参数
        String urlMD5 = UrlBuilder.decorateCommonParams(url, null, params);

        if (params != null) {
            // map 里面是请求中所需要的 key 和 value
            for (Map.Entry entry : params.entrySet()) {
                requestBody.addFormDataPart(valueOf(entry.getKey()), valueOf(entry.getValue())).setType(MultipartBody.MIXED);
            }
        }
        Log.i(TAG, "uploadFile: urlMD5 = " + urlMD5);
        Request request = new Request.Builder().url(urlMD5).post(requestBody.build()).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newBuilder().readTimeout(500, TimeUnit.SECONDS).build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                uploadFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
//                    uploadSuccessToUiThread(call, response);

                } else {
//                    uploadNotSuccess(call, response);
                }
            }
        });

    }

    public static <T> void uploadSuccessToUiThread(final Call call, final Response response, final ReqProgressCallBack<T> callBackToService){
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = response.body().string();
                    Log.i(TAG, response.message() + " , body " + str);
                    callBackToService.onSuccessInUiThread();
                    Toast.makeText(CYLogReporterApplication.getInstance(), "上传" + str, Toast.LENGTH_LONG).show();
                } catch (IOException e) {

                }
            }
        });
    }

    public static <T> void uploadNotSuccess(final Call call, final Response response , final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    callBackToService.onFail();
                    String string = response.body().string();
                    Log.i(TAG, response.message() + " error : body " + string);
                    Toast.makeText(CYLogReporterApplication.getInstance(), "" + string, Toast.LENGTH_LONG).show();
                } catch (IOException e) {

                }
            }
        });
    }

    public static <T> void uploadFailure(final Call call, final IOException e, final ReqProgressCallBack<T> callBackToService) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBackToService.onFail();
                Log.i(TAG, "onFailure: call = " + call.toString() + ",e = " + e);
                Toast.makeText(CYLogReporterApplication.getInstance(), "上传失败," + e, Toast.LENGTH_LONG).show();

            }
        });
    }


    /**
     * 带参数带进度 上传文件
     *
     * @param url       接口地址
     * @param paramsMap 参数
     * @param callBackToService  回调
     * @param <T>
     */
    public static <T> void upLoadFile(String url, HashMap<String, Object> paramsMap, final ReqProgressCallBack<T> callBackToService) {
        try {
            // 生成带token的url,并在params中加入3个参数
            String urlMD5 = UrlBuilder.decorateCommonParams(url, null, paramsMap);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), createProgressRequestBody(/*MEDIA_OBJECT_STREAM*/ null, file, callBackToService));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(urlMD5).post(body).build();
            final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    uploadFailure(call, e, callBackToService);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        uploadSuccessToUiThread(call, response, callBackToService);

                    } else {
                        uploadNotSuccess(call, response, callBackToService);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 创建带进度的RequestBody
     *
     * @param contentType MediaType
     * @param file        准备上传的文件
     * @param callBack    回调
     * @param <T>
     * @return
     */
    public static <T> RequestBody createProgressRequestBody(final MediaType contentType, final File file, final ReqProgressCallBack<T> callBack) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                long lastRefreshUiTime = 0;  //最后一次刷新的时间
                long lastWriteBytes = 0;     //最后一次写入字节数据
                try {

                    source = Okio.source(file);
                    Buffer buf = new Buffer();
                    long remaining = contentLength();
                    long current = 0;
                    for (long readCount; (readCount = source.read(buf, 2048)) != -1; ) {
                        sink.write(buf, readCount);
                        current += readCount;

                        final long finalSum = current;
                        long curTime = System.currentTimeMillis();
                        //每200毫秒刷新一次数据
                        if (curTime - lastRefreshUiTime >= 200 || current == remaining) {
                            //计算下载速度
                            long diffTime = (curTime - lastRefreshUiTime) / 1000;
                            if (diffTime == 0) diffTime += 1;
                            long diffBytes = finalSum - lastWriteBytes;
                            final long networkSpeed = diffBytes / diffTime;

                            progressCallBack(remaining, current, networkSpeed, callBack);
//                                    callback.downloadProgress(finalSum, total, finalSum * 1.0f / total, networkSpeed);   //进度回调的方法


                            lastRefreshUiTime = System.currentTimeMillis();
                            lastWriteBytes = finalSum;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void progressCallBack(final long total, final long current, final long networkSpeed, final ReqProgressCallBack callBack) {
        // 发送到主线程
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onProgressInUIThread(total, current, current * 1.0f / total, networkSpeed);

            }
        });
    }

    public static void uploadHttpUrlConn(String host, File file, Map<String, Object> params, FileUploadListener listener) {
        String BOUNDARY = UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = -- , LINE_END =
        ;
        String CONTENT_TYPE = "multipart/form-data"; //内容类型
        try {

            String urlMD5 = UrlBuilder.decorateCommonParams(host, null, params);

            URL url = new URL(urlMD5);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + " ;boundary=" + BOUNDARY);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            if (file != null) {
                /** * 当文件不为空，把文件包装并且上传 */
                OutputStream outputSteam = conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(LINE_END);
                if (params != null) {//根据格式，开始拼接文本参数
                    for (Map.Entry entry : params.entrySet()) {
                        sb.append(PREFIX).append(BOUNDARY).append(LINE_END);//分界符
                        sb.append("Content-Disposition: form-data; name=" + entry.getKey() + " " + LINE_END);
                        sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                        sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                        sb.append(LINE_END);
                        sb.append(entry.getValue());
                        sb.append(LINE_END);//换行！
                    }
                }
                sb.append(PREFIX);//开始拼接文件参数
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意：
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=file; filename=" + file.getName() + "" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                sb.append(LINE_END);
                //写入文件数据
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                long totalBytes = file.length();
                long curBytes = 0;
                Log.i("cky", "total=" + totalBytes);
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    curBytes += len;
                    dos.write(bytes, 0, len);
                    listener.onProgress(curBytes, 1.0d * curBytes / totalBytes);
                }
                is.close();
                dos.write(LINE_END.getBytes()); //一定还有换行
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功
                 * 当响应成功，获取响应的流
                 */
                int code = conn.getResponseCode();
                Log.i(TAG, "uploadHttpUrlConn: code = " + code);
                sb.setLength(0);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                listener.onFinish(code, sb.toString(), conn.getHeaderFields());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ReqProgressCallBack<T> extends ReqCallBack<T> {
        /**
         * 响应进度更新 在UI线程
         */
        void onProgressInUIThread(long total, long current, float progress, long networkSpeed);
    }

    public interface ReqCallBack<T> {
        void onSuccessInUiThread();

        void onFail();
    }

    public interface FileUploadListener {
        public void onProgress(long pro, double percent);

        public void onFinish(int code, String res, Map<String, List<String>> headers);
    }

    public static void uploadFileHttpClient(final String url, final Map<String, Object> params, File file) {

        // 生成带token的url,并在params中加入3个参数
        String urlMD5 = UrlBuilder.decorateCommonParams(url, null, params);

//        MultipartEntityBuilder entity = MultipartEntityBuilder.create();

        /*String imei = "emte-elts-e36f-rldf";
        entity.addPart("imei", new StringBody(imei, ContentType.TEXT_PLAIN));
        String key = "7576E9DD910227F0D1B297FC05D90BE7";
        entity.addPart("key", new StringBody(key, ContentType.TEXT_PLAIN));
        entity.addPart("timestamp", new StringBody(""+System.currentTimeMillis(), ContentType.TEXT_PLAIN));*/
/*        entity.addPart("logType", new StringBody("20013", ContentType.TEXT_PLAIN));
        entity.addPart("proType", new StringBody("YOTA Y3", ContentType.TEXT_PLAIN));
        entity.addPart("sysVersion", new StringBody("v2017.06.2_release", ContentType.TEXT_PLAIN));
        entity.addPart("upType", new StringBody("20007", ContentType.TEXT_PLAIN));*/

//        if (params != null) {
//            // map 里面是请求中所需要的 key 和 value
//            for (Map.Entry entry : params.entrySet()) {
//                entity.addPart(valueOf(entry.getKey()), new StringBody( String.valueOf(entry.getValue()), ContentType.TEXT_PLAIN));
//            }
//        }
//        entity.addPart("file", new FileBody(file));

        /*String token = MD5Util.md5(key+imei+System.currentTimeMillis());
        String url = "http://127.0.0.1:16001/dcss-collector/log/upload?token="+token;
        String url = "http://test.dcss.baoliyota.com/dcss-collector/log/upload?token="+token;
        String url = "http://172.16.7.29:16001/dcss-collector/log/upload?token="+token;*/

//        HttpPost request = new HttpPost(urlMD5);
//        request.setEntity(entity.build());
//
//        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
//        try {
//            httpClientBuilder.build().execute(request);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

}
