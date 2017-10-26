package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions.PathLevelInvalid;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by oliver on 2017/10/26.
 */
public class GitInfo extends MetaInfo{

    String commitFileName = "commit.txt";
    String reporterFileName = "reporter.txt";

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

    private int getCommitAmount(){
        int commitAmount = 0;
        try{
            // in the path, there will be a file(commit.txt) , the first line of this file is the amount of the commit.
            String commitInfoPath = path + "\\" + commitFileName;
            File file = new File(commitInfoPath);
            boolean succeed = false;
            if(file.exists() && file.isFile()){
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine().trim();
                if(line != null){
                    succeed = true;
                    commitAmount = Integer.parseInt(line);
                }
                reader.close();
            }
            if(!succeed){
                throw new PathLevelInvalid(path , pathLevel);
            }
        }catch(PathLevelInvalid e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        return commitAmount;
    }

    private String getReporterList(){
        return "";
    }
}
