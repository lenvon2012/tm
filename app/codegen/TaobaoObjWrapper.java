
package codegen;

public interface TaobaoObjWrapper<T> {

    public boolean isSameEntity(T t);

    public boolean isStatusChaned(T t);

    public boolean updateWrapper(T t);
}
