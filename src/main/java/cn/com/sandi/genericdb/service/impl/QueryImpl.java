package cn.com.sandi.genericdb.service.impl;

import cn.com.sandi.genericdb.service.Query;
import cn.com.sandi.genericdb.utils.GenericUtils;
import cn.com.sandi.genericdb.vo.PageQueryCriteria;
import cn.com.sandi.genericdb.vo.QueryCriteria;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.SUPPORTS,readOnly = true)
@Service
@Slf4j
public class QueryImpl implements Query {

    @Resource
    private SqlSession sqlSession;

    @Override
    public int queryForCount(String statementName, QueryCriteria criteria) {
        return sqlSession.selectOne(statementName,criteria);
    }

    @Override
    public int queryForCount(String statementName, HashMap<String, Object> paramMap) {
        return sqlSession.selectOne(statementName,paramMap);
    }

    @Override
    public <T> List<T> queryForList(String statementName) {
        return sqlSession.selectList(statementName,null,new RowBounds(0, GenericUtils.SELECT_MAX_CNT));
    }

    @Override
    public <T> List<T> queryForList(String statementName, QueryCriteria criteria) {
        return sqlSession.selectList(statementName,criteria,new RowBounds(0, GenericUtils.SELECT_MAX_CNT));
    }

    @Override
    public <T> List<T> queryForList(String statementName, HashMap<String, Object> paramMap) {
        return sqlSession.selectList(statementName,paramMap,new RowBounds(0, GenericUtils.SELECT_MAX_CNT));
    }

    @Override
    public <T> List<T> queryForListFull(String statementName) {
        return sqlSession.selectList(statementName);
    }

    @Override
    public <T> List<T> queryForListFull(String statementName, QueryCriteria criteria) {
        return sqlSession.selectList(statementName,criteria);
    }

    @Override
    public <T> List<T> queryForListFull(String statementName, HashMap<String, Object> paramMap) {
        return sqlSession.selectList(statementName,paramMap);
    }

    @Override
    public JSONObject queryForList(String listSqlName, String countSqlName, PageQueryCriteria criteria) {
        JSONObject queryListMap = new JSONObject();
        queryListMap.put("total",0);
        queryListMap.put("rows",new JSONArray());
        criteria.setIndex((criteria.getCurrentPage()-1)*criteria.getRows());
        List<?> queryList = this.queryForList(listSqlName,criteria);
        int size = this.queryForCount(countSqlName,criteria);
        queryListMap.put("total",size);
        queryListMap.put("rows",queryList);
        return queryListMap;
    }

    @Override
    public byte[] queryToExcel(String statementName, HashMap<String, Object> paramMap, String excelName, JSONArray clumnFields, JSONObject transFields) {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = null;
        try{
            List<?> listData = queryForList(statementName,paramMap);
            createExcel(workBook,sheet,1,excelName, clumnFields, transFields, listData);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workBook.write(outputStream);
            byte[] result = outputStream.toByteArray();
            return  result;
        }catch(Exception e){
            return null;
        }finally{
            if(workBook!=null) try{workBook.close();}catch(Exception e){log.error("关闭Excel对象失败",e);}
            sheet=null;
            workBook=null;
        }
    }

    @Override
    public byte[] queryToExcel(String statementName, PageQueryCriteria criteria, String excelName, JSONArray clumnFields, JSONObject transFields, boolean all) throws Exception {
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = null;
        try{
            if(criteria==null){	//只创建一个带列的空EXCEL
                createExcel(workBook,sheet,1,excelName, clumnFields, transFields, null);
            }else{
                if(!all){
                    List<?> listData = queryForList(statementName,criteria);
                    createExcel(workBook,sheet,1,excelName, clumnFields, transFields, listData);
                }else{
                    int currentPage = 0, rows= GenericUtils.SELECT_MAX_CNT,excelRowIdx=1;
                    criteria.setRows(rows);
                    criteria.setIndex((criteria.getCurrentPage() - 1) * criteria.getRows());
                    List<?> listData = queryForList(statementName,criteria);
                    //分页从数据库中获取数据
                    while(listData!=null && listData.size()>0){
                        sheet=createExcel(workBook,sheet,excelRowIdx,excelName, clumnFields, transFields, listData);
                        currentPage = criteria.getCurrentPage();
                        criteria.setCurrentPage(currentPage+1);
                        criteria.setIndex((criteria.getCurrentPage() - 1) * criteria.getRows());
                        excelRowIdx+=listData.size();
                        if(checkSize(workBook)){
                            List<Map<String, Object>> endData = new ArrayList<Map<String,Object>>();
                            Map<String, Object> paramsMap = new HashMap<String, Object>();
                            String field = clumnFields.getJSONObject(0).getString("field");
                            paramsMap.put(field, GenericUtils.WARNING_MESSAGE);
                            endData.add(paramsMap);
                            sheet = createExcel(workBook,sheet,excelRowIdx,excelName, clumnFields, transFields, endData);
                            break;
                        }
                        listData = queryForList(statementName,criteria);
                    }
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workBook.write(outputStream);
            byte[] result = outputStream.toByteArray();
            return  result;
        }catch(Exception e){
            throw new Exception(e);
        }finally{
            if(workBook!=null) try{workBook.close();}catch(Exception e){log.error("关闭Excel对象失败",e);}
            sheet=null;
            workBook=null;
        }
    }

    private HSSFSheet createExcel(HSSFWorkbook workBook, HSSFSheet sheet, int rowIdx, String excelName, JSONArray clumnFields, JSONObject transFields, List<?> listData){
        if(sheet==null){
            // 工作表
            sheet = workBook.createSheet(excelName);
            // 标题样式
            HSSFCellStyle csTitle = workBook.createCellStyle();
            csTitle.setAlignment(HorizontalAlignment.CENTER);
            csTitle.setVerticalAlignment(VerticalAlignment.CENTER);
            csTitle.setBorderBottom(BorderStyle.THIN);
            csTitle.setBorderLeft(BorderStyle.THIN);
            csTitle.setBorderRight(BorderStyle.THIN);
            csTitle.setBorderTop(BorderStyle.THIN);

            // 创建字体
            HSSFFont ffTitle = workBook.createFont();

            ffTitle.setFontHeightInPoints((short) 10);// 字体大小
            ffTitle.setFontName("黑体 "); // 字体
            ffTitle.setBold(true);
            csTitle.setFont(ffTitle);// 放入样式中

            // 正文样式
            HSSFCellStyle csText = workBook.createCellStyle();
            csText.setBorderBottom(BorderStyle.THIN);// 下边框
            csText.setBorderLeft(BorderStyle.THIN);// 左边框
            csText.setBorderRight(BorderStyle.THIN);// 右边框
            csText.setBorderTop(BorderStyle.THIN);// 上边框
            HSSFFont ffText = workBook.createFont();
            ffText.setFontHeightInPoints((short) 9);// 字体大小
            csText.setFont(ffText);

            HSSFRow row = null;
            // 设置列宽
            sheet.setColumnWidth(0, 2500);
            sheet.setColumnWidth(1, 2500);
            sheet.setColumnWidth(2, 2500);
            sheet.setColumnWidth(3, 4000);

            row = sheet.createRow((short) 0);
            row.setHeight((short) 400);
            // 首行
            for (int i = 0; i < clumnFields.size(); i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(new HSSFRichTextString(clumnFields.getJSONObject(i).get("title").toString()));
                cell.setCellStyle(csTitle);
            }
        }

        //int rowNum = 1;
        if (listData != null) {
            // 正文样式
            HSSFCellStyle csText = workBook.createCellStyle();
            csText.setBorderBottom(BorderStyle.THIN);// 下边框
            csText.setBorderLeft(BorderStyle.THIN);// 左边框
            csText.setBorderRight(BorderStyle.THIN);// 右边框
            csText.setBorderTop(BorderStyle.THIN);// 上边框
            HSSFFont ffText = workBook.createFont();
            ffText.setFontHeightInPoints((short) 9);// 字体大小
            csText.setFont(ffText);

            for(Object obj:listData){
                Map<String, Object> map = (Map<String, Object>) obj;
                HSSFRow row = sheet.createRow((short) (rowIdx));
                for (int i = 0; i < clumnFields.size(); i++) {
                    String field = clumnFields.getJSONObject(i).getString("field");
                    HSSFCell cell = row.createCell(i);
                    String val = getFieldValue(field, map.get(field), transFields);
                    cell.setCellValue(new HSSFRichTextString(val));
                    cell.setCellStyle(csText);
                }
                rowIdx++;
            }
        }
        return sheet;
    }

    private String getFieldValue(String field , Object value, JSONObject transFields){
        if(value==null) return "";
        String result = value.toString();
        if(StringUtils.isEmpty(field) || transFields==null || transFields.isEmpty() || !transFields.containsKey(field)) return result;
        JSONArray listDict = transFields.getJSONArray(field);
        JSONObject json = null;
        for(int i =0;i<listDict.size();i++){
            json = listDict.getJSONObject(i);
            if(json.getString("ID").equals(value.toString())) result = json.getString("NAME");
        }
        return result;
    }

    private Boolean checkSize(HSSFWorkbook workBook) throws IOException {
        byte[] result = workBook.getBytes();
        if(result.length>(GenericUtils.MAX_EXPORT_FILE*1024)) return true;
        return false;
    }
}
