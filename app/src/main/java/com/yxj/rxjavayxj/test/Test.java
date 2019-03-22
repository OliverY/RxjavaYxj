package com.yxj.rxjavayxj.test;

/**
 * Author:  Yxj
 * Time:    2019/3/22 下午1:41
 * -----------------------------------------
 * Description:
 */
public class Test {

    /*
    1.请求服务器，获取该定位对应的unionId
    2.然后根据网络请求的结果做登录操作
    3.根据登录返回的信息做 IM的注册操作
     */

    public static void main(String[] args){
        Test test = new Test();

        test.init();
    }

    public void init() {

        getLocation(new Callback<LocationData>(){

            @Override
            public void onSuccess(final LocationData location) {
                String unionId = location.unionId;

                System.out.println("获取到unionId="+unionId);

                login("yxj", "123456", unionId, new Callback<LoginData>() {
                    @Override
                    public void onSuccess(LoginData loginData) {
                        String imToken = loginData.imToken;

                        System.out.println("登录成功，获取到imToken="+imToken);
                        imLogin(imToken, new Callback<IMLoginData>() {
                            @Override
                            public void onSuccess(IMLoginData imLoginData) {
                                boolean isSuccess = imLoginData.isSuccess;

                                if(isSuccess){
                                    System.out.println("IM登录成功");
                                }
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    /**
     * 从服务器获取 定位数据
     * @param callback
     */
    public void getLocation(final Callback<LocationData> callback){

        new Thread(){
            @Override
            public void run() {
                // 模拟从服务器查找数据 2s
                try {
                    Thread.sleep(2000);
                    callback.onSuccess(new LocationData("杭州","100"));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * 登录
     * @param name
     * @param pwd
     * @param unionId
     * @param callback
     */
    public void login(String name,String pwd,String unionId,final Callback<LoginData> callback){
        new Thread(){
            @Override
            public void run() {
                // 模拟从服务器查找数据 2s
                try {
                    Thread.sleep(2000);
                    callback.onSuccess(new LoginData(true,"akdfajkfasfkjqfafkjwehfksajsf"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * IM 登录
     * @param imToken
     * @param callback
     */
    public void imLogin(String imToken,final Callback<IMLoginData> callback){
        new Thread(){
            @Override
            public void run() {
                // 模拟从服务器查找数据 2s
                try {
                    Thread.sleep(2000);
                    callback.onSuccess(new IMLoginData(true));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 回调接口
     * @param <T>
     */
    interface Callback<T>{
        void onSuccess(T t);
        void onError(Exception e);
    }

}
