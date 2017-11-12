package cn.edu.pku.sei.projectDataManagement.servlets;

import cn.edu.pku.sei.projectDataManagement.data.PathInfo;
import cn.edu.pku.sei.projectDataManagement.util.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
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

    public static void main(String[] args) throws IOException {
        DataBrowserServlet servlet = new DataBrowserServlet();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String query = reader.readLine();
            JSONObject result = servlet.searchDirectory(query);
            System.out.println(result.toString());
        }


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
            result = searchDirectory(request.getParameter("query"));
            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }
    }

    private JSONObject browseDirectory(String virtualPath){
        JSONObject result = new JSONObject();

        if(virtualPath.compareTo("\\projects") == 0){
            List<File> files = Directory.findAllProjects();
            List<PathInfo> pathInfos = new ArrayList<PathInfo>();
            for(File file : files){
                PathInfo info = new PathInfo(file , "projects");
                String dir = info.getDir();
                String path[] = dir.split("\\\\");
                info.setDir("\\" + path[1] + "\\" + path[3]);
                pathInfos.add(info);

            }
            result = PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "Directory" , virtualPath);
        }else{
            String realPath = Directory.virtualPathToRealPath(virtualPath);
            File temp = new File(realPath);
            if(  (  !temp.exists() || //  \projects\lucene , can not map to a real path , but it is valid
                    temp.exists() && temp.isDirectory()) //is a directory
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
                    virtualPath = "\\dataType\\" + dataTypes.get(dataType)[1] + "\\" + project; // value[1] stores the root name
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
                for(String type : dataTypes.keySet()){
                    virtualPath = "\\projects\\" + project + "\\" + dataTypes.get(type)[1];
                    realPath = Directory.virtualPathToRealPath(virtualPath);
                    File file = new File(realPath);
                    if(file.exists()){
                        pathInfos.add(new PathInfo(file , "projects"));
                    }
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
        dataTypes = Directory.getDataTypes();
        projects = Directory.getProjects();
    }


}
