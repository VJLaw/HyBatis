package cn.com.sandi.genericdb.service.impl;

import cn.com.sandi.genericdb.dao.GenericDao;
import cn.com.sandi.genericdb.service.GenericService;
import cn.com.sandi.genericdb.utils.GenericUtils;
import cn.com.sandi.genericdb.vo.KeyValueTemplate;
import cn.com.sandi.genericdb.vo.SqlTemplate;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GenericServiceImpl<T, PK extends Serializable> implements GenericService<T,PK> {

    @Resource
    private GenericDao genericDao;

    @Transactional
    @Override
    public Long save(T object)  throws Exception {
        if (object == null) return null;
        SqlTemplate saveDO = new SqlTemplate();
        String tableName = GenericUtils.getTableName(object.getClass());
        String idFielName = GenericUtils.getIdFielNames(object.getClass());
        String pkColumnValue = GenericUtils.getPkColumnValue(object.getClass());
        saveDO.setTableName(tableName);
        saveDO.setPkColumnValue(pkColumnValue);
        String[] saveFieldNames = null;
        List<Map<String, Object>[]> saveValues = new ArrayList<>();
        saveFieldNames = GenericUtils.getObjectFieldNames(object.getClass());
        Map<String, Object> map = new HashMap<>();
        GenericUtils.copyMap(map, object);
        Map<String, Object>[] objects = new HashMap[saveFieldNames.length];
        int i = 0;
        Long id = null;
        for(String saveField : saveFieldNames){
            if(saveField.equals(idFielName)){
                id = genericDao.nextId(saveDO);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap = GenericUtils.getSaveMap(id,saveFieldNames, map, i, saveField,null);
                paramMap.put("value", id);
                objects[i] = paramMap;
            }else{
                Object value = map.get(saveField);
                objects[i] = GenericUtils.getSaveMap(value,saveFieldNames, map, i, saveField,value);
            }
            i++;
        }
        saveValues.add(objects);
        saveDO.setSaveFieldNames(saveFieldNames);
        saveDO.setSaveValues(saveValues);
        int cnt = genericDao.save(saveDO);
        if (cnt>0){
            return id;
        }else{
            return null;
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public List<T> findByWhere(String whereJPQL, Map<String, Object> paramsMap, int begin, int pageSize) throws Exception {
        if (begin==-1) begin = 0;
        if (pageSize==-1) pageSize = GenericUtils.SELECT_MAX_CNT;
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String sql = GenericUtils.getSelectSql(c, GenericUtils.OPERATING_ALL,paramsMap,whereJPQL,begin,pageSize);
        SqlTemplate selectVo = new SqlTemplate();
        selectVo.setSql(sql);
        List<Map<String,Object>> data = genericDao.select(selectVo);
        return buildResultList(c,data);
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public List<T> getAll()  throws Exception {
        return getAll(0, GenericUtils.SELECT_MAX_CNT);
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public List<T> getAll(int begin, int pageSize) throws Exception {
        return getAllSub(begin,pageSize);
    }

    private List<T> getAllSub(int begin, int pageSize) throws Exception {
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String tableName = GenericUtils.getTableName(c);
        if (pageSize> GenericUtils.SELECT_MAX_CNT) pageSize = GenericUtils.SELECT_MAX_CNT;
        String sql = "SELECT " + GenericUtils.OPERATING_ALL + " FROM " + tableName + " limit " + begin + "," + pageSize;
        SqlTemplate selectVo = new SqlTemplate();
        selectVo.setSql(sql);
        List<Map<String,Object>> data = genericDao.select(selectVo);
        return buildResultList(c,data);
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public T get(PK id) throws Exception {
        return getSub(id);
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public boolean exists(PK id) throws Exception {
        if (get(id)==null){
            return false;
        }else{
            return true;
        }
    }

    private T getSub(PK id) throws Exception{
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String tableName = GenericUtils.getTableName(c);
        String idFielName = GenericUtils.getIdFielNames(c);
        String sql = "SELECT " + GenericUtils.OPERATING_ALL + " FROM " + tableName + " WHERE "
                + idFielName.split(":")[1] + "=" + id;
        SqlTemplate selectVo = new SqlTemplate();
        selectVo.setSql(sql);
        List<Map<String,Object>> data = genericDao.select(selectVo);
        List<T> result =  buildResultList(c,data);
        if (result!=null&&result.size()>0) return result.get(0);
        return null;
    }


    @Transactional
    @Override
    public void update(T object) throws Exception {
        if (object == null) return ;
        SqlTemplate updateDO = new SqlTemplate();
        String tableName = GenericUtils.getTableName(object.getClass());
        String idFielName = GenericUtils.getIdFielNames(object.getClass());
        String[] saveFieldNames = GenericUtils.getObjectFieldNames(object.getClass());
        Map<String, Object> map = new HashMap<>();
        GenericUtils.copyMap(map, object);
        int i = 0;
        KeyValueTemplate whereField = new KeyValueTemplate();
        List<KeyValueTemplate> updateFieldList = new ArrayList<KeyValueTemplate>();
        for (String saveField : saveFieldNames) {
            Object value = map.get(saveField);
            Map<String,Object> dataMap = GenericUtils.getSaveMap(value,saveFieldNames, map, i, saveField,value);
            KeyValueTemplate keyValueTemplate = new KeyValueTemplate();
            keyValueTemplate.setKey(saveField.split(":")[1]);
            keyValueTemplate.setValue(dataMap.get("value"));
            keyValueTemplate.setType(dataMap.get("type").toString());
            if(saveField.equals(idFielName)){
                whereField = keyValueTemplate;
            }else{
                updateFieldList.add(keyValueTemplate);
            }
            i++;
        }
        updateDO.setKeyValueTemplate(whereField);
        updateDO.setUpdateFieldList(updateFieldList);
        updateDO.setTableName(tableName);
        genericDao.updateObject(updateDO);
    }

    @Transactional
    @Override
    public void delete(PK id) throws Exception {
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String tableName = GenericUtils.getTableName(c);
        String idFielName = GenericUtils.getIdFielNames(c);
        String sql = "DELETE FROM " + tableName + " WHERE " + idFielName.split(":")[1] + "=" + id;
        SqlTemplate deleteVo = new SqlTemplate();
        deleteVo.setSql(sql);
        genericDao.remove(deleteVo);
    }

    @Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
    @Override
    public int countByWhere(String whereJPQL, Map<String, Object> paramMap) throws Exception {
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String sql = GenericUtils.getSelectSql(c, GenericUtils.OPERATING_COUNT,paramMap,whereJPQL,0,0);
        SqlTemplate countVo = new SqlTemplate();
        countVo.setSql(sql);
        return genericDao.count(countVo);
    }

    @Transactional
    @Override
    public int updateByWhere(String updateJPQL, String whereJPQL, Map<String, Object> paramMap) throws Exception {
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String sql = GenericUtils.getUpdateSql(c,paramMap,whereJPQL,updateJPQL);
        SqlTemplate updateVo = new SqlTemplate();
        updateVo.setSql(sql);
        return genericDao.update(updateVo);
    }

    @Transactional
    @Override
    public int deleteByWhere(String whereJPQL, Map<String, Object> paramMap) throws Exception {
        Class<T> c = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        String sql = GenericUtils.getDeleteSql(c,paramMap,whereJPQL);
        SqlTemplate deleteVo = new SqlTemplate();
        deleteVo.setSql(sql);
        return genericDao.remove(deleteVo);
    }


    private List<T> buildResultList(Class c, List<Map<String, Object>> list) throws Exception {
        if (list==null||list.size()==0) return null;
        List<T> datas = new ArrayList<T>();
        Map<String,String> fieldMap = new HashMap<String,String>();
        String[] fieldNames = GenericUtils.getObjectFieldNames(c);
        for (int i = 0; i < fieldNames.length; i++) {
            String[] temp = fieldNames[i].split(":");
            fieldMap.put(temp[1],temp[0]);
        }
        for (int i = 0; i < list.size(); i++) {
            Map<String,Object> son = list.get(i);
            Map<String,Object> newSon = new HashMap<String,Object>();
            for (Map.Entry<String,Object> entry:son.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String newKey = fieldMap.get(key);
                newSon.put(newKey,value);
            }
            datas.add((T) JSONObject.parseObject(JSONObject.toJSONString(newSon),c));
        }
        return  datas;
    }

}
