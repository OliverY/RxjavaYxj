package com.yxj.rxjavayxj.test;

import android.util.Log;

import com.yxj.rxjavayxj.rxjava.Downstream;
import com.yxj.rxjavayxj.rxjava.Function;
import com.yxj.rxjavayxj.rxjava.Upstream;

/**
 * Author:  Yxj
 * Time:    2019/3/22 下午3:21
 * -----------------------------------------
 * Description:
 */
public class Rxtest {
    private static final String TAG = Rxtest.class.getSimpleName();

    /*
    1.请求服务器，获取该定位对应的unionId
    2.然后根据网络请求的结果做登录操作
    3.根据登录返回的信息做 IM的注册操作
     */

    public void init(){
        getLocation()
                .flatMap(new Function<LocationData, Upstream<LoginData>>() {
                    @Override
                    public Upstream<LoginData> apply(LocationData locationData) throws Exception {
                        String unionId = locationData.unionId;
                        Log.e(TAG,"获取到unionId="+unionId);
                        return login("yxj","123456",unionId);
                    }
                })
                .flatMap(new Function<LoginData, Upstream<IMLoginData>>() {
                    @Override
                    public Upstream<IMLoginData> apply(LoginData loginData) throws Exception {
                        String imToken = loginData.imToken;
                        Log.e(TAG,"登录成功，获取到imToken="+imToken);
                        return imLogin(loginData.imToken);
                    }
                })
                .subscribe(new Downstream<IMLoginData>() {
                    @Override
                    public void onNext(IMLoginData imLoginData) {
                        boolean isSuccess = imLoginData.isSuccess;
                        if(isSuccess){
                            Log.e(TAG,"IM登录成功");
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 从服务器获取 定位数据
     *
     * @return
     */
    public Upstream<LocationData> getLocation() {

        return Upstream.createUpstream(new Upstream<LocationData>() {

            @Override
            public void subscribe(Downstream<LocationData> downstream) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                downstream.onNext(new LocationData("杭州", "100"));
            }
        });
    }

    /**
     * 登录
     *
     * @param name
     * @param pwd
     * @param unionId
     */
    public Upstream<LoginData> login(final String name, final String pwd, final String unionId) {

        return Upstream.createUpstream(new Upstream<LoginData>() {
            @Override
            public void subscribe(Downstream<LoginData> downstream) {
                // 模拟从服务器查找数据 2s
                if (name.equals("yxj") && pwd.equals("123456") && unionId.equals("100")) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    downstream.onNext(new LoginData(true, "akdfajkfasfkjqfafkjwehfksajsf"));
                }
            }
        });
    }

    /**
     * IM 登录
     *
     * @param imToken
     */
    public Upstream<IMLoginData> imLogin(final String imToken) {

        return Upstream.createUpstream(new Upstream<IMLoginData>() {
            @Override
            public void subscribe(Downstream<IMLoginData> downstream) {
                if(imToken.equals("akdfajkfasfkjqfafkjwehfksajsf")){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    downstream.onNext(new IMLoginData(true));
                }

            }
        });
    }

}
