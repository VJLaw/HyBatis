package cn.com.sandi.genericdb.service;

import cn.com.sandi.genericdb.vo.PageQueryCriteria;
import cn.com.sandi.genericdb.vo.QueryCriteria;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;

public interface Query {

    /**
     * 获取记录总数
     * @param statementName	查询语句名称
     * @param criteria	查询条件
     * @return 记录总数
     */
    public int queryForCount(String statementName, QueryCriteria criteria);

    /**
     * 获取记录总数
     * @param statementName	查询语句名称
     * @param paramMap	Map查询条件
     * @return	记录总数
     */
    public int queryForCount(String statementName, HashMap<String, Object> paramMap) ;

    /**
     * 无条件查询
     * @param statementName	查询语句名称
     * @return	记录数据集
     */
    public <T> List<T> queryForList(String statementName);

    /**
     * 带条件查询
     * @param statementName	查询语句名称
     * @param criteria	查询条件
     * @return	记录数据集
     */
    public <T> List<T> queryForList(String statementName, QueryCriteria criteria);

    /**
     * 带条件查询
     * @param statementName	查询语句名称
     * @param paramMap	Map查询条件
     * @return	记录数据集
     */
    public <T> List<T> queryForList(String statementName, HashMap<String, Object> paramMap);

    /**
     * 无条件查询(无数据量限制)
     * @param statementName	查询语句名称
     * @return	记录数据集
     */
    public <T> List<T> queryForListFull(String statementName);

    /**
     * 带条件查询(无数据量限制)
     * @param statementName	查询语句名称
     * @param criteria	查询条件
     * @return	记录数据集
     */
    public <T> List<T> queryForListFull(String statementName, QueryCriteria criteria);

    /**
     * 带条件查询(无数据量限制)
     * @param statementName	查询语句名称
     * @param paramMap	Map查询条件
     * @return	记录数据集
     */
    public <T> List<T> queryForListFull(String statementName, HashMap<String, Object> paramMap);

    /**
     * 查询数据(包括数据集合和记录总数)
     * @param listSqlName	查询语句名称(数据集)
     * @param countSqlName	查询语句名称(记录总数)
     * @param criteria	查询条件
     * @return	json对象，包括数据集合和记录总数
     */
    public JSONObject queryForList(String listSqlName, String countSqlName, PageQueryCriteria criteria);

    /**
     * 导出excel文件
     * @param statementName	查询语句名称(数据集)
     * @param paramMap	Map查询条件
     * @param excelName	excel文件名称
     * @param clumnFields	导出字段，格式：[{ "field":字段 , "title":excel列名 },{ "field":字段 , "title":excel列名  }]
     * @param transFields	需要转换的字段，格式：{字段:字典，字段：字典}
     * @return	字节流
     */
    public byte[] queryToExcel(String statementName, HashMap<String, Object> paramMap, String excelName, JSONArray clumnFields, JSONObject transFields);

    /**
     * 导出excel文件
     * @param statementName	查询语句名称(数据集)
     * @param criteria	查询条件
     * @param excelName	excel文件名称
     * @param clumnFields	导出字段，格式：[{ "field":字段 , "title":excel列名 },{ "field":字段 , "title":excel列名  }]
     * @param transFields	需要转换的字段，格式：{字段:字典，字段：字典}
     * @param all	是否导出所有数据集，true是，false否
     * @return	字节流
     * @throws Exception
     */
    public byte[] queryToExcel(String statementName, PageQueryCriteria criteria, String excelName, JSONArray clumnFields, JSONObject transFields, boolean all) throws Exception;
}
