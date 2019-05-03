# RxjavaYxj

> 这应该是目前为止最简洁，最形象化的Rxjava核心源码讲解了，没有之一。理解9行核心代码，完全掌握Rxjava运行机制。

你是不是看过了很多分析Rxjava源码的文章，但依旧无法在心中无法勾勒出Rxjava运行原理的样貌。是什么让我们阅读Rxjava源码变得如此艰难呢？是Rxjava的代码封装。本文中我把Rxjava的各种封装、抽象统统剥去，让最纯粹的Rxjava就赤裸裸的站在你面前，让你想不要都难。

本文不是Rxjava的api教学贴，仅适合已经使用过Rxjava的同学。在开始之前，真心不用担心看不懂，首先核心部分的代码仅仅9行而已，其次我还画了图帮助大家理解Rxjava运作机制。同时非常建议新创建一个工程，手写或复制其中的代码运行一下，即可明白Rxjava是怎么一回事。

[简书](https://www.jianshu.com/p/ba1835f65f89)

---

#### 本文分为：
- 手写Rxjava核心代码，create，nullMap操作符
- map，observeOn，subscribeOn，flatMap操作符
- 响应式编程的思想

---
### 手写Rxjava核心代码，create，nullMap操作符


首先我们看一个最简单调用

```
MainActivity.java

Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e("yxj",s);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });
```
```
Observable.java

public abstract class Observable<T> {

    public abstract void subscribe(Observer<T> observer);

    public static <T> Observable<T> create(Observable<T> observable){
        return observable;
    }

}
```

```
Observer.java

public interface Observer<T> {

    void onNext(T t);
    void onComplete();
}
```
######  本篇文章我把Observable称为“节点”，Observer称为“处理者”，因为我被观察者、被观察者、谁订阅谁给绕晕了。

我们先来看看Observable类，create方法是一个工厂方法，创建一个自己的对象。subscribe方法是abstract的，在创建Observable对象时需要实现该方法的内容。

这个地方其实在说，Observable通过create方法创建了一个自己的实例对象，subscribe方法说 **如果** 我能找到处理者（Observer），那我就让处理者处理“hello”，“world”。
> 这里记住2个点：
**1.这里是“如果”有处理者，要subscribe方法被调用时，“如果”才成立。这一点很重要，因为Rxjava就是建立在一系列的“如果”（回调）操作上的。**
**2.节点（observable）不触碰具体业务，处理者（observer）负责数据、业务的具体流转操作**

#### “nullMap”操作符

```
nullMap操作符等价于 下面这段代码，即没有做任何附加的操作map()
上个节点给的是String，直接把String数据给到下一个节点
中间无任何转换
 
.map(new Function<String, String>() {
    @Override
    public String apply(String s) throws Exception {
        return s;
    }
})

```
首先**"nullMap"操作符在Rxjava源码里并不存在**，是我方便大家理解Rxjava运行机制写出来的。
空map操作是一个 base操作符，map，flatMap，subscribeOn，observeOn操作符都是基于nullMap修改而来。所以Rxjava真正的核心就是空map操作符，代码也就8行代码。

```
1.创建一个observable
2.调用空map操作符做变换
3.交给observer处理

MainActivity.java

Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .nullMap()
        .subscribe(new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e("yxj",s);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });

```

```
Observable.java
// 这就是Rxjava的核心

public Observable<T> nullMap() {

        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observerC) {

                Observer<T> observerB = new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        observerC.onNext(t);
                    }

                    @Override
                    public void onComplete() {
                        observerC.onComplete();
                    }
                };
                Observable.this.subscribe(observerB);
            }
        };
    }
```
###### 以上的nullMap()方法就是Rxjava的核心代码，看懂这个其它的操作符都是由他派生出来的。

简单讲一下“nullMap”操作符做了件什么事情：

1. 节点A调用map()在内部创建了一个新的节点B，把任务交给了新节点B。
2. 节点B需要考虑到“如果自己有操作者Observer C”这件事，于是重写自己的subscribe方法，方法内部重新创建了一个操作者Observer B，并且让节点A使用节点B内部创建出来的这个Observer B。
3. 操作者Observer B中，onNext方法内，让处理者Observer C来接收t。
从而完成了任务从节点A与节点B连接

此时，如果节点B如果调用了subscribe方法，就意味着有一个操作者Observer C，整个流程就跑通了。原来那些个“如果”的承诺就都变成了现实。要是节点B不subscribe，而是map，意味着再来一遍1，2，3步骤，连接到了下一个节点C。任务就一直流转，直到某个节点可以调用subscribe方法，把任务交给observer，完成整个任务流。

#### 详细讲一遍nullMap调用过程

##### 第一阶段：许下承诺
![part1.png](https://upload-images.jianshu.io/upload_images/1538674-b6f7819086421721.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### 注：虚线代表还未执行到，方法体或对象 仅存在于某个回调函数中，是一个“如果”。不好意思，图里面的Observable C应该改成 Observer C，下面的图也是。

步骤1红色部分

1. 节点A调用map方法，在内部创建了一个新的节点B（new出来的匿名Observable），把任务交给了新节点B。
2. 节点B这时候对节点A做了个承诺：**“如果”**我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”
3. 节点B中的操作者Observer B内部做了与Observer C的衔接工作（function操作）
###### 这一阶段：主要就是节点与节点之间做连接，之间有各种“如果”的承诺。


##### 第二阶段：兑现承诺

![part2.png](https://upload-images.jianshu.io/upload_images/1538674-d389883759b23f60.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

步骤2、3、4、5绿色部分

1. 节点B调用subscribe方法，找到了处理者Observer C
2. 此时兑现对节点A的承诺：**“如果”**我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”
3. 通知节点A在subscribe方法调用Observer B
###### 这一阶段：主要是把原来各个环节的“如果”一一兑现的过程，从最末一个Observable的subscribe方法开始，按节点顺序逆向的兑现承诺。

##### 第三阶段：执行业务

![part3.png](https://upload-images.jianshu.io/upload_images/1538674-a538e082adc70002.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

步骤6、7、8蓝色部分

1. 上一阶段中节点A有了操作者Observer B，开始执行subscribe方法，把一开始的任务“hello”，“world”交给Observer B
2. 当Observer B执行onNext()方法时，Observer B内部通知了Observer C执行onNext()
3. 然后执行步骤8，在Observer C中的onNext()中处理t

###### 这一阶段：是顺序执行各个业务操作的过程，只有这个阶段是与具体业务相关的阶段。大家可以先思考一下，如果是一个普通的map(Function function)，这个变换发生在哪？答案是：步骤7，8之间，Observer B的内部。

[github上有nullMap详细注释版的代码](https://github.com/OliverY/RxjavaYxj)


##### 如果还没看明白，那请看再看1遍，最好能自己写一遍感受一下。因为整个Rxjava就是这9行核心变换代码。


---

### map，observeOn，subscribeOn，flatMap操作符

那我们开始飙车，瞬间搞懂这4个操作符

#### map操作符

[github上这几个操作符的具体调用](https://github.com/OliverY/RxjavaYxj)

```
Observable.java

public <R> Observable<R> map(final Function<T, R> function) {

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer1) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        R r = function.apply(t); // 仅仅在这里加了变换操作
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
```

和“nullMap”相比，仅仅加了一行代码function.apply() 方法的调用。


#### observeOn操作符

```
Observable.java

public Observable<T> observeOn() {
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(final T t) {
							//模拟切换到主线程（通常上个节点是运行在子线程的情况）
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
```

与“nullMap”相比，改变了，把onNext方法切换到了主线程中执行。Rxjava源码会更加灵活，observerOn方法参数让你可以指定切换到的线程，其实就是传入了一个线程调度器，用于指定observer.onNext()方法要在哪个线程执行。我这里就简写了，直接写了切换到主线程，这你肯定能看明白。

#### subscribeOn操作符
```
Observable.java

public Observable<T> subscribeOn() {
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                
                new Thread() {
                    @Override
                    public void run() {
                    // 这里简写了，没有new Observer做中转，github上有完整代码
                        Observable.this.subscribe(observer);
                    }
                }.start();
            }
        };
    }
```
将上一个节点切换到新的线程，run方法里的代码中的Observable.this指的就是上一个节点，是不是就把上一个节点的线程切换到了新的线程当中，后续节点中如果无线程切换就都运行在了这个线程当中。

#### flatMap操作符
```
public <R> Observable<R> flatMap(final Function<T, Observable<R>> function) {

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        try {
                            Observable<R> observable = function.apply(t);
                            observable.subscribe(observer);
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
```


其实flatmap和map极为相似，只不过返回值是function.apply()的返回值是一个Observable。

Observable可以用来封装异步操作（当然也可以是同步操作，不过异步操作才非常有意义，同步直接用map就行）。所以这样就可以很方便的写出一个
异步1 —> 异步2 —> 异步3...的操作

##### 是不是很简单，弄懂nullMap的9行代码，剩下的都是根据nullMap变换而来的。这回可以自己写一个简版的Rxjava。也去看Rxjava的源码了吧，可以顺便学习一下Rxjava都做了哪些封装。

---

### 响应式编程的思想

> 响应式编程是一种面向数据流和变化传播的编程范式。

直接看这句话其实不太容易理解。让我们换个说法，实际编程中是什么会干扰我们，使我们无法专注于数据流和变化传播呢？答案是：**异步**，它会让我们的代码形成嵌套，不够顺序化。

> 因为异步，我们的业务逻辑会写成回调嵌套的形式，导致过一段时间看自己代码看不懂，语义化不强，不是按着顺序一个节点一个节点的往下执行的。
>
> **Rxjava（RxDart也是、ReactiveX应该都是这种思想）将所有的业务操作拆分成一步一步，每一步不管你是同步、异步，统统用一个节点封装起来，节点与节点之间是同步调用的关系。如此，整个代码的节点都是按顺序执行的。**
>
> 所以代码看起来语义性非常强，**所写即所思**。


写那么辛苦，不打算给一颗小星星吗
