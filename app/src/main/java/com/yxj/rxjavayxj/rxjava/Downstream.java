package com.yxj.rxjavayxj.rxjava;

/**
 * Author:  Yxj
 * Time:    2019/3/15 上午9:50
 * -----------------------------------------
 * Description: 下游处理 相当于Observer
 */
public interface Downstream<T> {

    void onNext(T t);
    void onComplete();

}
