package cn.edu.pku.sei.projectDataManagement.util;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.PathLevel;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2017/10/15.
 */
public class Directory {
    private static String root = "";
    private static Map<String , String[]> dataTypes;
    private static Set<String> projects;
    static{
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        initializeDataTypesMap();
        initializeProjectsSet();
        root = bundle.getString("DataRoot");
    }

    public static Map<String ,String[]> getDataTypes(){
        return dataTypes;
    }

    public static Set<String> getProjects(){
        return projects;
    }


    private static List<String> virPaths = new ArrayList<String>();
    public static void test(File file){
        File[] files = file.listFiles();
        String path = "";
        for(File f : files){
            //System.out.println(f.getAbsolutePath());
            //path = realPathToVirtualPath(f.getAbsolutePath() , "projects");
            virPaths.add(path);
            //path = realPathToVirtualPath(f.getAbsolutePath() , "dataType");
            virPaths.add(path);
            //System.out.println();
            //System.out.println(realPathToVirtualPath(f.getAbsolutePath() , "dataType"));
            //System.out.println();
        }
        for(File f : files){
            if(f.isDirectory()){
                test(f);
            }
        }
    }

    public static void main(String[] args){

        File file = new File("D:\\projectddddDataManagement");
        test(file);
        List<File> files;
        for(String path:virPaths){
            if(path != null){
                path = "\\projects\\test";
                System.out.println(path);
                files = getSubDirByVirtualPath(path);
                for(File f : files){
                   // System.out.println(" " + realPathToVirtualPath(f.getAbsolutePath(), "projects"));
                }
            }
            int i = 0;
            i ++ ;
        }
    }

    //region <path conversion>
    /**
     * All the virtualPath will start with 1:\projects  , like \projects\project1 , which means browse the data by project category
     *                                     2:\dataType , like \dataType\email , which means browser the data by data type category
     * @param virtualPath
     * @return the corresponding real path of the virtual path.
     *          However, there will be a special case , which will not map to a real path ,this is \projects\project1
     *          in fact , all the data is stored by the dataType category, so all the data's path will be like \root\category\project\...
     *          category will take: email,commit and so on. project will take one of the 170 Apache projects name.
     *          so , when the virtualPath is \projects\project1 , it can not map to a real path
     *          if we add subDirectory to it ,like \projects\projects\email , it can successfully map to \root\email\projects\...
     */
    public static String virtualPathToRealPath(String virtualPath){
        System.out.println("in");
        String result = "";
        String oriPath = virtualPath;
        virtualPath = virtualPath.substring(1); // remove the first char '\'
        String paths[] = virtualPath.split("\\\\");

        try {
            if(paths[0].compareTo("projects") == 0){
                // this is a special case which return value is not a real path
                if(paths.length == 2){
                    result = virtualPath;
                }else if(paths.length > 2){
                    // path[1]: projectName; path[2]: category
                    result = root ;
                    result += ("\\" + paths[2]);
                    result += ("\\" + paths[1]);
                    for(int i = 3 ; i < paths.length ; i ++){
                        result += ("\\" + paths[i]);
                    }
                }else{
                    throw new VirtualPathIllegal(oriPath);
                }
            }else if(paths[0].compareTo("dataType") == 0){
                result = root;
                for(int i = 1 ; i < paths.length ; i++){
                    result += ("\\" + paths[i]);
                }
            }else{
                throw new VirtualPathIllegal(oriPath);
            }
        }catch(VirtualPathIllegal e){
            result = null;
            e.printStackTrace();
        }finally{
            return result;
        }
    }



    /**
     * covert a realPath to a virtualPath;
     * 1、projectFirst , the virtual path based on projects category 2、dataTypeFirst, the virtual path based on data type category
     * @param realPath
     * @return the virtual path
     */
    public static String realPathToVirtualPath_projectFirst(String realPath){
        String result = "";
        String oriPath = realPath;
        try {
            if(isRealPathValid(realPath)) {
                realPath = realPath.replace(root + "\\" , "");
                String[] paths = realPath.split("\\\\");
                if(paths.length < 2) throw new RealPathIllegal(oriPath);
                // paths[0]: category ; paths[1]: project
                result = "\\projects";
                result += "\\" + paths[1];
                result += "\\" + paths[0];
                for(int i = 2 ; i < paths.length ; i++ ){
                    result += ("\\" + paths[i]);
                }
            }else{
                throw new RealPathIllegal(realPath);
            }
        }catch(RealPathIllegal e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public static String realPathToVirtualPath_dataTypeFirst(String realPath){
        String result = "";
        String oriPath = realPath;
        try {
            if(isRealPathValid(realPath)) {
                result = realPath.replace(root, "\\dataType");
            }else{
                throw new RealPathIllegal(oriPath);
            }
        }catch(RealPathIllegal e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    static boolean isRealPathValid(String realPath){
        for(String type : dataTypes.keySet()){
            if(realPath.startsWith(dataTypes.get(type)[0]))
                return true;
        }
        return false;
    }
    //endregion<path conversion>


    /**
     * get the subDirectory of the specific path, which is represented by a virtual path virtualPath
     * @param virtualPath
     * @return the file set of the virtualPath
     */
    public static List<File> getSubDirByVirtualPath(String virtualPath){
        List<File> result = new ArrayList<File>();
        String[] paths = virtualPath.substring(1).split("\\\\");
        try{
            if(paths[0].compareTo("projects") == 0){
                // this is a special case
                if(paths.length == 2){
                    result = findAllDataTypeForAProject(paths[1]);
                }else if(paths.length > 2){
                    String realPath = virtualPathToRealPath(virtualPath);
                    File file = new File(realPath);
                    if(file.isDirectory()){
                        result = new ArrayList<File>(Arrays.asList(file.listFiles()));
                    }else{
                        throw new PathNotDirectory(file.getAbsolutePath());
                    }
                }
            }else if(paths[0].compareTo("dataType") == 0){
                String realPath = virtualPathToRealPath(virtualPath);
                File file = new File(realPath);
                if(file.isDirectory()){
                    result = new ArrayList<File>(Arrays.asList(file.listFiles()));
                }else{
                    throw new PathNotDirectory(file.getAbsolutePath());
                }
            }else{
                throw new VirtualPathIllegal(virtualPath);
            }

        }catch(VirtualPathIllegal e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            return result;
        }

    }

    private static List<File> findAllDataTypeForAProject(String project){
        List<File> result = new ArrayList<File>();
        String filePath ;

        for(String type : dataTypes.keySet()){
            filePath = dataTypes.get(type)[1] + "\\" + project;
            File file = new File(filePath);
            if(file.exists()){
                result.add(file);
            }
        }
        return result;
    }


    private static void initializeDataTypesMap(){
        dataTypes = new HashMap<String, String[]>();
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");

        String[] types = bundle.getString("DataTypes").split("\\|");
        String rootPrefix = "Root_";
        String rootNamePrefix = "RootName_";
        for(String type : types){
            //region <get attributes>
            String root = bundle.getString(rootPrefix + type);
            String rootName = bundle.getString(rootNamePrefix + type);
            //endregion <get attributes>

            //region <put attributes into a string array>
            String[] attributes = new String[2];
            attributes[0] = root;
            attributes[1] = rootName;
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

    public static PathLevel getPathLevel(String path){

        PathLevel result = PathLevel.NULL;
        File file = new File(path);
        Matcher matcher = null;
        if( file.exists() && file.isDirectory()){
            path = path.replace(root , "");
            //region <Bugzilla>
            if(path.startsWith("cn.edu.pku.EOSCN.crawler.BugzillaCrawler")){
                path = path.replace("cn.edu.pku.EOSCN.crawler.BugzillaCrawler" , "");
                Pattern projectPattern = Pattern.compile("[^\\\\]+");
                if(path.length() == 0)
                    result = PathLevel.BUGZILLA_ROOT;
                else if(projectPattern.matcher(path).find())
                    result = PathLevel.BUGZILLA_PROJECT;
            }//endregion <Bugzilla>
            //region <Git>
            else if(path.startsWith("cn.edu.pku.EOSCN.crawler.GitCrawler")){
                path = path.replace("cn.edu.pku.EOSCN.crawler.GitCrawler" , "");
                Pattern projectPattern = Pattern.compile("[^\\\\]+");
                Pattern monthPattern = Pattern.compile("([^\\\\]+\\\\){2}[0-9]{4}-[0-9]{2}");
                if(path.length() == 0)
                    result = PathLevel.GIT_ROOT;
                else if(projectPattern.matcher(path).find())
                    result = PathLevel.GIT_PROJECT;
                else if(monthPattern.matcher(path).find())
                    result = PathLevel.GIT_MONTH;
            }
            //endregion <Git>
            //region <Jira>
            else if(path.startsWith("cn.edu.pku.EOSCN.crawler.JiraIssueCrawler")){

            }
            //endregion<Jira>
            //region <MainSite>
            else if(path.startsWith("cn.edu.pku.EOSCN.crawler.MainSiteCrawler")){

            }
            //endregion<MainSite>
            //region <MailBox>
            else if(path.startsWith("cn.edu.pku.EOSCN.crawler.MboxCrawler")){
                path = path.replace("cn.edu.pku.EOSCN.crawler.MboxCrawler" , "");
                Pattern projectPattern = Pattern.compile("[^\\\\]+]");
                Pattern mailBoxPattern = Pattern.compile("[^\\\\]+[\\\\][^\\\\]");
                if(path.length() == 0)
                    return PathLevel.EMAIL_ROOT;
                else if(projectPattern.matcher(path).find())
                    return PathLevel.EMAIL_PROJECT;
                else if(mailBoxPattern.matcher(path).find())
                    return PathLevel.EMAIL_MAILBOX;
            }
            //endregion <MailBox>
            //region <Stackoverflow>
            else if(path.startsWith("cn.edu.pku.EOSCN.crawler.StackOverflow")){

            }
            //endregion <Stackoverflow>
        }
        return result;
    }

    //region <Exceptions>
    //when convert a virtual path to real path failed , it will throw this exception
    private static class VirtualPathIllegal extends Exception{
        public VirtualPathIllegal(String path) {
            super(path + " can not be mapped to a real path!");
        }
    }

    private static class RealPathIllegal extends Exception{
        public RealPathIllegal(String path){
            super(path + " can not be mapped to a virtual path!");
        }
    }

    private static class PathNotDirectory extends Exception{
        public PathNotDirectory(String path) {
            super(path + " is not a directory, so it is failed to get the subDirectory of this path");
        }
    }
    //endregion<Exceptions>
 }
