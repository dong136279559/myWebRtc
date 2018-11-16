package com.huawang.dongxiangjun.myapplication;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity implements IWebRTCHelper{


    private WebRTCHelper helper;

    private Map<String,VideoTrack> _remoteVideoTracks = new HashMap();
    private Map<String,VideoRenderer.Callbacks> _remoteVideoView = new HashMap();

    private static int x ;
    private static int y ;

    private GLSurfaceView vsv;

    private VideoRenderer.Callbacks localRender;

    private  double width ;
    private  double height ;


    private VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //设置摄像头切换
        View btn = findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });


        // 设置宽高比例
        WindowManager manager = (WindowManager)getSystemService(WINDOW_SERVICE);
        width = manager.getDefaultDisplay().getWidth()/3.0;
        height = width * 32.0/24.0;


        x = 0;
        y = 70;


        vsv = (GLSurfaceView) findViewById(R.id.glview_call);
        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);

        ///surface准备好后会调用runnable里的run()函数
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {

                Log.i("com.huawang.www","surfaceView准备完毕");


                helper = new WebRTCHelper(Main2Activity.this);

                helper.initSocket("ws://192.168.8.190","3000");
            }
        });

        // local and remote render

        try {

            localRender = VideoRendererGui.create(
                    0, 0,
                    100 , 100, scalingType, true);

        }catch (Exception e){
            e.printStackTrace();
        }

    }



    @Override
    public void webRTCHelper_SetLocalStream(MediaStream stream, String userId) {

        Log.i("com.huawang.www","在本地添加视频");

        stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));

        VideoRendererGui.update(localRender,
                0, 0,
                100, 100,
                scalingType,false);
    }

    @Override
    public void webRTCHelper_AddRemoteStream(MediaStream stream, String userId) {

        Log.i("com.huawang.www","接受到远端视频流     "  + userId);

        _remoteVideoTracks.put(userId,stream.videoTracks.get(0));


        VideoRenderer.Callbacks  vr = VideoRendererGui.create(
                0, 0,
                0, 0, scalingType, false);


        _remoteVideoView.put(userId,vr);

        stream.videoTracks.get(0).addRenderer(new VideoRenderer(vr));
        VideoRendererGui.update(vr,
                x, y,
                30, x+30,
                scalingType,false);

        x += 30;
    }


    @Override
    public void webRTCHelper_CloseWithUserId(String userId) {
        Log.i("com.huawang.www","有用户离开    " + userId);


        VideoRenderer.Callbacks callbacks = _remoteVideoView.get(userId);
        VideoRendererGui.remove(callbacks);

        _remoteVideoTracks.remove(userId);
        _remoteVideoView.remove(userId);

        if (_remoteVideoTracks.size() == 0){
            x = 0;
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            // 退出房间
            helper.exitRoom();
            this.finish();

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 切换摄像头
    public void switchCamera() {

        helper.switchCamera();
    }

}
