package cn.edu.pku.sei.projectDataManagement.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by oliver on 2017/10/15.
 */


public class PathInfo {
    private final long KB = 1024;
    private final long MB = 1024 * KB;
    private final long GB = 1024 * MB;
    private final long TB = 1024 * GB;

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

    public PathType getType(){
        return type;
    }

    public String getFileName(){
        return fileName;
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
        File file = new File("D:\\apache\\abdera\\1.1.3");
        PathInfo info = new PathInfo(file);

        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("data" , "1"));
        array.put(new JSONObject().put("data" , "2"));
        object.put("datas" , array);
        int i= 0;
        i ++;
    }

    public PathInfo(File file){
        if(file == null) {
            status = false;
            return;
        }
        int i = 0;
        i ++;
        try{
            //region <set the directory of this path>
            this.dir = file.getAbsolutePath();
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
            this.fileName = file.getName();

            //endregion

            //file.l
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String dataSizeConvertToString(long size) {
        double result;
        String resultString = "";
        DecimalFormat format = new DecimalFormat(".0");

        try {
            if (size >= TB) throw new FileSizeTooLarge();
            else if (size >= GB) {
                result = (double) size / GB;
                resultString = format.format(result);
            } else if (size >= MB) {
                result = (double) size / MB;
                resultString = format.format(result);
            } else if (size >= KB) {
                result = (double) size / KB;
                resultString = format.format(result);
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
            result.put("type" , this.type.toString());
            result.put("fileName" , this.fileName);
            result.put("dataSize" , this.dataSize );
            result.put("metaInfo" , this.metaInfo.toJSONObject());
        }else{
            result = null;
        }
        return result;
    }

    public static JSONObject toJSONObject(PathInfo[] pathInfos , String dataType){
        JSONObject result = new JSONObject();
        try{
            if(pathInfos == null || pathInfos.length == 0)
                throw new JSONObjectConvertFailed("The parameter 'pathInfos' is null (or size is 0)");
            else if(dataType == null || dataType.trim().length() == 0)
                throw new JSONObjectConvertFailed("There is no dataType");
            else{
                result.put("dataType" , dataType);
                JSONArray array = new JSONArray();
                JSONObject temp = new JSONObject() ;
                for(PathInfo pathInfo : pathInfos){
                    temp = pathInfo.toJSONObject();
                    if(temp != null)
                        array.put(pathInfo.toJSONObject());
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






















