package cn.edu.pku.sei.projectDataManagement.data;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.*;
import cn.edu.pku.sei.projectDataManagement.util.Directory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by oliver on 2017/10/15.
 */


public class PathInfo {
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;
    private static final long TB = 1024 * GB;

    private String dir = "";
    private PathType type = null;
    private String fileName = "";
    private String dataSize = "";
    MetaInfo metaInfo = null;
    boolean status = false;

    //region<getter>
    public String getDir(){
        return dir;
    }

    public void setDir(String value){
        this.dir = value;
    }

    public PathType getType(){
        return type;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String value){
        this.fileName = value;
    }

    public String getDataSize(){
        return dataSize;
    }

    public MetaInfo getMetaInfo(){
        return metaInfo;
    }

    public boolean getStatus(){
        return status;
    }
    //endregion



    public static void main(String[] args){
        File file = new File("E:\\CrawlData\\Apache");
        show(file);
    }

    public static void show(File file){
        PathInfo Info = new PathInfo(file , "dataType");
        System.out.println(Info.toJSONObject().toString());

        File[] files = file.listFiles();
        for(File f : files){
            if(f.isDirectory()){
                show(f);
            }else if(f.isFile()){
                Info = new PathInfo(f , "dataType");
                //System.out.println(Info.toJSONObject().toString());
            }
        }
    }

    public PathInfo(File file , String type){
        status = true;
        if(file == null) {
            status = false;
            return;
        }
        try{
            //region <set the directory of this path>
            String path = file.getAbsolutePath();
            if(type.compareTo("projects") == 0){
                this.dir = Directory.realPathToVirtualPath_projectFirst(path);
                if(this.dir == null)
                    status = false;
            }else if(type.compareTo("dataType") == 0){
                this.dir = Directory.realPathToVirtualPath_dataTypeFirst(path);
                if(this.dir == null)
                    status = false;
            }else{
                this.dir = null; // this case should not occur.
                status = false;
            }
            //endregion
            //region<set the type of this path , if the path is file then also set the data size >
            if(file.isDirectory()){
                this.type = PathType.DIRECTORY;
            }else if(file.isFile()){
                this.type = PathType.FILE;
                this.dataSize = dataSizeConvertToString(file.length());
            }else{
                status = false;
                return ;
            }
            //endregion
            //region <set the file name of this path>
            this.fileName = this.dir.substring(this.dir.lastIndexOf("\\") + 1);

            //endregion
            //region <set the metaInfo of this path>
            PathLevel pathLevel = Directory.getPathLevel(path);
            switch (pathLevel){
                case BUGZILLA_PROJECT:
                case BUGZILLA_ROOT:{
                    this.metaInfo = new BugzillaInfo(path , pathLevel);
                    break;
                }
                case GIT_ROOT:
                case GIT_PROJECT:
                case GIT_MONTH:{
                    this.metaInfo = new GitInfo(path , pathLevel);
                    break;
                }
                case EMAIL_ROOT:
                case EMAIL_PROJECT:
                case EMAIL_MAILBOX:{
                    this.metaInfo = new EmailInfo(path , pathLevel);
                    break;
                }
                case NULL:{
                    this.metaInfo = new MetaInfo(path , pathLevel);
                }
            }
            //endregion <set the metaInfo of this path>
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 一个项目的根目录比较特殊，所以特别声明一个构造函数
     * @param projectName
     */
    public PathInfo(String projectName){
        status = true;
        this.dir = "\\projects\\" + projectName;
        this.type = PathType.DIRECTORY;
        this.fileName = projectName;
        this.metaInfo = new MetaInfo(projectName, Directory.getPathLevel(""));
    }

    public PathInfo(String projectName, String dataType){
        status = true;
        this.dir = "\\projects\\" + projectName + "\\" + dataType;
        this.type = PathType.DIRECTORY;
        this.fileName = dataType;
        this.metaInfo = new MetaInfo(projectName, Directory.getPathLevel(""));
    }

    private String dataSizeConvertToString(long size) {
        double result;
        String resultString = "";
        DecimalFormat format = new DecimalFormat(".0");

        try {
            if (size >= TB) throw new FileSizeTooLarge();
            else if (size >= GB) {
                result = (double) size / GB;
                resultString = format.format(result) + "GB";
            } else if (size >= MB) {
                result = (double) size / MB;
                resultString = format.format(result) + "MB";
            } else if (size >= KB) {
                result = (double) size / KB;
                resultString = format.format(result) + "KB";
            } else if (size >= 0) {
                resultString = "1KB";
            } else {
                throw new FileSizeIllegal();
            }
        }catch(FileSizeTooLarge e){
            e.printStackTrace();
        }catch(FileSizeIllegal e){
            e.printStackTrace();
        }

        return resultString;
    }

    public JSONObject toJSONObject(){
        JSONObject result = new JSONObject();
        if(status){
            result.put("dir" , this.dir);
            result.put("type" , this.type.toString().toLowerCase());
            result.put("fileName" , this.fileName);
            result.put("dataSize" , this.dataSize );
            result.put("metaInfo" , this.metaInfo.toJSONObject());
        }else{
            result = null;
        }
        return result;
    }

    public static JSONObject toJSONObject(PathInfo[] pathInfos , String dataType , String absolutePath){
        JSONObject result = new JSONObject();
        try{
            if(pathInfos == null || pathInfos.length == 0)
                throw new JSONObjectConvertFailed("The parameter 'pathInfos' is null (or size is 0)");
            else if(dataType == null || dataType.trim().length() == 0)
                throw new JSONObjectConvertFailed("There is no dataType");
            else{
                result.put("dataType" , dataType);
                if(absolutePath != null)
                    result.put("absolutePath" , absolutePath);
                JSONArray array = new JSONArray();
                JSONObject temp ;
                for(PathInfo pathInfo : pathInfos){
                    temp = pathInfo.toJSONObject();

                    if(temp != null) {
                        array.put(temp);
                    }
                }
                result.put("data" , array);
            }
        }catch (JSONObjectConvertFailed e){
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    private class FileSizeTooLarge extends Exception{

        public FileSizeTooLarge() {
            super("File: " + dir + " data size is too large");
        }
    }

    private class FileSizeIllegal extends Exception{
        public FileSizeIllegal(){
            super("File: " + dir + " data size is less than 0");
        }
    }

    private static class JSONObjectConvertFailed extends Exception{
        public JSONObjectConvertFailed(String errorInfo) {
            super(errorInfo);
        }
    }

}






















