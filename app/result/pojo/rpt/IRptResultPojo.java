package result.pojo.rpt;

public interface IRptResultPojo<T> {

    public T add(T t);

    public T divide(int divide);
    
    public boolean jdbcSave(Long userId);

}
