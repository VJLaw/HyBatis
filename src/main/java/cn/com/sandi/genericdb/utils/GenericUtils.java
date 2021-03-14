package cn.com.sandi.genericdb.utils;


import cn.com.sandi.generic.utils.DateUtils;
import cn.com.sandi.genericdb.annotation.FieldName;
import cn.com.sandi.genericdb.annotation.ID;
import cn.com.sandi.genericdb.annotation.TableName;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

public class GenericUtils {

    public static final String OPERATING_COUNT = " COUNT(*) ";

    public static final String OPERATING_ALL = " * ";

    public static final int SELECT_MAX_CNT = 20000;

    public static final String WARNING_MESSAGE = "导出文件大小已超出系统限制，请与系统管理员联系";

    public static final int MAX_EXPORT_FILE=10240;

    public static String getTableName(Class c){
        if(c.isAnnotationPresent(TableName.class))
            return ((TableName) c.getAnnotation(TableName.class)).value();
        return c.getSimpleName();
    }

    public static String[] getObjectFieldNames(Class c){
        Field[] fields = c.getDeclaredFields();
        List<String> fieldNameList = new ArrayList<>();
        for(Field field : fields){
            if(field.isAnnotationPresent(FieldName.class)){
                FieldName fieldNameAnno = field.getAnnotation(FieldName.class);
                String fieldName = fieldNameAnno.value();
                //前面是本身的字段名，后面是注解里的字段名
                fieldNameList.add(field.getName() + ":" + fieldName);
            }else{
                fieldNameList.add(field.getName());
            }
        }
        int size = fieldNameList.size();
        String[] fieldNames = new String[size];
        for(int i = 0; i < size; i++){
            fieldNames[i] = fieldNameList.get(i);
        }
        return fieldNames;
    }

    public static String getPkColumnValue(Class c){
        Field[] fields = c.getDeclaredFields();
        String pkColumnValue = "";
        for(Field field : fields){
            if(field.isAnnotationPresent(ID.class)){
                ID idAnno = field.getAnnotation(ID.class);
                pkColumnValue = idAnno.value();
            }
        }
        if (StringUtils.isEmpty(pkColumnValue)){
            throw new IllegalArgumentException("对象类@ID注解中缺少PkColumnValue");
        }
        return pkColumnValue;
    }

    public static String getIdFielNames(Class c){
        Field[] fields = c.getDeclaredFields();
        String idFielName = "";
        for(Field field : fields){
            if(field.isAnnotationPresent(ID.class)){
                if(field.isAnnotationPresent(FieldName.class)){
                    FieldName fieldNameAnno = field.getAnnotation(FieldName.class);
                    String fieldName = fieldNameAnno.value();
                    //前面是本身的字段名，后面是注解里的字段名
                    idFielName = field.getName() + ":" + fieldName;
                }else{
                    idFielName = field.getName();
                }
            }
        }
        if (StringUtils.isEmpty(idFielName)){
                throw new IllegalArgumentException("对象类缺少@ID注解标识主键");
        }
        return idFielName;
    }

    public static void copyMap(Map dest, Object orig){
        copyMap(dest, orig, true, null);
    }

    private static void copyMap(Map dest, Object orig, boolean copyNulls, String[] ext){

        if(dest == null)
            throw new IllegalArgumentException("No destination bean specified");
        if(orig == null)
            throw new IllegalArgumentException("No origin bean specified");

        JSONObject ojson = (JSONObject) JSON.toJSON(orig);
        for(String key : ojson.keySet()){
            if(((ojson.get(key) == null) && (!copyNulls)) || searchInArray(key, ext)){
            }else{
                dest.put(key, ojson.get(key));
            }
        }
    }

    private static boolean searchInArray(String key, String[] arry){
        if(arry != null && arry.length > 0){
            for(String s : arry){
                if(key.equals(s)){
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * 获取jdbcType
     * @param object object
     * @return String
     */
    private static String getJdbcType(Object object){
        String type = ",jdbcType=";
        if(object instanceof String){
            type += "VARCHAR";
        }else if(object instanceof Integer){
            type += "DECIMAL";
        }else if(object instanceof Long){
            type += "DECIMAL";
        }else if(object instanceof Date){
            type += "DATETIME";
        }else if(object instanceof Timestamp){
            type += "TIMESTAMP";
        }else{
            type = "";
        }
        return type;
    }

    public static Map<String, Object> getSaveMap(Object object, String[] saveFieldNames, Map<String, Object> map, int i, String saveField, Object param){
        String type = getJdbcType(object);
        Object value = getTableFieldName(saveFieldNames, map, i, saveField,param);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("type", type);
        paramMap.put("value", value);
        return paramMap;
    }

    private static Object getTableFieldName(String[] saveFieldNames, Map<String, Object> map, int i, String saveField, Object param){
        if(param == null){
            //前面是字段本身的名字
            if(saveField.contains(":")){
                String[] saveFieldNameCo = saveField.split(":");
                param = map.get(saveFieldNameCo[0]);
                saveFieldNames[i] = saveFieldNameCo[1];
            }
        }
        return param;
    }

    public static String getDeleteSql(Class c,Map<String,Object> paramMap,String whereJPQL){
        String sql = "";
        String tableName = getTableName(c);
        String[] fieldNames = getObjectFieldNames(c);
        String whereSql = getWhereSQL(whereJPQL,paramMap,fieldNames,-1,-1,null);
        sql = "DELETE FROM " + tableName + whereSql;
        return sql;
    }

    public  static String getUpdateSql(Class c,Map<String,Object> paramMap,String whereJPQL,String updateJPQL){
        String sql = "";
        String tableName = getTableName(c);
        String[] fieldNames = getObjectFieldNames(c);
        String whereSql = getWhereSQL(whereJPQL,paramMap,fieldNames,-1,-1,null);
        String updateSql = "";
        updateSql = replaceMap(updateJPQL,paramMap);
        updateSql = replaceFieldName(updateSql,fieldNames);
        sql = "UPDATE " + tableName + " SET " + updateSql + whereSql;
        return sql;
    }

    public static String getSelectSql(Class c,String operating,Map<String,Object> paramMap,String whereJPQL,int begin,int pageSize){
        String sql = "";
        String tableName = getTableName(c);
        String[] fieldNames = getObjectFieldNames(c);
        String[] sqls = {"SELECT ", " FROM "};
        sql = sqls[1] + tableName;
        String whereSql = getWhereSQL(whereJPQL,paramMap,fieldNames,begin,pageSize,operating);
        sql = sql + whereSql;
        switch (operating){
            case OPERATING_ALL:
                sql = sqls[0] + OPERATING_ALL + sql;
                break;
            case OPERATING_COUNT:
                sql = sqls[0] + OPERATING_COUNT + sql;
                break;
            default:
                throw new IllegalArgumentException("operating参数值错误");
        }
        return sql;
    }

    private static String getWhereSQL(String whereJPQL,Map<String,Object> paramMap,String[] fieldNames,int begin,int pageSize,String operating){
        String whereSql = "";
        if (StringUtils.isEmpty(whereJPQL)) return whereSql;
        whereSql = " WHERE " + whereJPQL;
        whereSql = replaceMap(whereSql,paramMap);
        whereSql = replaceFieldName(whereSql,fieldNames);
        if (OPERATING_ALL.equals(operating)) whereSql = whereSql + " limit " + begin + "," + pageSize;
        return whereSql;
    }

    private static String replaceMap(String sql,Map<String,Object> paramMap){
        for (Map.Entry<String,Object> entry:paramMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String replace = "";
            if (value instanceof String){
                replace = "'" + value.toString() + "'";
            }else if (value instanceof Integer ||value instanceof Long||value instanceof Double||value instanceof Float){
                replace = value.toString();
            }else if (value instanceof Date){
                Date date = (Date) value;
                replace = "'" + DateUtils.dateToString(date,DateUtils.TIME_PATTERN_YMDHMS) + "'";
            }else if (value instanceof List){
                List<Object> data = (List<Object>) value;
                for (int i = 0; i < data.size(); i++) {
                    Object son = data.get(i);
                    if (son instanceof String){
                        replace = replace + "'" + son.toString() + "'";
                    }else if (son instanceof Integer ||son instanceof Long||son instanceof Double||son instanceof Float){
                        replace = replace + son.toString();
                    }
                    if ((i+1)<data.size()) replace = replace + ",";
                }
            }
            sql = sql.replace(":"+key,replace);
        }
        return sql;
    }

    private static String replaceFieldName(String sql,String[] fieldNames){
        for (int i = 0; i < fieldNames.length; i++) {
            String[] temp = fieldNames[i].split(":");
            sql = sql.replace(temp[0],temp[1]);
        }
        return sql;
    }





    /***
     * 校验类型是否为基本类型
     * @param classType 校验类型
     * @return
     */
    private static boolean checkType(Class classType){
        String[] types = {"Long", "Integer", "Double", "Float", "Character", "Boolean"};
        String typeName = classType.getSimpleName();
        for(String type : types) if(typeName.equals(type)) return true;
        return false;
    }

    /***
     * 将一个List转为
     * @param list
     * @param type
     * @param <T>
     * @return
     */
    private static <T> List<T> jsonArrayToList(List<? extends Object> list, Class<T> type){
        return JSONArray.parseArray(JSONArray.toJSONString(list), type);
    }


}
