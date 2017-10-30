package cn.edu.pku.sei.projectDataManagement.servlets;

import cn.edu.pku.sei.projectDataManagement.data.PathInfo;
import cn.edu.pku.sei.projectDataManagement.util.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by oliver on 2017/10/15.
 */
public class DataBrowserServlet extends HttpServlet {
    static List<String> dataTypes;
    static{
        dataTypes = new ArrayList<String>();
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        Collections.addAll(dataTypes , bundle.getString("DataTypes").split("|"));

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request , response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("test");

        String requestType = request.getParameter("requestType");
        JSONObject result = new JSONObject();

        if(requestType.compareTo("browseDirectory") == 0){
            result = browseDirectory(request , response);
            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }else if(requestType.compareTo("searchDirctory") == 0){
            String project = request.getParameter("project").trim();
            String dataType = requestType.getParameter("dataType").trim();
            String virtualPath = "";
            String realPath = "";
            if( (project == null || project.length() == 0 ) &&
                    (dataType == null || dataType.length == 0) ){
                result.put("dataType" , "searchFailed");
                result.put("errorLog" , "The parameter(s) for searching is/are not valid!");
            }else{
                if(project == null || project.length() == 0){
                    virutalPath = "\\dataType\\" + dataType;
                }else if( (dataType == null || dataType.length() == 0)){
                    virtualPath = "\\projects";
                }else{
                    virtualPath = "\\dataType\\" + dataType + "\\" + project;
                }
                realPath = Directory.virtualPathToRealPath(virtualPath);
                if(new File(realPath).exists()){
                    request.setAttribute("directory" , virtualPath);
                    result = browseDirectory(request , response);
                }else{
                    result.put("dataType" , "searchFailed");
                    result.put("errorLog" , "No result found!");
                }
            }
            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }
    }

    private JSONObject browseDirectory(HttpServletRequest request, HttpServletResponse response){
        System.out.println("start!");
        JSONObject result = new JSONObject();
        String virtualPath = request.getParameter("directory");
        String realPath = Directory.virtualPathToRealPath(virtualPath);
        File temp = new File(realPath);
        if(  (temp.exists() && temp.isDirectory()) ||  //is a directory
                !temp.exists() //  \projects\lucene , can not map to a real path , but it is valid
                ){
            List<File> files = Directory.getSubDirByVirtualPath(virtualPath);
            List<PathInfo> pathInfos = new ArrayList<PathInfo>();
            String type = virtualPath.startsWith("\\projects") ? "projects" : "dataType";
            for(File file : files){
                pathInfos.add(new PathInfo(file , type));
            }
            result = PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "Directory" , virtualPath);
        }else{
            result = null;
        }
        return result;
    }

    private static boolean contiansDataType(String type){
        return dataTypes.contains(type);
    }

}
