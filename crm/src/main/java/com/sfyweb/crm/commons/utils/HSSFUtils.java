package com.sfyweb.crm.commons.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * 关于excel文件操作的工具类
 */
public class HSSFUtils {

    /**
     * 从指定的HSSFCell对象中获取列的值
     * @return
     */
    public static String getCellValueForStr(HSSFCell hssfCell){
        String ret = "";
        if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_STRING){ //字符串
            ret = hssfCell.getStringCellValue();
        } else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){ //数值型
            ret = hssfCell.getNumericCellValue() + "";
        } else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){ //布尔型
            ret = hssfCell.getBooleanCellValue() + "";
        } else if (hssfCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA){ //公式型
            ret = hssfCell.getCellFormula() + "";
        } else { //错误型和空类型
            ret = "";
        }

        return ret;
    }
}
