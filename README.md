#### 本文已授权微信公众号:鸿洋(hongyangAndroid)在微信公众号平台原创首发

你是不是看过了很多分析Rxjava源码的文章，但依旧无法在心中勾勒出Rxjava原理的样貌。是什么让我们阅读Rxjava源码变得如此艰难？是Rxjava的代码封装，以及各种细节问题的解决。本文我把Rxjava的各种封装、抽象统统剥去，只专注于**基本的事件变换**。在理解了事件变换大概是做了件什么事情时，再去看源码，考虑一些其它问题就会更加容易。

###### 说明：这是一篇Rxjava源码分析的入门文章。旨在让读者脑中有个概念Rxjava最主要干了件什么事情，几个常用操作符的主要原理。今后再去看其它源码分析文章或源码能够更容易理解。因此本文先不去考虑Rxjava源码中复杂的抽象封装，线程间通信，onComplete、onError、dispose等方法，仅专注于“onNext”的最基本调用方式。

[掘金](https://juejin.im/post/5cce6fb05188254177317fdc)
[简书](https://www.jianshu.com/p/ba1835f65f89)

---

#### 本文目录：
1. 手写Rxjava核心代码，create，nullMap（核心）操作符
2. map，observeOn，subscribeOn，flatMap操作符
3. 响应式编程思想的理解

---
### 手写Rxjava核心代码，create，nullMap操作符

#### Create操作符
我们先来看一个最简单调用

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
######  本篇文章我把Observable称为“节点”，Observer称为“处理者”，一是因为我被观察者、被观察者、谁订阅谁给绕晕了，更重要的是我觉得这个名称比较符合Rxjava的设计思想。

Observable调用create方法创建一个自己，重写subscribe方法说：**如果** 我有一个处理者Observer，我就把“hello”，“world”交给它处理。

Observable调用了subscribe方法，真的找到了Observer。于是兑现承诺，完成整个调用逻辑。

**这里是“如果”有处理者，需要subscribe方法被调用时，“如果”才成立。Rxjava就是建立在一系列的“如果”（回调）操作上的。**

#### “nullMap”操作符（核心）
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
nullMap()等价于 下面这段代码
即把上个节点的数据不做任何修改的传递给下一节点的map操作
 
.map(new Function<String, String>() {
    @Override
    public String apply(String s) throws Exception {
        return s;
    }
})

```
**"nullMap"操作符在Rxjava源码里并不存在**，是我方便大家理解Rxjava运行机制写出来的。
因为nullMap操作是一个 **base变换操作**，map，flatMap，subscribeOn，observeOn操作符都是在nullMap上修改而来。所以Rxjava的变换的基础就是nullMap操作符。

```
Observable.java
// 这就是Rxjava的变换核心

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


“nullMap”操作符做了件什么事情：

1. 上一个节点Observable A调用nullMap(),在内部new一个新的节点Observable B。
2. 节点B重写subscribe方法，说"如果"自己有操作者Observer C，就new一个操作者Observer B，然后让节点A subscribe 操作者B。
3. 节点A subscribe 操作者B，让操作者B执行onNext方法。操作者B的onNext方法内部，调用了操作者C的onNext。从而完成了整个调用。

请注意2中的”如果“。意味着，当节点B中的subscribe方法没有被调用的时候，2，3步骤都不会执行（他们都是回调），没有Observer B，节点A也不会调用subscribe方法。
接下来分两种情况：

- 节点B调用了subscribe方法，则执行2，3，完成整个流程。
- 节点B调用nullMap，从新走一遍1，2，3步骤，相当于节点B把任务交给了下一个节点C。

> 概况一下就是：
> 
> **Observable每调用一次操作符，其实就是创建一个新的Observable。新Observable内部通过subscribe方法“逆向的”与上一Observable关联。在新Observable中的new出来的Observer内的onNext方法中做了和下一个Observer之间的关联。**
>

#### 图文详细解说nullMap整体调用过程

##### 第一阶段
![part_1.png](https://user-gold-cdn.xitu.io/2019/5/7/16a8f3fd330849da?w=888&h=776&f=png&s=107129)




上图代表节点还未最终subscribe一个Observer，图中步骤1

1. 节点A调用map方法，在内部创建了一个新的节点B

###### 这一阶段：主要就是节点与节点之间做连接，之间有各种“如果”（回调）的承诺。节点B这时候对节点A做了个承诺：“如果”我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”。节点B中的操作者Observer B内部做了与Observer C的衔接工作


##### 第二阶段：逆向subscribe

![part_2.png](https://user-gold-cdn.xitu.io/2019/5/7/16a8f3fd332b2028?w=1184&h=767&f=png&s=171619)

这一阶段是subscribe方法被调用，传入了最终的Observer。图中步骤2、3

2. 节点B调用subscribe方法，找到处理者Observer C
3. 节点B兑现对节点A的承诺：**“如果”**我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”，这里的Observable.this == Observable A。

###### 这一阶段：是把原来各个节点的“如果”一一兑现的过程，从最末一个Observable的subscribe方法开始，按节点顺序逆向的兑现承诺。每个subscribe方法内部都会新建一个Observer，然后用上一个节点Observable来subcriber这个Observer。这是一个逆序的过程。

##### 第三阶段：运行业务

![part_3.png](https://user-gold-cdn.xitu.io/2019/5/7/16a8f3fd333edc04?w=913&h=855&f=png&s=193035)


图中步骤4，5

4. 节点A调用subscribe，让Observer B调用onNext方法传入“hello”，“world”数据
5. 在Observer B的onNext()方法中，通知ObserverC调用onNext方法

###### 这一阶段：是通过各个节点的Observer顺序执行具体的业务操作的过程，只有这个阶段是与具体业务相关的阶段。

> 大家可以先思考一下，如果是一个普通的map(Function function)，这个变换发生在哪？
>
> 答案是：在第三阶段中，Observer B的内部的onNext方法中。那如果是线程切换呢？

[github上有nullMap详细注释版的代码](https://github.com/OliverY/RxjavaYxj)


##### 整个Rxjava就是这9行核心变换代码了。如果以上不是特别理解我非常建议你继续看完剩下的部分，再回过头来看一遍第一部分。

---

### map，observeOn，subscribeOn，flatMap操作符

接下来让我们看看这4个操作符，仅仅是在nullMap中做了小改动而已。
[操作符源码](https://github.com/OliverY/RxjavaYxj/blob/master/app/src/main/java/com/yxj/rxjavayxj/rxjava/Observable.java)

#### map操作符

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
                        
                    }
                });
            }
        };
    }
```

与“nullMap”相比，修改了最内部的onNext方法执行所在的线程。Rxjava源码会更加灵活，observerOn方法参数让你可以指定切换到的线程，其实就是传入了一个线程调度器，用于指定observer.onNext()方法要在哪个线程执行。原理是一样的。我这里就简写，直接写了切换到主线程，这你肯定能看明白。

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
将上一个节点切换到新的线程，修改了Observable.this.subscribe()运行的线程，Observable.this指的是调用subscribeOn()的Observable，即上一个节点。因此subscribeOn操作符修改了上一个节点的运行所在的线程

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


flatmap和map极为相似，只不过function.apply()的返回值是一个Observable。

Observable是一个节点，既可以用来封装异步操作，也可以用来封装同步操作（封装同步操作 == map操作符）。所以这样就可以很方便的写出一个
耗时1操作 —> 耗时2操作 —> 耗时3操作...的操作

##### 到这里相信大家已经对Rxjava怎样运行，几个常见的操作符内部基本原理有了初步的理解，本文的目的就已经达到了。在之后看Rxjava源码或者其它分析文章时，就能少受各种变换的干扰。接下来就可以思考Rxjava是如何对各个Observable做封装，线程之间如何通信，onComplete、onError、dispose等方法如何实现了。
---

### 响应式编程的思想

> 响应式编程是一种面向数据流和变化传播的编程范式。

直接看这句话其实不太容易理解。让我们换个说法，实际编程中是什么会干扰我们，使我们无法专注于数据流和变化传播呢？答案是：**异步**，它会让我们的代码形成嵌套，不够顺序化。

> 因为异步，我们的业务逻辑会写成回调嵌套的形式，导致过一段时间看自己代码看不懂，语义化不强，不是按着顺序一个节点一个节点的往下执行的。
>
> **Rxjava将所有的业务操作变成一步一步，每一步不管你是同步、异步，统统用一个节点包裹起来，节点与节点之间是同步调用的关系。如此，整个代码的节点都是按顺序执行的。**




##### 限于作者个人水平有限，本文部分表述难免有不对之处，请留言指出，相互交流。
