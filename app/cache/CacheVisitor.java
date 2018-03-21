
package cache;

public interface CacheVisitor<T> {
    public String prefixKey();

    public String expired();

    public String genKey(T t);
}
