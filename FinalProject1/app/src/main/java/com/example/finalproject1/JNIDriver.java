package com.example.finalproject1;

import android.util.Log;

public class JNIDriver implements JNIListener {

    private boolean mConnectFlag;

    private TranseThread mTranseThread;
    private JNIListener mMainActivity;

    static {
        System.loadLibrary("OpenCLDriver");
    }

    private native static int openDriverbu(String path);
    private native static void closeDriverbu();
    private native char readDriverbu();

    private native int getInterruptbu();

    public JNIDriver() {
        mConnectFlag = false;
    }

    @Override
    public void onReceivebu(int val) {
        Log.e("test", "4");
        if (mMainActivity != null) {
            mMainActivity.onReceivebu(val);
            Log.e("test", "2");
        }
    }

    public void setListener(JNIListener a) {
        mMainActivity = a;
    }

    public int open(String driver) {
        if (mConnectFlag) return -1;

        if (openDriverbu(driver) > 0) {
            mConnectFlag = true;
            mConnectFlag = true;
            mTranseThread = new TranseThread();
            mTranseThread.start();
            return 1;
        } else {
            return -1;
        }
    }
    public void close(){
        if(!mConnectFlag) return;
        mConnectFlag = false;
        closeDriverbu();
    }
    protected void finalize() throws Throwable{
        close();
        super.finalize();
    }
    public char read() {return readDriverbu();}
    private class TranseThread extends Thread{
        @Override
        public void run(){
            super.run();
            try{
                while (mConnectFlag){
                    try{Log.e("test","1");
                        onReceivebu(getInterruptbu());
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
            }
        }
    }
}
