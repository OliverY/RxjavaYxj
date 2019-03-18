package com.yxj.rxjavayxj.api;

import android.database.Observable;

import com.yxj.rxjavayxj.rxjava.Downstream;
import com.yxj.rxjavayxj.rxjava.Upstream;

/**
 * Author:  Yxj
 * Time:    2019/3/18 下午1:27
 * -----------------------------------------
 * Description:
 */
public class Api {

    /**
     * 原始的网络访问
     * @param userName
     * @param password
     * @param callback
     */
    public static void login(String userName, String password, final Callback callback) {

        new Thread() {
            @Override
            public void run() {
                try {
                    // 从服务器去获取数据，用sleep替代，真实情况用 HttpUrlConnection
                    Thread.sleep(3000);
                    // ...

                    callback.onSuccess("登录成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    callback.onError("登录失败");
                }
            }
        }.start();

    }

    /**
     * 用Rxjava做封装
     * @param userName
     * @param password
     * @return
     */
    public static Upstream<String> login(String userName, String password){
        return Upstream.createUpstream(new Upstream<String>() {
                    @Override
                    public void subscribe(Downstream<String> downstream) {
                        try {
                            Thread.sleep(3000);
                            downstream.onNext("登录成功");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            //downstream.onError(new Exception("登录失败"));
                        }
                    }
                }).compose();
    }


    public interface Callback {
        void onSuccess(String result);

        void onError(String error);
    }

}
