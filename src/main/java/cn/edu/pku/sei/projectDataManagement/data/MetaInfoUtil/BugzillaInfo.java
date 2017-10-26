package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions.PathLevelInvalid;
import javafx.util.Pair;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2017/10/25.
 */
public class BugzillaInfo extends MetaInfo{

    public static void main(String[] args){
        String name = "d1234.xml";
        String reg = "[0-9]+\\.xml";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(name);
        matcher.find();
        boolean b = matcher.matches();
        boolean bd = matcher.matches();
    }

    public BugzillaInfo(String path , PathLevel level){
        super(path , level);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject result = new JSONObject();
        switch (pathLevel){
            case BUGZILLA_ROOT:{
                result = getMetaInfo_ROOT();
                break;
            }case BUGZILLA_PROJECT:{
                result = getMetaInfo_PROJECT();
                break;
            }
        }
        return result;
    }

    /**
     * when the path Level is BUGZILLA_ROOT, it means this path is the root of  the bugzilla directory.
     * @return
     */
    public JSONObject getMetaInfo_ROOT(){
        JSONObject result = new JSONObject();

        //region <get project amount>
        int projectAmount = getProjectAmount();
        result.put("project amount" , projectAmount + "");
        //endregion <get project number>

        return result;
    }

    public JSONObject getMetaInfo_PROJECT(){
        JSONObject result = new JSONObject();

        File file = new File(path);
        // region<get bug amount>
        int bugAmount = getBugAmount(file);
        result.put("bug amount" , bugAmount + "");
        // endregion<get bug amount>

        return result;
    }

    private int getBugAmount(File file){
        int bugAmount = 0;
        try{
            if(file.isDirectory()){
                Pattern pattern = Pattern.compile("[0-9]+\\.xml");
                Matcher matcher = null;
                String fileName;
                File[] fileList = file.listFiles();
                for(File f : fileList){
                    if(f.isFile()){
                        fileName = f.getName();
                        matcher = pattern.matcher(fileName);
                        if(matcher.find()) bugAmount ++;
                    }
                }
            }else{
                throw new PathLevelInvalid(path , pathLevel);
            }
        }catch (PathLevelInvalid e){
            e.printStackTrace();
            bugAmount = 0;
        }catch (Exception e){
            e.printStackTrace();
            bugAmount = 0;
        }
        return bugAmount;
    }

}
