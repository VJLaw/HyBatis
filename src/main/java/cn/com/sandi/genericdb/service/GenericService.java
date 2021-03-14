package cn.com.sandi.genericdb.service;



import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface GenericService<T, PK extends Serializable>{


    /***
     * 添加一条记录
     * @param object 元素名称或者tableName注解的值必须与数据库中表名相同格式必须为驼峰例如：数据库中：demo_table, 实体类名：demoTable
     * @return return 返回主键
     */
    Long save(T object) throws Exception;

    /**
     * SQL语句条件查询
     * @param whereJPQL	select语句中where部分的内容
     * @param paramMap	条件数据
     * @param begin	查询开始行数
     * @param pageSize	单次查询数据数量大小
     * @return	返回类对象数据集
     */
    List<T> findByWhere(String whereJPQL, Map<String, Object> paramsMap, int begin, int pageSize) throws Exception;

    /**
     * 获取所有数据(包含重复数据，最大只能获取2万条数据)
     * @return	返回继承的实际类对象集合
     */
    List<T> getAll() throws Exception;

    /**
     * 获取指定范围内的数据(包含重复数据，最大只能获取2万条数据)
     * @param begin	开始行数
     * @param pageSize	获取的数据量大小
     * @return	返回继承的实际类对象集合
     */
    List<T> getAll(int begin, int pageSize) throws Exception;


    /**
     * 获取所有数据(包含重复数据)
     * @return	返回继承的实际类对象集合
     */
    T get(PK id) throws Exception;

    /**
     * 按主键判断该类对象数据是否已存在
     * @param id 继承的实际类对象的主键
     * @return	返回布尔值，存在true，不存在false
     */
    boolean exists(PK id) throws Exception;

    /**
     * 更新类对象数据
     * @param object 继承的实际类对象
     */
    void update(T object) throws Exception;

    /**
     * 按主键删除
     * @param id 继承的实际类对象的主键
     */
    void delete(PK id) throws Exception;

    /**
     * SQL语句条件统计类的数据总量(where条件)
     * @param whereJPQL	select语句中where部分的内容
     * @param paramMap	条件数据
     * @return	返回数据总量大小
     */
    int countByWhere(String whereJPQL, Map<String, Object> paramMap) throws Exception;


    /**
     * 更新类对象数据
     * @param updateJPQL update语句中set部分的内容
     * @param whereJPQL	update语句中where部分的内容
     * @param paramMap	对应更新语句中的字段值
     * @return	更新成功数
     */
    int updateByWhere(String updateJPQL, String whereJPQL, Map<String, Object> paramMap) throws Exception;

    /**
     * 删除类对象数据
     * @param whereJPQL	delete语句中where部分的内容
     * @param paramMap	对应删除语句中的字段值
     * @return	删除成功数
     */
    int deleteByWhere(String whereJPQL, Map<String, Object> paramMap) throws Exception;



}
