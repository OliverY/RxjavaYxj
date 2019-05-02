package com.yxj.rxjavayxj.rxjava;

/**
 * Author:  Yxj
 * Time:    2019/4/30 上午9:21
 * -----------------------------------------
 * Description:
 */
public interface Observer<T> {

    void onNext(T t);
    void onComplete();
}
