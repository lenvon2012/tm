
package message;

public interface EntityWorkerMessage<T> {

    public T findEntity();

    public void applyFor(T t);
}
