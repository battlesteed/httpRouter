package steed.router;

public interface ModelDriven<T> {
    T getModel();
    /**
     * model创建并填充httpRequest参数后,会回调该方法,
     * 可以在该方法设置model额外的字段,比如组织,公司什么的
     * @param t
     */
    void onModelReady(T t);
}