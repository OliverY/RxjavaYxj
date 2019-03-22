//package com.yxj.rxjavayxj.test;
//
//import com.yxj.rxjavayxj.rxjava.Downstream;
//import com.yxj.rxjavayxj.rxjava.Function;
//import com.yxj.rxjavayxj.rxjava.Upstream;
//
///**
// * Author:  Yxj
// * Time:    2019/3/22 下午3:21
// * -----------------------------------------
// * Description:
// */
//public class Rxtest {
//
//    /*
//    1.请求服务器，获取该定位对应的unionId
//    2.然后根据网络请求的结果做登录操作
//    3.根据登录返回的信息做 IM的注册操作
//     */
//
//    public static void main(String[] args) {
//        Rxtest test = new Rxtest();
//
//        test.init();
//    }
//
//    public void init(){
//        getLocation()
//                .map(new Function<LocationData, LoginData>() {
//                    @Override
//                    public LoginData apply(LocationData locationData) throws Exception {
//
//                        login("yxj","123456",locationData.unionId);
//
//                        return null;
//                    }
//                })
//    }
//
//    /**
//     * 从服务器获取 定位数据
//     *
//     * @return
//     */
//    public Upstream<LocationData> getLocation() {
//
//        return Upstream.createUpstream(new Upstream<LocationData>() {
//
//            @Override
//            public void subscribe(Downstream<LocationData> downstream) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                downstream.onNext(new LocationData("杭州", "100"));
//            }
//        });
//    }
//
//    /**
//     * 登录
//     *
//     * @param name
//     * @param pwd
//     * @param unionId
//     */
//    public Upstream<LoginData> login(final String name, final String pwd, final String unionId) {
//
//        return Upstream.createUpstream(new Upstream<LoginData>() {
//            @Override
//            public void subscribe(Downstream<LoginData> downstream) {
//                // 模拟从服务器查找数据 2s
//                if (name.equals("yxj") && pwd.equals("123456") && unionId.equals("100")) {
//
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    downstream.onNext(new LoginData(true, "akdfajkfasfkjqfafkjwehfksajsf"));
//                }
//            }
//        });
//    }
//
//    /**
//     * IM 登录
//     *
//     * @param imToken
//     */
//    public Upstream<IMLoginData> imLogin(final String imToken) {
//
//        return Upstream.createUpstream(new Upstream<IMLoginData>() {
//            @Override
//            public void subscribe(Downstream<IMLoginData> downstream) {
//                if(imToken.equals("akdfajkfasfkjqfafkjwehfksajsf")){
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    downstream.onNext(new IMLoginData(true));
//                }
//
//            }
//        });
//    }
//
//}
