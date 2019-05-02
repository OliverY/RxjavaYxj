package com.yxj.rxjavayxj.api;

import com.yxj.rxjavayxj.myrxjava.Downstream;
import com.yxj.rxjavayxj.myrxjava.Upstream;
import com.yxj.rxjavayxj.rxjava.Observable;
import com.yxj.rxjavayxj.rxjava.Observer;

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
    public static Observable<String> login(String userName, String password){
        return Observable.create(new Observable<String>() {
                    @Override
                    public void subscribe(Observer<String> observer) {
                        try {
                            /*
                            模拟网络访问
                            这里你可以把sleep替换成 okhttp、httpclient的网络访问
                            在获得结果时调用observer.onNext()
                            把结果放入onNext当中
                             */

                            Thread.sleep(3000);
                            observer.onNext("登录成功");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            //observer.onError(new Exception("登录失败"));
                        }
                    }
                });
    }


    public interface Callback {
        void onSuccess(String result);

        void onError(String error);
    }

}
