package com.yxj.rxjavayxj.rxjava;

import android.os.Handler;

/**
 * Author:  Yxj
 * Time:    2019/3/15 上午9:50
 * -----------------------------------------
 * Description: 上游 相当于Observable
 */
public abstract class Upstream<T>{

    public abstract void subscribe(Downstream<T> downstream);

    public static <T> Upstream<T> createUpstream(final Upstream<T> source){
        return new Upstream<T>(){
            @Override
            public void subscribe(Downstream<T> downFlow) {
                source.subscribe(downFlow);
            }
        };
    }

    public <R> Upstream<R> map(final Function<T,R> function){

        return new Upstream<R>(){// 创建了一个新的 上游
            @Override
            public void subscribe(final Downstream<R> downFlow) {
                // 原来的上游 subscribe 新的下游
                Upstream.this.subscribe(new Downstream<T>() {// 创建了一个新的 下游
                    @Override
                    public void onNext(T t) {
                        try {
                            R r = function.apply(t);
                            downFlow.onNext(r);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        };
    }

    /**
     * 改变上游所在的线程
     * @return
     */
    public Upstream<T> subscribeOnNewThread(){
        return new Upstream<T>() {
            @Override
            public void subscribe(final Downstream<T> downFlow) {

                new Thread(){
                    @Override
                    public void run() {
                        Upstream.this.subscribe(downFlow);
                    }
                }.start();

            }
        };
    }

    /**
     * 改变下游所在的线程，切换到主线程
     * @return
     */
    public Upstream<T> observeOnMainThread(){
        return new Upstream<T>() {
            @Override
            public void subscribe(final Downstream<T> downFlow) {

                Upstream.this.subscribe(new Downstream<T>() {
                    @Override
                    public void onNext(final T t) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downFlow.onNext(t);
                            }
                        });

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        };
    }

    /**
     * 改变下游所在的线程，在新的线程
     * @return
     */
    public Upstream<T> observeOnNewThread(){
        return new Upstream<T>() {
            @Override
            public void subscribe(final Downstream<T> downFlow) {

                Upstream.this.subscribe(new Downstream<T>() {
                    @Override
                    public void onNext(final T t) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downFlow.onNext(t);
                            }
                        });

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        };
    }

    /**
     * compose 连接上下游
     *
     * 这里固定写成了 上游切换至 新线程，下游切换至 主线程
     * @return
     */
    public Upstream<T> compose(){
        return Upstream.this.subscribeOnNewThread().observeOnMainThread();
    }

    Handler handler = new Handler();

}
