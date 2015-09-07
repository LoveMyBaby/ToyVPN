package com.example.hackeris.toyvpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hackeris on 15/9/6.
 */
public class DemoVPNService extends VpnService implements Handler.Callback, Runnable {

    private static final String TAG = "DemoVPNService";

    private Handler mHandler;

    private Thread mThread;

    private ParcelFileDescriptor mInterface;

    private FileInputStream mInputStream;

    private FileOutputStream mOutputStream;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        if (mThread != null) {
            mThread.interrupt();
        }

        if (mInterface != null) {
            try {
                mInterface.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mThread = new Thread(this);
        mThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        stopVPNService();
    }

    public void stopVPNService() {

        if (mThread != null) {
            mThread.interrupt();
        }
        try {
            if (mInterface != null) {
                mInterface.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        mThread = null;
        mInterface = null;
        mInputStream = null;
        mOutputStream = null;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {

        mHandler.sendEmptyMessage(R.string.connecting);

        try {
            VpnService.Builder builder = new VpnService.Builder();
            builder.addAddress("10.0.0.2", 32).addRoute("0.0.0.0", 0).setSession("DemoVPN").addDnsServer("8.8.8.8")
                    .setMtu(1500);

            mInterface = builder.establish();

            mInputStream = new FileInputStream(mInterface.getFileDescriptor());
            mOutputStream = new FileOutputStream(mInterface.getFileDescriptor());

            mHandler.sendEmptyMessage(R.string.connected);

            // Allocate the buffer for a single packet.
            ByteBuffer packet = ByteBuffer.allocate(32767);
            while (true) {

                int length = mInputStream.read(packet.array());
                if (length > 0) {
                    NetworkUtils.logIPPack(TAG, packet, length);
                }
            }

        } catch (IOException ie) {
            Log.e(TAG, ie.toString());
        } finally {
            mHandler.sendEmptyMessage(R.string.disconnected);
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new VPNServiceBinder();
    }

    public class VPNServiceBinder extends Binder {

        public DemoVPNService getService() {

            return DemoVPNService.this;
        }
    }
}
