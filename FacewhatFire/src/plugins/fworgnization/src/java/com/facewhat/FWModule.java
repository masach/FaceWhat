package com.facewhat;


/**
 * 所有EIM模块都应该实现该接口. 
 * <br/>
 * 注意, 不能与XMPP Component接口一起使用, 因为两个接口有重叠的地方.
 * <p>
 * 该接口包括初始化,启动,停止,销毁等4个method.
 * 可方便EIMPlugin对各个组件的管理
 * @author MaiJingFeng
 *
 */
public interface FWModule {

	/**
     *
     * 初始化组件.
     * <br/>
     * 组件的初始化操作应该在start()之前,
     * 调用destroy()应该保证之前已经initialize()过
     */
    void initialize(FWPlugin fwPlugin);

    /**
     * 启动组件
     * <br/>
     * 应该保证该方法的快速执行并返回, 任何长时间的操作应该尽可能使用分支线程;
     */
    void start();

    /**
     * 停止组件
     * <br/>
     * 该方法用户停止组件相关工作,包括停止组件所开启的其他分支线程的工作.
     * 以便新一轮的重新初始化或者销毁
     * 
     */
    void stop();

    /**
     * 销毁组件
     * <br/>
     * 该方法中应该回收并销毁组件初始化所使用的资源开销(对引用资源置为null).
     * 该方法一般在组件生命周期结束时被调用(例如插件的摘除).
     */
    void destroy();
    
    
    /**
     * 是否已经启动
     * @return
     */
    public boolean isStart();
}
