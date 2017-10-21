package cn.edu.pku.sei.projectDataManagement.appender;


import cn.edu.pku.sei.tsr.sql.SqlConnector;

import java.sql.ResultSet;
import java.util.ResourceBundle;

/**
 * Created by oliver on 2017/8/11.
 */
public class LabelTaskBrowserAppender {
    private static final int TASK_PER_PAGE = 3;
    private static String taskBrowserFooterAppendString = "";
    private static String[] taskBrowserTableHeader;


    public static String getTaskBrowserFooterAppendString(){
        return taskBrowserFooterAppendString;
    }

    public static String getTaskBrowserAppendString(int index){
        String result = "";

        ResourceBundle bundle = ResourceBundle.getBundle("database");
        String url = bundle.getString("platformInfoDatabaseURL");
        String user = bundle.getString("platformInfoDatabaseUser");
        String pwd = bundle.getString("platformInfoDatabasePwd");
        String driver = bundle.getString("platformInfoDatabaseDriver");
        String tableName = bundle.getString("tasksTableName");

        SqlConnector conn = null ;
        try{
            conn = new SqlConnector(url , user , pwd , driver);
            conn.start();
            String sql = "select count(taskID) from " + tableName;
            conn.start();
            conn.setPreparedStatement(sql);
            ResultSet rs = conn.executeQuery();
            int taskCount = 0;
            if(rs.next()){
                taskCount = rs.getInt(1);
            }

            // initiate for the table header for taskBrowser
            initTaskTableHeader();
            result += appendAnTaskToTaskBrowser(taskBrowserTableHeader , null);


            int display = 0 ; // 记录实际上有几个显示
            int end = (index + 1)* TASK_PER_PAGE;
            end = end > taskCount ? taskCount : end;
            display = end - index * TASK_PER_PAGE;
            if(end > 0 && display > 0){
                sql = "select * from (select * from (select * from (select taskID , userID , tableName ,databaseURL , databaseUser from tasks order by taskID limit ENDINDEX ) a order by taskID desc) b limit DISPLAY ) c order by taskID asc" ;
                sql = sql.replace("ENDINDEX" , end + "").replace("DISPLAY" , display + "");
                conn.setPreparedStatement(sql);
                rs = conn.executeQuery();
                int columnCount = rs.getMetaData().getColumnCount();
                while(rs.next()){
                    String[] columns = new String[columnCount];
                    for(int i = 0 ; i < columnCount ; i++)
                        columns[i] = rs.getString(i + 1);
                    result += appendAnTaskToTaskBrowser(columns , new String[1]);
                }

                //
                getTaskBrowserFooterAppendString(index , taskCount);
            }
        }catch(Exception e){
            result = "";
            System.out.println(e.getMessage());
        }finally{
            conn.close();
            return result;
        }

    }

    private static void getTaskBrowserFooterAppendString(int index , int taskCount){
        taskBrowserFooterAppendString = "";

        String template =
                "<li>" +
                    "<a href='labelTaskBrowser.jsp?index=INDEXNUMBER'>DISPLAY</a>" +
                "</li>";

        if(index > 0){
            taskBrowserFooterAppendString += template.replace("INDEXNUMBER" , (index - 1) + "").replace("DISPLAY" , "«");
        }
        taskBrowserFooterAppendString += template.replace("INDEXNUMBER" , index + "").replace("DISPLAY" , index + 1 +"");
        if((index + 1) * TASK_PER_PAGE < taskCount){
            taskBrowserFooterAppendString += template.replace("INDEXNUMBER" , index + 1 + "").replace("DISPLAY" , "»");
        }
    }

    /**
     * when otherInfo equals null , this function will add the table head to taskTable
     * @param columns
     * @param otherInfo
     * @return
     */
    private static String appendAnTaskToTaskBrowser(String[] columns , String[] otherInfo){
        String result = "";
        String ul_otherInfo = "";
        if(otherInfo != null) {
            ul_otherInfo =
                "<ul class='treeview-menu'>" +
                    "<li>" +
                        "<div style='margin:10px'>" +
                            "<input type='text' name='q' class='form-control' placeholder='Search...'>" +
                            "<a href='labelTask.jsp?taskID="+ columns[0] + "&pageIndex=0&dataIndex=0'>加入</a>" +
                        "</div>" +
                    "</li>" +
                "</ul>";
        }

        result =
            "<li style='list-style-type:none;margin:0px;padding:0px' class='treeview'>" +
                "<a href='http://www.baidu.com'>" +
                    "<span>" +
                        "<table class='table table-bordered' style='margin:0px'>" +
                            "<tbody>" +
                                "<tr id='tasksdTable'>" +
                                    getAnTaskTableString(columns , otherInfo == null) +
                                "</tr>" +
                            "</tbody>" +
                        "</table>" +
                    "</span>" +
                "</a>" +
                ul_otherInfo +
            "</li>" ;
        return result;
    }

    /**
     * every task will show as an table in taskBrowser, this function is used to get the html string
     * @param columns : the meta data of a task
     * @return
     */
    private static String getAnTaskTableString(String[] columns , boolean isTableHeader){
        String result =
                "<th class='task-ID'>"+ columns[0]+"</th>" +
                "<th class='task-creator'>" + columns[1] + "</th>" +
                "<th class='task-tableName'>" + columns[2] + "</th>" +
                "<th class='task-databaseURL'>" + columns[3] + "</th>" +
                "<th class='task-databaseUser'>" + columns[4] + "</th>" ;
        if(isTableHeader)
                result += "<th class='task-operation'>" + columns[5]+ "</th>";
        else{
            result += (
                    "<th class='task-operation'>" +
                        "<button href='labelTask.jsp?taskID="+ columns[0] + "'>加入</a>" +
                    "</th>"
            );
        }
        return result;
    }

    private static void initTaskTableHeader(){
        taskBrowserTableHeader = new String[6];
        taskBrowserTableHeader[0] = "#";
        taskBrowserTableHeader[1] = "创建者";
        taskBrowserTableHeader[2] = "数据表名";
        taskBrowserTableHeader[3] = "数据库地址";
        taskBrowserTableHeader[4] = "数据库用户";
        taskBrowserTableHeader[5] = "操作";
    }

}
