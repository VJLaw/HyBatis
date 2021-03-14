package cn.com.sandi.genericdb.vo;

import lombok.Data;

@Data
public class PageQueryCriteria implements QueryCriteria {
    /**
     * 查询起始行
     */
    private int index = 0;

    /**
     * 当前页数
     */
    private int currentPage = 1;

    /**
     * 记录行数
     */
    private int rows = 20;

    /**
     * 总页数
     */
    private int pageCount;

    /**
     * 排序字段
     */
    private String sort=null;

    /**
     * 排序策略，desc/asc
     */
    private String order=null;
}
