package cn.edu.pku.sei.projectDataManagement.util;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.PathLevel;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.pku.sei.projectDataManagement.data.PathInfo;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



/**
 * Created by oliver on 2017/10/15.
 */
public class Directory {
    private static String root = "";
    private static Map<String , String[]> dataTypes;
    public static Map<String , Map<String, String>> dataTypePaths;
    private static Set<String> projects;

    public static Map<String, String> soPaths = new HashMap<String, String>();
    public static Map<String, String> gitPaths = new HashMap<String, String>();
    public static Map<String, String> emailPaths = new HashMap<String, String>();
    public static Map<String, String> codePaths = new HashMap<String, String>();
    public static Map<String, String> bugPaths = new HashMap<String, String>();

    private static Map<String , String> realPathToVirtualPath = new HashMap<String, String>();

    static{


        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        initializeDataTypesMap();
        initializeProjectsSet();
        initializeDataTypesPathsMap();
        root = bundle.getString("DataRoot");

        for(String dataType : dataTypes.keySet()){
            Map<String , String> dataTypePath = dataTypePaths.get(dataType);
            String baseDir = dataTypes.get(dataType)[0];
            for(String project : dataTypePath.keySet()){
                File file = new File(baseDir + "\\" + dataTypePath.get(project));
                if(!file.exists()){
                    System.out.print("-");
                    System.out.println(baseDir + "\\" + dataTypePath.get(project));
                }else
                    System.out.println("+");

            }
        }

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
        Directory dir = new Directory();

        /*File file = new File("D:\\projectddddDataManagement");
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
        }*/
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
                if(paths.length <= 2){ // "\\projects" or "\\projects\\project"
                    result = "\\" + virtualPath ;
                }else if(paths.length > 2){
                    String project = paths[1];
                    String dataType = paths[2];
                    result = dataTypes.get(dataType)[0] + "\\" + dataTypePaths.get(dataType).get(project);
                    for(int i = 3 ; i < paths.length ; i ++){
                        result += ("\\" + paths[i]);
                    }
                }

                //region <old>
                /*
                // this is a special case which return value is not a real path
                if(paths.length == 2){
                    result = virtualPath;
                }else if(paths.length > 2){
                    // path[1]: projectName; path[2]: category
                    result = root ;
                    result += ("\\" + dataTypes.get(paths[2])[1]); // 可能会导致出错。
                    result += ("\\" + paths[1]);
                    for(int i = 3 ; i < paths.length ; i ++){
                        result += ("\\" + paths[i]);
                    }
                }else{
                    throw new VirtualPathIllegal(oriPath);
                }
                */
                //endregion <old>
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
        for(String type : dataTypes.keySet()){
            String dir = dataTypes.get(type)[0];
            if(realPath.startsWith(dir)) {
                realPath = realPath.substring((dir + "\\").length());
                String[] paths = realPath.split("\\\\");
                if(paths.length == 0){
                    new Exception().printStackTrace();
                }else{
                    String temp = paths[0];
                    Map<String , String> dataTypePath = dataTypePaths.get(type);
                    for(String project : dataTypePath.keySet()){
                        if(dataTypePath.get(project).compareTo(temp) == 0){
                            result = "\\projects\\" + project + "\\" + type;
                            for(int i = 1 ; i < paths.length ; i ++)
                                result += ("\\" + paths[i]);
                            break;
                        }
                    }

                }
                break;
            }
        }
        return result;
        // region <old>
        /*try {
            if(isRealPathValid(realPath)) {
                //region <old>
                String[] paths = realPath.split("\\\\");
                if(paths.length < 2) throw new RealPathIllegal(oriPath);

                String dataType = "";


                for(String type : dataTypes.keySet()){
                    String root = dataTypes.get(type)[0];
                    if(realPath.startsWith(root)){
                        dataType = type;
                        break;
                    }
                }

                if(dataType.length() > 0){
                    String root = dataTypes.get(dataType)[0];
                    paths = realPath.replace(root + "\\" , "").split("\\\\");
                    result = "\\projects";
                    result += ( "\\" + paths[0] );
                    result += ("\\" + dataType);
                    for(int i = 1; i < paths.length ; i++) {
                        result += ("\\" + paths[i]);
                    }
                }else{
                    result = "";
                }
                // endregion <old>


            }else{
                throw new RealPathIllegal(realPath);
            }
        }catch(RealPathIllegal e){
            e.printStackTrace();
            result = null;
        }
        return result;*/
        //endregion <old>
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
            //e.printStackTrace();
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


                if(paths.length == 1){// this is a special case
                    result = findAllProjects();
                }
                else if(paths.length == 2){// this is a special case
                    result = findAllDataTypeForAProject(paths[1]);
                }else if(paths.length > 2){
                    String realPath = virtualPathToRealPath(virtualPath);

                    File file = new File("\\\\162.105.88.12\\F:\\Tomcat 8.0\\logs");//realPath);
                    if(file.isDirectory()){
                        result = new ArrayList<File>(Arrays.asList(file.listFiles()));
                    }else{
                        System.out.println(file.getAbsolutePath() + file.exists());
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
        }catch(Throwable e){
            e.printStackTrace();
        }
        finally{
            return result;
        }

    }

    public static List<File> findAllProjects(){
        List<File> result = new ArrayList<File>();
        if(dataTypes == null)
            return null ;
        for(String type : dataTypes.keySet()){
            String root = dataTypes.get(type)[0]; // value[0] stores the root of the type
            File rootFile = new File(root);
            if(rootFile.exists() && rootFile.isDirectory()){
                File[] fileList = rootFile.listFiles();

                for(File subFile : fileList){
                    if(subFile.isDirectory()){
                        result.add(subFile);
                    }
                }

            }
        }
        return result;
    }

    private static List<File> findAllDataTypeForAProject(String project){
        List<File> result = new ArrayList<File>();
        String filePath ;

        for(String type : dataTypes.keySet()){
            filePath = dataTypes.get(type)[0] + "\\" + project;
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
        if(dataTypes == null)
            return ;
        projects = new HashSet<String>();
        try{
            FileInputStream fis = new FileInputStream(new File("E:\\Intellij workspace\\ProjectDataManagement\\projectPaths.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getLastRowNum();
            for(int i = 1 ; i <= rows; i ++){
                String project = sheet.getRow(i).getCell(0).toString();
                projects.add(project);
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        //region <old>
        /*
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
        */
        //endregion <old>
    }

    private static void initializeDataTypesPathsMap(){
        try{
            Map<Integer , String> type_column = new HashMap<>();
            FileInputStream fis = new FileInputStream("E:\\Intellij workspace\\ProjectDataManagement\\projectPaths.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFRow firstRow = sheet.getRow(0);
            int columnCount = firstRow.getPhysicalNumberOfCells();
            for(int i = 1 ; i < columnCount ; i ++){
                type_column.put(i , firstRow.getCell(i).toString());
            }

            dataTypePaths = new HashMap<>();
            for(String type : dataTypes.keySet()){
                dataTypePaths.put(type, new HashMap<String , String>());
            }

            int rows = sheet.getLastRowNum();
            for(int i = 1 ; i <= rows ; i ++){
                XSSFRow row = sheet.getRow(i);
                String project = row.getCell(0).toString();
                for(int j = 1 ; j < columnCount; j ++){
                    XSSFCell cell = row.getCell(j);
                    if(cell != null) {
                        dataTypePaths.get(type_column.get(j))
                                .put(project , cell.toString());
                    }
                }
            }

        }catch(Exception e ){
            e.printStackTrace();
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
