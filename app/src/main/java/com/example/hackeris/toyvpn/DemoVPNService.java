package com.example.hackeris.toyvpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        if (mThread != null) {
            mThread.interrupt();
        }

        mThread = new Thread(this);
        mThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {

        try {
            VpnService.Builder builder = new VpnService.Builder();
            builder.addAddress("10.0.8.1", 32).addRoute("0.0.0.0", 0).setSession("Firewall").addDnsServer("8.8.8.8")
                    .setMtu(1500);

            ParcelFileDescriptor mInterface = builder.establish();

            FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(mInterface.getFileDescriptor());

            // Allocate the buffer for a single packet.
            byte[] packet = new byte[32767];

            while (true) {

                int length = in.read(packet);
                if (length > 0) {
                    Log.i(TAG, "\n" + byteArrayToHexString(packet));
                }
            }

        } catch (IOException ie) {
            Log.e(TAG, ie.toString());
        } finally {
            mHandler.sendEmptyMessage(R.string.disconnected);
        }
    }

    private String byteArrayToHexString(byte[] src) {

        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            stringBuilder.append(' ');
            if (hv.length() < 2) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();

    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
