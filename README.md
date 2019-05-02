# RxjavaYxj
目标是让看过这篇文章的同学都会写Rxjava核心源码，能看懂Rxjava源码

> 本文适合已经使用过Rxjava，但不知其所以然的同学。你将了解到支撑Rxjava运行最核心的代码。本文手写了一个Rxjava，剔除了Rxjava源码中的各种抽象封装之后，使得核心代码极其简单明了。几乎不用担心看不懂，总共代码不超过50行，还有个小故事模型帮助你理解代码的运行过程。同时非常建议新开一个工程，复制其中的代码，自己打上相应的断点跑一跑，即可明白Rxjava是怎么一回事。源码之下无秘密。[项目源码](https://github.com/OliverY/RxjavaYxj)


#### 本文分为：
- 手写Rxjava核心代码，create，空map操作符
- map，observeOn，subscribeOn，flatMap操作符
- Rxjava的一些封装使用
- 响应式编程的意义

---
### 手写Rxjava核心代码，create，map操作符


首先我们看一个最简单调用
```
1.创建一个observable
2.交给observer处理

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

// Observable
public abstract class Observable<T> {

    public abstract void subscribe(Observer<T> observer);

    public static <T> Observable<T> create(Observable<T> observable){
        return observable;
    }

}

// Observer
public interface Observer<T> {

    void onNext(T t);
    void onComplete();
}
```
#####  本篇文章Observable我称为“节点”，Observer称为“处理者”，因为我被观察者、被观察者、谁订阅谁给绕晕了。

我们先来看看Observable类，create方法是一个工厂方法，创建一个自己的对象。subscribe方法是abstract的，在创建Observable对象时需要实现该方法的内容。
这个地方其实在说，Observable通过create方法创建了一个自己的实例对象，subscribe方法说 **如果** 我能找到处理者（Observer），那我就让处理者处理“hello”，“world”。
> 这里记住2个点：
**1.这里是“如果”有处理者，要subscribe方法被调用时，如果才成立。这一点很重要，请记住。**
**2.节点（observable）不触碰具体业务，处理者（observer）负责业务的具体流转操作**

那节点干嘛用的，我们接着看？

```
1.创建一个observable
2.调用map操作符做变换，这里做“空变换”操作
3.交给observer处理

Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .map()
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

                  // 上面的map等价于   
//                .map(new Function<String, String>() {
//                    @Override
//                    public String apply(String s) {
//                        return s;
//                    }
//                })


public abstract class Observable<T> {

    ...之前本类的代码...

    // 节点A调用map操作符
    public Observable<T> map(){

         /*
         以下是rxjava变换的关键
         剩下的subscribeOn，observeOn，flatMap等操作符都是在这个基础上修改而来
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
    
}
```
如果你已经看懂了就不需要看了下面这部分解释，可以直接跳到下一节 observeOn，subscribeOn，flatMap，compose操作符

我们讲一遍“空map”操作符做了件什么事情：
1.节点A因为自己没有操作者（observer），所以在内部创建了一个新的节点B，把任务交给了新节点B。
2.节点是个抽象类，所以节点B需要重写自己的subscribe方法，方法内部重新创建了一个操作者observerB，并且让节点A使用节点B内部创建出来的这个observerB。
3.操作者observerB中，onNext方法内，让处理者observerC来接收t。
从而完成了数据从节点A—>节点B

所以此时，如果节点B如果调用了subscribe方法，就意味着有一个操作者observerC，整个流程就跑通了，原来那些个如果的事就都变成了现实。要是节点B不subscribe，而是map，意味着再来一遍1，2，3步骤，把任务传递到了下一个节点C。任务就一直流转，直到某个节点可以调用subscribe方法，把任务交给observer，完成整个任务流。

如果到这里你已经看懂了就不需要看了以下这部分小故事模型，可以直接跳到下一节 observeOn，subscribeOn，flatMap，compose操作符

如果你还是觉得这个过程太复杂，不够形象。那我们先来看一个故事：

领导A说，**如果**我有小秘，我就让她干 任务一、任务二，但是领导A并没有小秘。
于是他找来领导B说“我有事要你办”。
领导B说拍胸脯说，包我身上，但咱俩身份都是领导，活具体都得秘书干。
所以**如果**我(B)有小秘，**您就把活一起交给她**，剩下的事您就不用管了。
她会再把结果交给下一个秘书（我会去找下一个秘书的，或者下一个领导）。

结果就是领导之间一环推一环，都在想着如果我有小秘，我就让她干活，没小秘就把活推给下一个领导，下个领导会给我安排小秘干活，直到真有一个没领导的秘书出现，整件事才能办成。不然都只是个构想。

领导只是指挥小秘，和其它领导沟通，不处理具体的任务。做事的都是小秘。

领导：Observable
小秘：observer

如果还没看明白，那请看再看1遍，因为这玩意是核心。


---
### map，observeOn，subscribeOn，flatMap操作符

#### map操作符

```
public abstract class Observable<T> {

   ...之前本类的代码...

    public <R> Observable<R> map(final Function<T,R> function){

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer1) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        R r = function.apply(t); // 和“空map”相比，仅仅在这里加了变换
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
}

Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .map(new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();  // 转换成字符串长度
            }
        })
        .subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer i) {
                Log.e("yxj","i:"+i);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });

```

和“空map”相比，仅仅加了一行代码function.apply() 方法的调用。


#### observeOn操作符

```
public abstract class Observable<T> {

    ...之前本类的代码...

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

                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(final T t) {
                        // 与“空map”相比，在这里开了一个线程，把onNext方法调用放到了子线程中
                        new Thread(){
                            @Override
                            public void run() {
                                observer.onNext(t);
                            }
                        }.start();

                          // 模拟切换到主线程（通常上个节点是运行在子线程的情况）
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                observer.onNext(t);
//                            }
//                        });
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        };
    }

    Handler handler = new Handler();
}

```
#### subscribeOn操作符
```
/**
     * 改变上个节点运行的线程
     * @return
     */
    public Observable<T> subscribeOn(){
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                new Thread(){
                    @Override
                    public void run() {
                        /*
                        还记得这里的Observable.this == 调用subscribeOn方法的 Observable对象吧！
                        所以把subscribe方法调用放到子线程就切换了上一个节点的运行线程
                         */
                        Observable.this.subscribe(observer);// 这里也可以写成 new 一个匿名的Observer，内部中转一下onNext数据
                    }
                }.start();
            }
        };
    }
```

#### flatMap操作符
```
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
```
其实flatmap和map极为相似，只不过返回值是function.apply()的返回值是一个Observable。Observable可以用来封装异步操作（当然也可以是同步操作，不过异步操作才非常有意义，同步直接用map就行）。所以这样就可以很方便的写出一个
异步1 —> 异步2 —> 异步3...的操作。
当然我们看到的许多博客给出的例子flatmap也可以用作将一个Observable<list>拆分成多个Observable<item>。这里我的代码里没有写那么细，我相信看到这里，你已经有能力对照Rxjava源码看明白flatMap具体是怎么做的了。


### Rxjava的一些封装使用

其实这一part非常简单，许多同学都会，会的同学可以略过。不会的同学可以尝试了解一下，封装出自己需要的操作。

- 封装网络访问
- 封装Edittext的addTextChangedListener()

#### 封装网络访问
```
Api.java中

/**
     * 用Rxjava做封装
     * @param userName
     * @param password
     * @return
     */
    public static Observable<String> login(String userName, String password){
        return Observable.create(new Observable<String>() {
                    @Override
                    public void subscribe(Observer<String> observer) {
                        try {
                            /*
                            模拟网络访问
                            这里你可以把sleep替换成 okhttp、httpclient的网络访问
                            在获得结果时调用observer.onNext()
                            把结果放入onNext当中
                             */
                            
                            Thread.sleep(3000);
                            observer.onNext("登录成功");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            //observer.onError(new Exception("登录失败"));
                        }
                    }
                });
    }

Test.java中
/**
     * 模拟用Rxjava封装网络访问
     */
    private void login() {
        String userName = "yxj";
        String password = "123456";

        Api.login(userName,password)
                .subscribeOn()// 上游切换到子线程
                .observeOn() // 下游切换到主线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.e(TAG,s);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
```

#### 封装Edittext的addTextChangedListener()
```
RxEditText.java中

public static Observable<String> textChanges(final EditText editText){
        return Observable.create(new Observable<String>() {
            @Override
            public void subscribe(final Observer<String> observer) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        observer.onNext(s.toString());
                    }
                });
            }
        });
    }

Test.java 中
private void addEditTextWatcher(final EditText editText){
        RxEditText.textChanges(editText)
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) {
                        return s.length()>4;
                    }
                })
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        editText.setTextColor(aBoolean? Color.RED:Color.GREEN);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

```
---

### 响应式编程的意义

> 响应式编程是一种面向数据流和变化传播的编程范式。

直接看这句话其实不太容易理解，那我们换个说法。实际编程中什么会干扰我们，使我们无法专注于数据流和变化传播呢？答案是：**异步**

> 因为异步，我们的业务逻辑会写成回调嵌套的形式，导致过一段时间看自己代码看不懂，语义化不强，不是按着顺序一个节点一个节点的往下执行的。
**Rxjava（RxDart也是、ReactiveX应该都是这种思想）将所有的操作拆分成一步一步，每一步不管你是同步、异步，统统用一个节点统一封装起来，节点与节点之间是同步调用的关系。如此，整个代码的节点按顺序执行。**
所以代码看起来语义性非常强，所写即所思。
