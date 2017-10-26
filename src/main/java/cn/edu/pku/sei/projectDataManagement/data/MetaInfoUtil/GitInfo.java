package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions.PathLevelInvalid;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by oliver on 2017/10/26.
 */
public class GitInfo extends MetaInfo{

    public GitInfo(String path , PathLevel level){
        super(path , level);
    }

    public JSONObject getMeatInfo(){
        JSONObject result = new JSONObject();

        return result;
    }

    public JSONObject getMetaInfo_ROOT(){
        JSONObject result = new JSONObject();

        //region <get project amount>
        int projectAmount = getProjectAmount();
        result.put("project amount" , projectAmount + "");
        //endregion <get project amount>

        return result;
    }

    public JSONObject getMetaInfo_PROJECT(){
        return null;
    }
}
