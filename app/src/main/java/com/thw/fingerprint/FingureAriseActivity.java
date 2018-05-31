package com.thw.fingerprint;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.KeyGenerator;

import com.thw.fingerprint.callback.CryptoObjectHelper;
import com.thw.fingerprint.callback.MyAuthCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.keystore.KeyProperties;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author tianhongwei
 * @version 2.0
 * @describe
 * @since 2018/5/31 14:39
 */
public class FingureAriseActivity extends Activity {

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private Handler handler;
    private FingerprintManagerCompat fingerprintManager;
    private MyAuthCallback myAuthCallback;
    private CancellationSignal cancellationSignal;
    private Dialog dialog;


    public static final int MSG_AUTH_SUCCESS = 100;
    public static final int MSG_AUTH_FAILED = 101;
    public static final int MSG_AUTH_ERROR = 102;
    public static final int MSG_AUTH_HELP = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingure_arise);

        initFingure();

    }


    private void initFingure() {
        fingerprintManager = FingerprintManagerCompat.from(this);
        if (!fingerprintManager.isHardwareDetected()) {
            // no fingerprint sensor is detected, show dialog to tell user.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("没有指纹传感器");
            builder.setMessage("您的设备上没有指纹传感器，点击取消退出");
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setCancelable(false);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // show this dialog.
            builder.create().show();
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            // no fingerprint image has been enrolled.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("没有指纹登记");
            builder.setMessage("没有指纹登记，请到设置->安全中设置");
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setCancelable(false);
            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            // show this dialog
            builder.create().show();
        } else {
            try {
                setFingurePws();
                initFingureDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void setFingurePws() {
        // TODO Auto-generated method stub
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStroe");
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {

            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStroe");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

//                Log.e(TAG, "msg: " + msg.what + " ,arg1: " + msg.arg1);
                switch (msg.what) {
                    case MSG_AUTH_SUCCESS:
                        setResultInfo("指纹识别");
                        cancellationSignal = null;
                        break;
                    case MSG_AUTH_FAILED:
                        setResultInfo("指纹识别失败");
                        cancellationSignal = null;
                        break;
                    case MSG_AUTH_ERROR:
                        handleErrorCode(msg.arg1);
                        break;
                    case MSG_AUTH_HELP:
                        handleHelpCode(msg.arg1);
                        break;
                }
            }
        };

        myAuthCallback = new MyAuthCallback(handler);

        try {

            CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
            if (cancellationSignal == null) {

                cancellationSignal = new CancellationSignal();
            }

            fingerprintManager.authenticate(cryptoObjectHelper.buildCryptoObject(), 0, cancellationSignal, myAuthCallback, null);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(FingureAriseActivity.this, "指纹识别初始化失败，请重试！", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorCode(int code) {

        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                setResultInfo("切换手势解锁");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                setResultInfo("硬件不可用，请稍后再试");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                setResultInfo("locked");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                setResultInfo("无法完成操作，因为还没有足够的存储空间来完成操作");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                setResultInfo("操作超时，请重试");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                setResultInfo("指纹图像无法识别，请重试");
                break;
        }
    }

    private void setResultInfo(String stringId) {
        if ("指纹识别".equals(stringId)) {
            Toast.makeText(FingureAriseActivity.this, "已解锁", Toast.LENGTH_SHORT).show();
            finish();
        } else if ("locked".equals(stringId)) {
//        	Log.i("TAG--->", "指纹传感器被锁");
            Toast.makeText(getApplicationContext(), "识别错误次数太多,已切换到手势", Toast.LENGTH_SHORT).show();
            if (cancellationSignal != null) {

                cancellationSignal.cancel();
                cancellationSignal = null;
            }
            dialog.dismiss();

        } else {
            Toast.makeText(FingureAriseActivity.this, stringId, 0).show();
//        	ShowToast.showToast(FingureAriseActivity.this, stringId);
        }


    }


    private void handleHelpCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ACQUIRED_GOOD:
                setResultInfo("获得的图像是好的");
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                setResultInfo("由于传感器上的可疑或检测到的灰尘，指纹图像太嘈杂了。");
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
                setResultInfo("指纹图像太嘈杂了");
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
                setResultInfo("请紧按指纹传感器");
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
                setResultInfo("手指移动过快");
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
                setResultInfo("请移动手指");
                break;
        }
    }


    /**
     * 弹出指纹识别的弹框
     */
    private void initFingureDialog() {

        dialog = new Dialog(FingureAriseActivity.this, R.style.customDialog);
        dialog.setContentView(R.layout.zhiwen_customdialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView tv_gesture_lock = (TextView) dialog.findViewById(R.id.tv_gesture_lock);
        tv_gesture_lock.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (cancellationSignal != null) {
                    cancellationSignal.cancel();
                    cancellationSignal = null;
                }
                dialog.dismiss();

            }
        });
        TextView tv_zhiwen_detail = (TextView) dialog.findViewById(R.id.tv_zhiwen_detail);
        tv_zhiwen_detail.setText("DEMO的 Touch ID" + "\n" + "请验证已有指纹");

        dialog.show();


    }

}