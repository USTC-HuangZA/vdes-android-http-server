/**************************************************
 * Android Web Server
 * Based on JavaLittleWebServer (2008)
 * <p/>
 * Copyright (c) Piotr Polak 2008-2017
 **************************************************/

package ro.polak.webserver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;

import java.util.Arrays;
import java.util.Set;

import configs.DataCenterConfig;
import configs.LogConfig;
import ro.polak.webserver.base.BaseMainActivity;
import ro.polak.webserver.base.BaseMainService;
import ro.polak.webserver.webserver.R;

/**
 * The main server Android activity.
 *
 * @author Piotr Polak piotr [at] polak [dot] ro
 * @since 201008
 */
public class MainActivity extends BaseMainActivity {

    private TextView status;
    private TextView ipText;
    private TextView consoleText;
    private Button actionButton;
    private Button backgroundButton;
    private Button requestPermissionsButton;
    private Button quitButton;
    private ImageView imgView;

    private void serviceInit(){
        //启动服务的时候把logger初始化
        LogConfig.LoggerInit(getApplicationContext());
        println("logger init done");
        DataCenterConfig.singletonInit(getApplicationContext(), this);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doOnCreate() {

        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.ImageView01);
        status = findViewById(R.id.TextView01);
        ipText = findViewById(R.id.TextView02);
        consoleText = findViewById(R.id.textView1);
        actionButton = findViewById(R.id.Button01);
        actionButton.setOnClickListener(new ButtonListener(this));

        backgroundButton = findViewById(R.id.Button02);
        backgroundButton.setOnClickListener(new ButtonListener(this));

        quitButton = findViewById(R.id.Button03);
        quitButton.setOnClickListener(new ButtonListener(this));

        requestPermissionsButton = findViewById(R.id.Button04);
        requestPermissionsButton.setOnClickListener(new ButtonListener(this));

        serviceInit();
        status.setText("初始化中");

    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected Class<MainService> getServiceClass() {
        return MainService.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRequestPermissions() {
        status.setText("请求权限");
        actionButton.setVisibility(View.GONE);
        backgroundButton.setVisibility(View.GONE);
        ipText.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doShowMustAcceptPermissions() {
        status.setText("无法初始化，缺少权限");
        requestPermissionsButton.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doNotifyStateChangedToOffline() {
        imgView.setImageResource(R.drawable.offline);
        status.setText("服务下线");
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText("服务启动");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doNotifyStateChangedToOnline(final BaseMainService.ServiceStateDTO serviceStateDTO) {
        ipText.setText(serviceStateDTO.getAccessUrl());

        imgView.setImageResource(R.drawable.online);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setText("停止服务");
        status.setText("服务在线");
    }

    /**
     * 打印日志，同时限制长度
     */
    @Override
    public void println(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String newText = text + "\n" + consoleText.getText();
                    String[] splitText = newText.split("\n");
                    if(splitText.length > LogConfig.MAX_PRINT_LINES){
                        splitText = Arrays.copyOfRange(splitText, splitText.length - LogConfig.MAX_PRINT_LINES, splitText.length);
                    }
                    consoleText.setText(String.join("\n", splitText));
                    if(LogConfig.LoggerInitDone){
                        XLog.i(text);
                    }else {
                        Log.i("OK", text);
                    }
                } catch (Exception e) {
                    Log.i("ERROR", text);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doOnPermissionsAccepted() {
        backgroundButton.setVisibility(View.VISIBLE);
        ipText.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    protected Set<String> getRequiredPermissions() {
        Set<String> permissions = super.getRequiredPermissions();
//      Other permissions
//      permissions.add(Manifest.permission.READ_SMS);
//      permissions.add(Manifest.permission.SEND_SMS);

        return permissions;
    }

    /**
     * Button listener for the move to background and exit action.
     */
    private class ButtonListener implements View.OnClickListener {

        private MainActivity activity;

        ButtonListener(final MainActivity activity) {
            this.activity = activity;
        }

        public void onClick(final View v) {
            int id = v.getId();

            if (id == requestPermissionsButton.getId()) {
                requestPermissions();
                return;
            } else if (id == backgroundButton.getId()) {
                moveTaskToBack(true);
                return;
            } else if (id == quitButton.getId()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("确定退出?")
                        .setCancelable(false)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                if (isMainServiceBound()) {
                                    requestServiceStop();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "后台服务未绑定", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();

            } else if (id == actionButton.getId()) {
                if (isMainServiceBound()) {
                    if (getMainService().getServiceState().isWebServerStarted()) {
                        getMainService().getController().stop();
                        XLog.i("service end");
                    } else {
                        getMainService().getController().start();
                        XLog.i("service start");
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "后台服务未绑定", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
