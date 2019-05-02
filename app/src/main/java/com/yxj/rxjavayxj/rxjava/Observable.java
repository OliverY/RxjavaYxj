package com.yxj.rxjavayxj.rxjava;

import android.os.Handler;

/**
 * Author:  Yxj
 * Time:    2019/4/30 上午9:20
 * -----------------------------------------
 * Description:
 */
public abstract class Observable<T> {

    public abstract void subscribe(Observer<T> observer);

    public static <T> Observable<T> create(Observable<T> observable){
        return observable;
    }

    // 节点A调用map操作符
    public Observable<T> map(){

        /*
         以下是rxjava变换的关键，看明白这个
         剩下的subscribeOn，observeOn，flatMap，compose就都无压力了
         其它的操作符都是在这个基础上修改而来
          */
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observerC) {// 节点B的处理者observerC（为什么不叫observerB，别着急，接着看）

                // 节点B在subscribe方法内部创建了一个新的处理者observerB
                Observer<T> observerB = new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        /*
                         让处理者observerB来做变换
                         并且把变换结果交给observerC
                         （这时候observerC还不一定存在，
                         只有当节点B调用了subscribe方法，observerB才被new出来。
                         所以如果没有observerC，那observerB还会不会存在呢？）
                         */
                        observerC.onNext(t);
                    }

                    @Override
                    public void onComplete() {
                        observerC.onComplete();
                    }
                };
                // 节点A把自己的处理者B交给了节点A的subscribe方法
                Observable.this.subscribe(observerB); // Observable.this == 节点A，即调用map方法的Observable，也就是上一个节点
            }
        };
    }

    public <R> Observable<R> map(final Function<T,R> function){

        // 以下是rxjava变换的关键，看明白这个，剩下的subscribeOn，observeOn，flatMap，compose就都无压力了。
        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer1) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        R r = function.apply(t);
                        observer1.onNext(r);
                    }

                    @Override
                    public void onComplete() {
                        observer1.onComplete();
                    }
                });
            }
        };
    }

    /**
     * 改变下个节点运行的线程，在新的线程
     *
     * 我们常用的observerOn(AndroidScheduler.mainThread())
     * 其实AndroidScheduler.mainThread()就是一个线程调度器，内部含有了线程池，切换到主线程无非就是内部含有一个主线程的handler
     * 所以这里为了代码的简单，我直接把线程写在方法内部
     *
     * @return
     */
    public Observable<T> observeOn(){
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                // 与“空map”相比，在这里开了一个线程，把onNext方法调用放到了子线程中
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(final T t) {
//                        new Thread(){
//                            @Override
//                            public void run() {
//                                observer.onNext(t);
//                            }
//                        }.start();

//                         模拟切换到主线程（通常上个节点是运行在子线程的情况）
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                observer.onNext(t);
                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        };
    }

    /**
     * 改变上个节点运行的线程
     * @return
     */
    public Observable<T> subscribeOn(){
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                /*
                还记得这里的Observable.this == 调用subscribeOn方法的 Observable对象吧！
                所以把subscribe方法调用放到子线程就切换了上一个节点的运行线程
                 */
                new Thread(){
                    @Override
                    public void run() {
                        Observable.this.subscribe(observer);
                    }
                }.start();
            }
        };
    }

    /**
     * flatMap
     * @param function
     * @param <R>
     * @return
     */
    public <R> Observable<R> flatMap(final Function<T,Observable<R>> function){

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> downstream) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        try {
                            Observable<R> upstream = function.apply(t);
                            upstream.subscribe(downstream);
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

    Handler handler = new Handler();
}
