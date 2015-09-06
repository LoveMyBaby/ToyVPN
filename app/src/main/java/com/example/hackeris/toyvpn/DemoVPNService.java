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
            ByteBuffer packet = ByteBuffer.allocate(32767);

            while (true) {

                int length = in.read(packet.array());
                if (length > 0) {
                    packet.limit(length);
                    Log.d(TAG, new String(packet.array(), "UTF-8"));
                    packet.clear();
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
}
