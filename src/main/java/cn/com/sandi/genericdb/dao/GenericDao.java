package cn.com.sandi.genericdb.dao;

import cn.com.sandi.genericdb.vo.SqlTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Mapper
public interface GenericDao<T, PK extends Serializable> {

    int save(SqlTemplate saveVo);

    Long nextId(SqlTemplate saveVo);

    List<Map<String, Object>> select(SqlTemplate selectVo);

    int count(SqlTemplate countVo);

    int update(SqlTemplate updateVo);

    int updateObject(SqlTemplate updateVo);

    int remove(SqlTemplate removeVo);
}
