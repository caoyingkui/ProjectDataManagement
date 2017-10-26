package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions.PathLevelInvalid;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by oliver on 2017/10/14.
 */
public class MetaInfo {

    String path ;
    PathLevel pathLevel;

    public MetaInfo(String path , PathLevel level){
        this.path = path;
        this.pathLevel = level;
    }

    public JSONObject toJSONObject(){
        return null;
    }

    protected int getProjectAmount(){
        int projectAmount = 0;
        File file = new File(path);
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for(File f : files){
                    if(file.isDirectory()) projectAmount ++;
                }
            }else {
                throw new PathLevelInvalid(path, pathLevel);
            }
        }catch (PathLevelInvalid e){
            e.printStackTrace();
            projectAmount = 0;
        }catch (Exception e ){
            e.printStackTrace();
            projectAmount = 0;
        }

        return projectAmount;
    }


}
