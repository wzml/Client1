package com.example.client;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tools.Sensor;
import tools.Worker;

import static com.example.client.R.color.black;
import static tools.geneData.getBeginS;
import static tools.geneData.getBeginW;
import static tools.geneData.getProcessS;
import static tools.geneData.getProcessW;
import static tools.geneData.getTime;
//AppCompatActivity

public class MainActivity extends Activity implements RtmpHandler.RtmpListener, SrsEncodeHandler.SrsEncodeListener, SrsRecordHandler.SrsRecordListener {
    private Button bt_sensor;
    private Button bt_worker;
    private Button bt_publish;
    private Button bt_pause;
    private Button bt_SwitchCamera;
    private SrsPublisher mPublisher;
    private SrsCameraView mCameraView;
    private volatile boolean flagS = true;
    private volatile boolean flagW = true;
    private static final int SUCCESS = 665;
    private static final int FALL = 894;
    public final static int RC_CAMERA = 100;
    private boolean isPermissionGranted = false;
    private String rtmpUrl = "rtmp://120.77.243.247/sensor/sensorstream";
    private int mWidth = 1280;  //  预览分辨率
    private int mHeight = 720;
    private String TAG = "RTMP";

    public Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case SUCCESS:
                    String text = (String)msg.obj;
                    Log.i("success",text +": 发送成功");
//                    Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    break;
                case FALL:
                    String text1 = (String)msg.obj;
                    Log.i("success",text1 +": 发送失败"); // 标注每个client发送信息的提示是为了检测发送信息过程中的谁发送失败
                    Toast.makeText(MainActivity.this, "网络走丢啦", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private RadioGroup rg;
    private ConstraintLayout consSensor;
    private ConstraintLayout consVideo;
    private ConstraintLayout consWorker;
    List<Worker> workers;
    List<Sensor> sensors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);  //  响应屏幕旋转事件
        requestPermission();
    }

    private void init() {
        rg = (RadioGroup) findViewById(R.id.RG);
        consSensor = (ConstraintLayout)findViewById(R.id.ConsSensor);
        consWorker = (ConstraintLayout)findViewById(R.id.ConsWorker);
        consVideo = (ConstraintLayout)findViewById(R.id.ConsVideo);
        workers = getBeginW();
        sensors = getBeginS();
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.RadioSensor){
                    consWorker.setVisibility(View.GONE);
                    consSensor.setVisibility(View.VISIBLE);
                    bt_sensor = (Button)findViewById(R.id.ButtonSensor);
                    bt_sensor.setOnClickListener(new sensorFlow());
                    rtmpUrl = "rtmp://120.77.243.247/sensor/sensorstream";
                }else {
                    consSensor.setVisibility(View.GONE);
                    consWorker.setVisibility(View.VISIBLE);
                    bt_worker = (Button)findViewById(R.id.ButtonWorker);
                    bt_worker.setOnClickListener(new workerFlow());
                    rtmpUrl = "rtmp://120.77.243.247/worker/workerstream";
                }
                consVideo.setVisibility(View.VISIBLE);
            }
        });

        bt_publish =findViewById(R.id.publish);
        bt_SwitchCamera = (Button)findViewById(R.id.swCam);
        bt_pause = findViewById(R.id.pause);
        bt_pause.setEnabled(false);
        mCameraView = (SrsCameraView)findViewById(R.id.glsurfaceview_camera);
        mPublisher = new SrsPublisher(mCameraView);
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));  // 编码状态回调
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(mWidth,mHeight);
        mPublisher.setOutputResolution(mHeight,mWidth);
        mPublisher.setVideoHDMode();
        mPublisher.startCamera();

        mCameraView.setCameraCallbacksHandler(new SrsCameraView.CameraCallbacksHandler(){
            @Override
            public void onCameraParameters(Camera.Parameters params) { }
        });

        bt_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt_publish.getText().toString().contentEquals("publish")){
                    mPublisher.startCamera();
                    mPublisher.switchToHardEncoder(); // 选择硬编码
                    mPublisher.startPublish(rtmpUrl); // 开始推流 ip应当为部署的ip "rtmp://192.168.31.126/android/teststream"
                    bt_publish.setText("stop");
                    bt_pause.setEnabled(true);
                }else{
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();
                    bt_publish.setText("publish");
                    bt_pause.setEnabled(false);
                }
            }
        });
        bt_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt_pause.getText().toString().equals("Pause")){
                    mPublisher.pausePublish();
                    bt_pause.setText("resume");
                }else{
                    mPublisher.resumePublish();
                    bt_pause.setText("Pause");
                }
            }
        });
        bt_SwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublisher.switchCameraFace((mPublisher.getCameraId()+1)% Camera.getNumberOfCameras());
            }
        });
    }

    private class sensorFlow implements View.OnClickListener {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            if(flagS){
                flagS = false;
                bt_sensor.setTextColor(black);
                new Thread(new SensorThread()).start();
            }else {
                flagS = true;
                bt_sensor.setTextColor(0xFFFFFFFF);
            }
        }
    }

    private class workerFlow implements View.OnClickListener {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            if (flagW){
                flagW = false;
                bt_worker.setTextColor(black);
                new Thread(new WorkerThread()).start();
            }else {
                flagW = true;
                bt_worker.setTextColor(0xFFFFFFFF);
            }
        }
    }

    class SensorThread implements Runnable {
        @Override
        public void run() {

            String urlSensorServer = "http://120.77.243.247/MonitorServer/servlet/UploadSensorData";
            while(!flagS){
                try {
                    getProcessS(sensors);
                    int i = 0;
                    for(; i < sensors.size(); i++){
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("IdTime",getTime(sensors.get(i).id))
                               .put("Id",sensors.get(i).id)
                               .put("Destx",sensors.get(i).locx)
                               .put("Desty",sensors.get(i).locy)
                               .put("TotalDust",sensors.get(i).TotalDust)
                               .put("BreatheDust",sensors.get(i).BreatheDust)
                               .put("Temp",sensors.get(i).Temp)
                               .put("Humidy",sensors.get(i).Humidity);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        String who = "Sensor" + String.valueOf(i+1);
                        sendInfo(urlSensorServer,obj,who);
                    }
                    Thread.sleep(10*1000); // 10s线程休眠 重复发送
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }


    class WorkerThread implements Runnable {
        @Override
        public void run() {

            String urlserver = "http://120.77.243.247/MonitorServer/servlet/UploadWorkerData";
            while(!flagW){
                try {
                    getProcessW(workers);
                    for (int i = 0; i < workers.size();i++){
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("IdTime",getTime(workers.get(i).id))
                                    .put("Id",workers.get(i).id)
                                    .put("Destx",workers.get(i).locx)
                                    .put("Desty",workers.get(i).locy);
                            Log.e("place",workers.get(i).locx+";"+workers.get(i).locy);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        String who = "Worker" + (i+1);
                        sendInfo(urlserver,obj,who);
                    }
                    Thread.sleep(10*1000); // 10s线程休眠 重复发送
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendInfo(String urlServer ,JSONObject obj, String who) {
        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody requestBody = RequestBody.create(type,""+obj.toString());
        OkHttpClient clientHttp = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(urlServer)
                .post(requestBody)
                .build();
        Call call = clientHttp.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = Message.obtain();
                msg.what = FALL;
                msg.obj = who;
                handler.sendMessage(msg);
//                handler.sendEmptyMessage(FALL);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.toString();
                Message msg = Message.obtain();
                msg.what = SUCCESS;
                msg.obj = who;
//                msg.obj = string;
                handler.sendMessage(msg);
            }
        });
    }

    private void requestPermission() {
        //1. 检查是否已经有该权限
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            //2. 权限没有开启，请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_CAMERA);
        }else{
            //权限已经开启，做相应事情
            isPermissionGranted = true;
            init();
        }
    }

    //3. 接收申请成功或者失败回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限被用户同意,做相应的事情
                isPermissionGranted = true;
                init();
            } else {
                //权限被用户拒绝，做相应的事情
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mPublisher.getCamera() == null && isPermissionGranted){
            //if the camera was busy and available again
            mPublisher.startCamera();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        final Button btn = (Button) findViewById(R.id.publish);
        btn.setEnabled(true);
        mPublisher.resumeRecord();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.stopEncode();
        mPublisher.stopRecord();
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (bt_publish.getText().toString().contentEquals("stop")){
            mPublisher.startEncode();
        }
        mPublisher.startCamera();
    }

    private void handleException(Exception e) {
        try {
            Log.e("handle exception", e.getMessage());
            mPublisher.stopPublish();
            mPublisher.stopRecord();
            bt_publish.setText("publish");
        } catch (Exception e1) {
            //
        }
    }

       //  实现 Srsrtmplistener
    @Override
    public void onRtmpConnecting(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRtmpConnected(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRtmpVideoStreaming() {}
    @Override
    public void onRtmpAudioStreaming() {}
    @Override
    public void onRtmpStopped() {
        Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRtmpDisconnected() {
        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.i(TAG, String.format("Output Fps: %f", fps));
    }
    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Video bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Video bitrate: %d bps", rate));
        }
    }
    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        int rate = (int) bitrate;
        if (rate / 1000 > 0) {
            Log.i(TAG, String.format("Audio bitrate: %f kbps", bitrate / 1000));
        } else {
            Log.i(TAG, String.format("Audio bitrate: %d bps", rate));
        }
    }
    @Override
    public void onRtmpSocketException(SocketException e) { handleException(e); }
    @Override
    public void onRtmpIOException(IOException e) {handleException(e);}
    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) { handleException(e); }
    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) { handleException(e);}

    // 实现SrsEncoderHandler
    @Override
    public void onNetworkWeak() {
        Toast.makeText(getApplicationContext(), "Network weak", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onNetworkResume() {
        Toast.makeText(getApplicationContext(), "Network resume", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {  handleException(e); }

    // Implementation of SrsRecordHandler.
    @Override
    public void onRecordPause() {
        Toast.makeText(getApplicationContext(), "Record paused", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRecordResume() {
        Toast.makeText(getApplicationContext(), "Record resumed", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRecordStarted(String msg) {
        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRecordFinished(String msg) {
        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {handleException(e);}
    @Override
    public void onRecordIOException(IOException e) {handleException(e);}
}