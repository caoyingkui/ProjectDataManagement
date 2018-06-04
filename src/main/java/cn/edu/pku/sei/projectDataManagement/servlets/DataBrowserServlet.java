package cn.edu.pku.sei.projectDataManagement.servlets;

import cn.edu.pku.sei.projectDataManagement.data.PathInfo;
import cn.edu.pku.sei.projectDataManagement.data.PathType;
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
        DownloadServlet ds = new DownloadServlet();
        DownloadServlet.test t = ds.new test();
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
        List<PathInfo> pathInfos = new ArrayList<PathInfo>();
        if(virtualPath.startsWith("\\projects")){
            String[] args = virtualPath.substring(1).split("\\\\");
            if(args.length == 1){// "\\projects"

                for(String project : Directory.getProjects()){
                    pathInfos.add(new PathInfo(project));
                }
            }else if(args.length == 2){
                String project = args[1];

                for(String dataType : Directory.dataTypePaths.keySet()){
                    Map<String, String> dataTypePath = Directory.dataTypePaths.get(dataType);
                    if(dataTypePath.containsKey(project)){
                        pathInfos.add(new PathInfo(project , dataType));
                    }
                }
            }else if(args.length > 2){
                List<File> files = Directory.getSubDirByVirtualPath(virtualPath);
                for(File file : files){
                    pathInfos.add(new PathInfo(file , "projects"));
                }
            }
        }else if(virtualPath.startsWith("\\dataType")){
            List<File> files = Directory.getSubDirByVirtualPath(virtualPath);
            for(File file : files){
                pathInfos.add(new PathInfo(file , "dataType"));
            }
        }

        pathInfos.sort(new Comparator<PathInfo>() {
            @Override
            public int compare(PathInfo o1, PathInfo o2) {
                if(o1.getType() == PathType.DIRECTORY && o2.getType() == PathType.FILE)
                    return -1;
                else if(o2.getType() == PathType.DIRECTORY && o1.getType() == PathType.FILE)
                    return 1;
                else {
                    int result = o1.getFileName().compareTo(o2.getFileName());
                    return result == 0 ? 0 : (result > 0 ? 1 : -1);
                }
            }
        });
        return PathInfo.toJSONObject(pathInfos.toArray(new PathInfo[0]) , "Directory" , virtualPath);
    }

    private JSONObject searchDirectory(String query){
        query = query.replaceAll("[ ]+" , " ");
        String[] parameters = query.split(" ");
        Set<String> dataTypeSet = new HashSet<String>();
        Set<String> projectSet = new HashSet<String>();

        //region <get all the data types and projects which are contained in the query>
        for(String parameter : parameters){
            String similarType = getSimilarType(parameter);
            if(similarType.length() > 0)
                dataTypeSet.add(similarType);

            List<String> similarProjects = getSimilarProjects(parameter);
            if(similarProjects.size() > 0)
                projectSet.addAll(similarProjects);
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
                    virtualPath = "\\projects\\" + project + "\\" + type;
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

    private static String getSimilarType(String type){
        String result = "";
        for(String similarType : dataTypes.keySet()){
            if(similarType.toLowerCase().contains(type.trim().toLowerCase())){
                result = similarType;
            }
        }
        return result;
    }

    private static List<String> getSimilarProjects(String project){
        List<String> result = new ArrayList<String>();
        for(String similarProject : projects){
            if(similarProject.toLowerCase().contains(project.trim().toLowerCase())){
                result.add(similarProject);
            }
        }
        return result;
    }

    private static void initialize(){
        dataTypes = Directory.getDataTypes();
        projects = Directory.getProjects();
    }


}
