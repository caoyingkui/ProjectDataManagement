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
    static Set<String> projects;
    static{
        dataTypes = new ArrayList<String>();
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        Collections.addAll(dataTypes , bundle.getString("DataTypes").split("|"));

        projects = getAllProjects();
    }

    public static void main(String[] args){
        DataBrowserServlet servlet = new DataBrowserServlet();
        servlet.searchDirecotry("bug lucene");

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
            result = browseDirectory(request.getParameter("directory"));
            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }else if(requestType.compareTo("searchDirectory") == 0){
            result = searchDirecotry(request.getParameter("query"));
            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }
    }

    private JSONObject browseDirectory(String virtualPath){
        JSONObject result = new JSONObject();
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

    private JSONObject searchDirectory(String query){
        query = query.replaceAll("[ ]+" , " ");
        String[] parameters = query.split(" ");
        Set<String> dataTypeSet = new HashSet<String>();
        Set<String> projectSet = new HashSet<String>();

        //region <get all the data types and projects which are contained in the query>
        for(String parameter : parameters){
            parameter = parameter.toLowerCase();
            for(String type : dataTypes){
                if(type.toLowerCase().compareTo(parameter) == 0){
                    dataTypeSet.add(type);
                }
            }

            for(String project : projects){
                if(project.toLowerCase().compareTo(parameter) == 0){
                    projectSet.add(project);
                }
            }

        }
        //endregion <get all the dataType and projects which are contained in the query>


        List<PathInfo> pathInfos = new ArrayList<PathInfo>();

        if(dataTypeSet.size() > 0 && projectSet.size() > 0){
            String virtualPath ;
            String realPath;
            for(String dataType : dataTypeSet){
                for(String project : projectSet){
                    virtualPath = "\\dataType\\" + dataType + "\\" + project;
                    realPath = Directory.virtualPathToRealPath(virtualPath);
                    File file = new File(realPath);
                    if(file.exists()){
                        pathInfos.add(new PathInfo(file , "dataType"));
                    }
                }
            }

            return PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "searchResult" , null);
        }else if(dataTypeSet.size() == 0 && projectSet.size() > 0){
            String virtualPath;
            String realPath;
            for(String project : projectSet){
                virtualPath = "\\projects\\" + project;
                realPath = Directory.virtualPathToRealPath(virtualPath);
                File file = new File(realPath);
                if(file.exists()){
                    pathInfos.add(new PathInfo(file , "projects"));
                }
            }
            return PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "searchResult" , null);
        }else if(dataTypeSet.size () > 0 && projectSet.size() == 0){
            String virtualPath;
            String realPath;
            for(String dataType : dataTypeSet){
                virtualPath = "\\dataType\\" + dataType;
                realPath = Directory.virtualPathToRealPath(virtualPath);
                File file = new File(realPath);
                if(file.exists()){
                    List<File> subFiles = Directory.getSubDirByVirtualPath(virtualPath);
                    for(File subFile : subFiles){
                        pathInfos.add(new PathInfo(subFile , "dataType"));
                    }
                }
            }
            return PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "searchResult" , null);
        }
        else{
            JSONObject result = new JSONObject();
            result.put("dataType" , "searchFailed");
            result.put("errorLog" , "No file found!");
            return result;
        }
    }

    private static boolean containsDataType(String type){
        type = type.toLowerCase();
        for(String dataType : datatypes){
            if(dataType.toLowerCase().compareTo(type) == 0) return true;
        }

        return false;
    }

    private static boolean containsProject(String project){
        project = project.toLowerCase();
        for(String p : projects){
            if(p.toLowerCase().compareTo(project) == 0) return true;
        }
        return false;
    }

    private static Set<String> getAllProjects(){
        Set<String> result = new HashSet<String>();
        result.add("lucene");
        result.add("Stackoverflow");
        result.add("Email");
        result.add("Commit");
        return result;
    }

}
