package cn.com.sandi.genericdb.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PagingVO<T> extends GenericVO {
    /***
     * 总条数
     */
    private Integer total;

    /***
     * 总页数
     */
    private Integer pageTotal;

    /***
     * 当前页
     */
    private Integer currentPage;

    /***
     * 内容
     */
    private List<T> list;

    /***
     * 获取总页数
     * @param total 总条数
     * @param pageSize 每页大小
     * @return int
     */
    public static int getPageTotal(int total, int pageSize){
        return (total % pageSize) > 0 ? total / pageSize + 1 : total / pageSize;
    }
}
