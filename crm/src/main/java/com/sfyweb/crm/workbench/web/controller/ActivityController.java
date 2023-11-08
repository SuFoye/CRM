package com.sfyweb.crm.workbench.web.controller;

import com.sfyweb.crm.commons.contants.Contants;
import com.sfyweb.crm.commons.domain.ReturnObject;
import com.sfyweb.crm.commons.utils.DateUtils;
import com.sfyweb.crm.commons.utils.HSSFUtils;
import com.sfyweb.crm.commons.utils.UUIDUtils;
import com.sfyweb.crm.settings.domain.User;
import com.sfyweb.crm.settings.service.UserService;
import com.sfyweb.crm.workbench.domain.Activity;
import com.sfyweb.crm.workbench.domain.ActivityRemark;
import com.sfyweb.crm.workbench.service.ActivityRemarkService;
import com.sfyweb.crm.workbench.service.ActivityService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Controller
public class ActivityController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRemarkService activityRemarkService;

    @RequestMapping("/workbench/activity/index.do")
    public String index(HttpServletRequest request){
        //调用service层方法查询所有用户
        List<User> userList = userService.queryAllUsers();
        //把数据保存到request作用域
        request.setAttribute("userList", userList);
        //请求转发到市场活动的主页面
        return "workbench/activity/index";
    }

    @RequestMapping("/workbench/activity/saveCreateActivity.do")
    @ResponseBody
    public Object saveCreateActivity(Activity activity, HttpSession session){

        User user = (User) session.getAttribute(Contants.SESSION_USER);

        //封装参数
        activity.setId(UUIDUtils.getUUID());
        activity.setCreateTime(DateUtils.formateDateTime(new Date()));
        activity.setCreateBy(user.getId());

        ReturnObject returnObject = new ReturnObject();

        try {
            //调用service层方法，创建市场活动
            int ret = activityService.saveCreateActivity(activity);
            if (ret > 0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试...");
            }

        } catch (Exception e) {
            e.printStackTrace();

            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/queryActivityByConditionForPage.do")
    @ResponseBody
    public Object queryActivityByConditionForPage(String name, String owner, String startDate, String endDate, int pageNo, int pageSize){
        //封装参数
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("owner", owner);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("beginNo", (pageNo - 1) * pageSize);
        map.put("pageSize", pageSize);
        //调用service层方法，查询数据
        List<Activity> activityList = activityService.queryActivityByConditionForPage(map);
        int totalRows = activityService.queryCountOfActivityByCondition(map);
        //根据查询结果生成响应信息
        Map<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("activityList", activityList);
        retMap.put("totalRows", totalRows);
        return retMap;
    }

    @RequestMapping("/workbench/activity/deleteActivityIds.do")
    @ResponseBody
    public Object deleteActivityIds(String[] id){
        ReturnObject returnObject = new ReturnObject();
        try {
            //调用service层方法，删除市场活动
            int ret = activityService.deleteActivityByIds(id);
            if (ret > 0) {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            } else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后重试...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/queryActivityById.do")
    @ResponseBody
    public Object queryActivityById(String id){
        //调用service层方法，查询市场活动
        Activity activity = activityService.queryActivityById(id);
        //根据查询结果，返回响应信息
        return activity;
    }

    @RequestMapping("/workbench/activity/saveEditActivity.do")
    @ResponseBody
    public Object saveEditActivity(Activity activity, HttpSession session){
        User user = (User) session.getAttribute(Contants.SESSION_USER);
        //进一步封装参数
        activity.setEditTime(DateUtils.formateDateTime(new Date()));
        activity.setEditBy(user.getId());

        ReturnObject returnObject = new ReturnObject();

        //调用service层方法，保存修改的市场活动
        try {
            int ret = activityService.saveEditActivity(activity);
            if (ret > 0){
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙，请稍后...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后...");
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/exportAllActivity.do")
    public void exportAllActivity(HttpServletResponse response) throws Exception{
        //调用service层方法，查询所有市场活动
        List<Activity> activityList = activityService.queryAllActivities();
        //创建excel文件，并且把activityList写入到excel文件中
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet sheet = hssfWorkbook.createSheet("市场活动列表");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("ID");
        cell = row.createCell(1);
        cell.setCellValue("所有者");
        cell = row.createCell(2);
        cell.setCellValue("名称");
        cell = row.createCell(3);
        cell.setCellValue("开始日期");
        cell = row.createCell(4);
        cell.setCellValue("结束日期");
        cell = row.createCell(5);
        cell.setCellValue("成本");
        cell = row.createCell(6);
        cell.setCellValue("描述");
        cell = row.createCell(7);
        cell.setCellValue("创建时间");
        cell = row.createCell(8);
        cell.setCellValue("创建者");
        cell = row.createCell(9);
        cell.setCellValue("修改时间");
        cell = row.createCell(10);
        cell.setCellValue("修改者");

        //遍历activityList，创建HSSFRow对象，生成所有的数据行
        if (activityList != null && activityList.size() > 0){
            for (int i = 0; i < activityList.size(); i++) {
                Activity activity = activityList.get(i);
                //每遍历一个市场活动，生成一行
                row = sheet.createRow(i + 1);
                //每一行创建11列，每一列的数据从activity中获取
                cell = row.createCell(0);
                cell.setCellValue(activity.getId());
                cell = row.createCell(1);
                cell.setCellValue(activity.getOwner());
                cell = row.createCell(2);
                cell.setCellValue(activity.getName());
                cell = row.createCell(3);
                cell.setCellValue(activity.getStartDate());
                cell = row.createCell(4);
                cell.setCellValue(activity.getEndDate());
                cell = row.createCell(5);
                cell.setCellValue(activity.getCost());
                cell = row.createCell(6);
                cell.setCellValue(activity.getDescription());
                cell = row.createCell(7);
                cell.setCellValue(activity.getCreateTime());
                cell = row.createCell(8);
                cell.setCellValue(activity.getCreateBy());
                cell = row.createCell(9);
                cell.setCellValue(activity.getEditTime());
                cell = row.createCell(10);
                cell.setCellValue(activity.getEditBy());
            }
        }

        /*
        //根据workbook对象生成excel文件
        OutputStream os = new FileOutputStream("F:\\idea_project_java\\crm-project\\activityList.xls");
        hssfWorkbook.write(os);
        //关闭资源
        os.close();
        hssfWorkbook.close();
         */

        //把生成的excel文件下载到客户端
        response.setContentType("application/octet-stream; charset = UTF-8");
        response.addHeader("Content-Disposition", "attachment; filename = activityList.xls");
        OutputStream out = response.getOutputStream();
        /*
        InputStream is = new FileInputStream("F:\\idea_project_java\\crm-project\\activityList.xls");
        byte[] buff = new byte[256];
        int len = 0;
        while ( (len = is.read(buff)) != -1 ){
            out.write(buff, 0, len);
        }
        is.close();
        out.flush();
         */

        //优化速度，避免反复从内存与磁盘之间读写数据，直接将hssfworkbook对象的数据写到输出流中传给浏览器
        hssfWorkbook.write(out);
        hssfWorkbook.close();
        out.flush();
    }

    @RequestMapping("/workbench/activity/exportActivitiesByIds.do")
    public void exportActivitiesByIds(String[] id, HttpServletResponse response) throws Exception{
        //调用service层方法查询市场活动
        List<Activity> activityList = activityService.queryActivitiesByIds(id);
        //创建excel文件，把activityList写入到excel文件中
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet sheet = hssfWorkbook.createSheet("市场活动列表");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("ID");
        cell = row.createCell(1);
        cell.setCellValue("所有者");
        cell = row.createCell(2);
        cell.setCellValue("名称");
        cell = row.createCell(3);
        cell.setCellValue("开始日期");
        cell = row.createCell(4);
        cell.setCellValue("结束日期");
        cell = row.createCell(5);
        cell.setCellValue("成本");
        cell = row.createCell(6);
        cell.setCellValue("描述");
        cell = row.createCell(7);
        cell.setCellValue("创建时间");
        cell = row.createCell(8);
        cell.setCellValue("创建者");
        cell = row.createCell(9);
        cell.setCellValue("修改时间");
        cell = row.createCell(10);
        cell.setCellValue("修改者");

        //遍历activityList，创建HSSFRow对象，生成所有的数据行
        if (activityList != null && activityList.size() > 0){
            for (int i = 0; i < activityList.size(); i++) {
                Activity activity = activityList.get(i);
                //每遍历一个activity，创建一行
                row = sheet.createRow(i + 1);
                //每生成一行，创建对应的列并填充数据
                cell = row.createCell(0);
                cell.setCellValue(activity.getId());
                cell = row.createCell(1);
                cell.setCellValue(activity.getOwner());
                cell = row.createCell(2);
                cell.setCellValue(activity.getName());
                cell = row.createCell(3);
                cell.setCellValue(activity.getStartDate());
                cell = row.createCell(4);
                cell.setCellValue(activity.getEndDate());
                cell = row.createCell(5);
                cell.setCellValue(activity.getCost());
                cell = row.createCell(6);
                cell.setCellValue(activity.getDescription());
                cell = row.createCell(7);
                cell.setCellValue(activity.getCreateTime());
                cell = row.createCell(8);
                cell.setCellValue(activity.getCreateBy());
                cell = row.createCell(9);
                cell.setCellValue(activity.getEditTime());
                cell = row.createCell(10);
                cell.setCellValue(activity.getEditBy());
            }
        }

        //把生成的excel文件响应给浏览器
        response.setContentType("application/octet-stream; charset = UTF-8");
        response.addHeader("Content-Disposition", "attachment; filename = activityList.xls");
        OutputStream out = response.getOutputStream();
        hssfWorkbook.write(out);
        hssfWorkbook.close();
        out.flush();
    }

    @RequestMapping("/workbench/activity/importActivity.do")
    @ResponseBody
    public Object importActivity(MultipartFile activityFile, HttpSession session) {
        User user = (User) session.getAttribute(Contants.SESSION_USER);
        ReturnObject returnObject = new ReturnObject();
        try {
            /*
            //把接收到的excel文件写到磁盘目录中
            String originalFilename = activityFile.getOriginalFilename();
            File file = new File("F:\\idea_project_java\\crm-project\\crm\\src\\main\\webapp\\WEB-INF\\file" + originalFilename);
            activityFile.transferTo(file);

            //解析excel文件，获取文件中的数据，并且封装成activityList
            InputStream is = new FileInputStream("F:\\idea_project_java\\crm-project\\crm\\src\\main\\webapp\\WEB-INF\\file" + originalFilename);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
            */

            InputStream is = activityFile.getInputStream();
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);

            //根据wb获取HSSFSheet对象，封装了一页的所有信息
            HSSFSheet sheet = hssfWorkbook.getSheetAt(0); //页的下标，下标从0开始，依次增加
            //根据sheet获取HSSFRow对象，封装了一行的所有信息
            HSSFRow row = null;
            HSSFCell cell = null;
            Activity activity = null;
            List<Activity> activityList = new ArrayList<Activity>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { //sheet.getLastRowNum()：最后一行的下标
                row = sheet.getRow(i); //行的下标，下标从1开始（除去表头），依次增加
                activity = new Activity(); //每一行对应一个实体类对象

                //给用户写数据的excel模板提前设置好表头和单元格数据类型，生成部分市场活动信息
                activity.setId(UUIDUtils.getUUID()); //生成活动id
                activity.setOwner(user.getId()); //谁导入谁是活动的所有者
                activity.setCreateTime(DateUtils.formateDateTime(new Date())); //活动生成时间，即当前系统时间
                activity.setCreateBy(user.getId()); //谁导入谁是创建活动的人

                for (int j = 0; j < row.getLastCellNum(); j++) { //row.getLastCellNum():最后一列的下标+1
                    //根据row获取HSSFCell对象，封装了一列的所有信息
                    cell = row.getCell(j); //列的下标，下标从0开始，依次增加
                    //获取列中的数据
                    String cellValue = HSSFUtils.getCellValueForStr(cell);
                    if (j == 0) {
                        activity.setName(cellValue);
                    } else if (j == 1) {
                        activity.setStartDate(cellValue);
                    } else if (j == 2) {
                        activity.setEndDate(cellValue);
                    } else if (j == 3) {
                        activity.setCost(cellValue);
                    } else if (j == 4) {
                        activity.setDescription(cellValue);
                    }
                }

                //把每一行中所有列都封装之后，把activity保存到list中
                activityList.add(activity);
            }

            //调用service层方法，保存导入的市场活动
            int ret = activityService.saveCreateActivityByList(activityList);

            returnObject.setCode(Contants.RETURN_OBJECT_CODE_SUCCESS);
            returnObject.setRetData(ret);
        } catch (IOException e) {
            e.printStackTrace();
            returnObject.setCode(Contants.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙，请稍后重试...");
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/detailActivity.do")
    public String detailActivity(String id, HttpServletRequest request){
        //调用service层方法，查询数据
        Activity activity = activityService.queryActivityForDetailById(id);
        List<ActivityRemark>  remarkList = activityRemarkService.queryActivityRemarkForDetailByActivityId(id);
        //把数据保存到request中
        request.setAttribute("activity", activity);
        request.setAttribute("remarkList", remarkList);
        //请求转发
        return "workbench/activity/detail";
    }

    @RequestMapping("/workbench/activity/downloadModel.do")
    public void downloadModel(HttpServletResponse response) throws Exception{
        //设置响应类型和浏览器下载
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=activityListModel.xls");

        //获取模板文件和响应输出流
        InputStream is = new FileInputStream("F:\\idea_project_java\\crm-project\\crm\\src\\main\\webapp\\WEB-INF\\file\\activityListModel.xls");
        OutputStream os = response.getOutputStream();

        //读取模板文件内容输出到浏览器
        byte[] buff = new byte[256];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            os.write(buff, 0, len);
        }

        //关闭资源
        is.close();
        os.flush();
    }
}