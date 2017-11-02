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

    // the value is a array of string, which all are the attributes of a kind of data type;
    // value[0] : the data root of the data. all types are stored in lower case
    static Map<String , String[]> dataTypes;
    static Set<String> projects;
    static{
        initialize();
    }

    public static void main(String[] args){
        DataBrowserServlet servlet = new DataBrowserServlet();
        servlet.searchDirectory("bug lucene");

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
        }else if(requestType.compareTo("searchDiretory") == 0){
            result = searchDirectory(request.getParameter("query"));
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
            if(containsDataType(parameter))
                dataTypeSet.add(parameter);

            if(containsProject(parameter))
                projectSet.add(parameter);

        }
        //endregion <get all the dataType and projects which are contained in the query>


        List<PathInfo> pathInfos = new ArrayList<PathInfo>();

        String virtualPath ;
        String realPath;
        if(dataTypeSet.size() > 0 && projectSet.size() > 0){
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
        return dataTypes.containsKey(type);
    }

    private static boolean containsProject(String project){
        project = project.toLowerCase();
        return projects.contains(project);
    }

    private static void initialize(){
        initializeDataTypesMap();
        initializeProjectsSet();
    }

    private static void initializeDataTypesMap(){
        dataTypes = new HashMap<String, String[]>();
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");

        String[] types = bundle.getString("DataTypes").split("\\|");
        String rootPrefix = "Root_";
        for(String type : types){
            //region <get attributes>
            String root = bundle.getString(rootPrefix + type);
            //endregion <get attributes>

            //region <put attributes into a string array>
            String[] attributes = new String[1];
            attributes[0] = root;
            //endregion <put attributes into a string array>

            dataTypes.put(type.toLowerCase() , attributes);
        }
    }

    private static void initializeProjectsSet(){
        // TODO
        if(dataTypes == null)
            return ;
        projects = new HashSet<String>();
        for(String type : dataTypes.keySet()){
            String root = dataTypes.get(type)[0]; // value[0] stores the root of the type
            File rootFile = new File(root);
            if(rootFile.exists() && rootFile.isDirectory()){
                File[] fileList = rootFile.listFiles();

                for(File subFile : fileList){
                    if(subFile.isDirectory()){
                        projects.add(subFile.getName().toLowerCase());
                    }
                }

            }
        }
    }

}
