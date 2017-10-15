package cn.edu.pku.sei.projectDataManagement.util;

import java.io.File;
import java.util.*;

/**
 * Created by oliver on 2017/10/15.
 */
public class Directory {
    private static String root = "";
    private static String bugRoot = "";
    private static String commitRoot = "";
    private static String emailRoot = "";
    private static String stackoverflowRoot = "";

    private static String Bug;
    private static String Commit;
    private static String Email;
    private static String Stackoverflow;
    static{
        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        root = bundle.getString("DataRoot");
        bugRoot = bundle.getString("BugRoot");
        commitRoot = bundle.getString("CommitRoot");
        emailRoot = bundle.getString("EmailRoot");
        stackoverflowRoot = bundle.getString("StackoverflowRoot");

        Bug = bundle.getString("Bug");
        Commit = bundle.getString("Commit");
        Email = bundle.getString("Email");
        Stackoverflow = bundle.getString("Stackoverflow");
    }

    public static void main(String[] args){
        System.out.println("asdfasdf");
        int c = 1;
        String path ;
        Scanner sc = new Scanner(System.in);
        while(c == 1 || c == 2){
            c = (char)sc.nextInt();
            path = sc.nextLine().trim();
            if(c == 1){
                System.out.println(virtualPathToRealPath(path));
            }else if(c == 2){
                System.out.println(realPathToVirtualPath(path , "projects"));
                System.out.println(" ");
                System.out.println(realPathToVirtualPath(path , "dataType"));
            }
        }



    }
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
    static String virtualPathToRealPath(String virtualPath){
        String result = "";
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
                    for(int i = 2 ; i < paths.length ; i ++){
                        result += ("\\" + paths[i]);
                    }
                }else{
                    throw new VirtualPathIllegal(virtualPath);
                }
            }else if(paths[0].compareTo("dataType") == 0){
                result = root;
                for(int i = 1 ; i < paths.length ; i++){
                    result += ("\\" + paths[i]);
                }
            }else{
                throw new VirtualPathIllegal(virtualPath);
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
     * @param realPath
     * @param type takes 2 values :1、projects , the virtual path based on projects category 2、dataType, the virtual path based on data type category
     * @return the virtual path
     */
    static String realPathToVirtualPath(String realPath , String type){
        String result = "";
        String oriPath = realPath;
        try {
            if(isRealPathValid(realPath)) {
                if (type.compareTo("projects") == 0) {
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
                } else if (type.compareTo("dataType") == 0) {
                    result = realPath.replace(root, "\\dataType");
                } else {
                    result = null;
                }
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
        boolean result = false;
        if(realPath.startsWith(bugRoot) ||
                realPath.startsWith(commitRoot) ||
                realPath.startsWith(emailRoot) ||
                realPath.startsWith(stackoverflowRoot)){
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    /**
     * get the subDirectory of the specific path, which is represented by a virtual path virtualPath
     * @param virtualPath
     * @return the file set of the virtualPath
     */
    static List<File> getSubDirByVirtualPath(String virtualPath){
        List<File> result = new ArrayList<File>();
        String[] paths = virtualPath.split("\\\\");
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
        File file;
        //region<find bug path>
        filePath = bugRoot + "\\" + project;
        file = new File(filePath);
        if(file.exists()){
            result.add(file);
        }
        //endregion
        //region<find Commit path>
        filePath = commitRoot + "\\" + project;
        file = new File(filePath);
        if(file.exists()){
            result.add(file);
        }
        //endregion
        //region<find Email path>
        filePath = emailRoot + "\\" + project;
        file = new File(filePath);
        if(file.exists()){
            result.add(file);
        }
        //endregion
        //region<find Stackoverflow path>
        filePath = stackoverflowRoot + "\\" + project;
        file = new File(filePath);
        if(file.exists()){
            result.add(file);
        }
        //endregion
        return result;
    }

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
 }
