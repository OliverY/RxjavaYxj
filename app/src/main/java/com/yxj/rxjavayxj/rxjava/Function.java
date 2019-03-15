package com.yxj.rxjavayxj.rxjava;

/**
 * Author:  Yxj
 * Time:    2019/3/12 上午11:28
 * -----------------------------------------
 * Description:
 */
public interface Function<T,R> {

    R apply(T t) throws Exception;

}
