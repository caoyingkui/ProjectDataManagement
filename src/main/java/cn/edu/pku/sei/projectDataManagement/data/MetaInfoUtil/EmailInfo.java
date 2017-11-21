package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2017/10/27.
 */
public class EmailInfo extends MetaInfo{
    public EmailInfo(String path, PathLevel level) {
        super(path, level);
    }

    @Override
    protected int getProjectAmount() {
        File file = new File(path);
        return file.listFiles().length;
    }

    @Override
    public JSONObject toJSONObject() {
        switch (this.pathLevel){
            case EMAIL_ROOT: return getMetaInfo_ROOT();
            case EMAIL_PROJECT: return getMetaInfo_PROJECT();
            case EMAIL_MAILBOX: return getMetaInfo_MailBox();
        }
        return null;
    }

    private  JSONObject getMetaInfo_ROOT(){
        JSONObject result = new JSONObject();
        result.put("pathLevel" , PathLevel.EMAIL_ROOT.toString());

        //region <get the project number of this dir>
        int projectAmount = getProjectAmount();
        result.put("projectAmount" , projectAmount + "");
        //endregion <get the project number of this dir>

        return result;
    }

    private JSONObject getMetaInfo_PROJECT(){
        JSONObject result = new JSONObject();
        result.put("pathLevel" , PathLevel.EMAIL_PROJECT.toString());

        JSONArray mailBox = new JSONArray();

        Map<String , Integer> mailBoxList = getMailBoxList();
        for(String key : mailBoxList.keySet()){
            JSONObject object = new JSONObject();
            object.put("mailBox" , key);
            object.put("mailAmount" , mailBoxList.get(key));
            mailBox.put(object);
        }

        result.put("mailBoxAmount" , mailBox.length());
        result.put("mailBoxList" , mailBox.toString());

        return result;
    }

    private JSONObject getMetaInfo_MailBox(){
        JSONObject result = new JSONObject();
        result.put("pathLevel" , PathLevel.EMAIL_MAILBOX.toString());
        result.put("mailAmout" , getMailAmount(path));

        return result;
    }

    private Map<String , Integer> getMailBoxList(){
        Map<String , Integer> result = new HashMap<String, Integer>();
        File[] files = new File(path).listFiles();
        for(File f : files){
            if(f.isDirectory()) {
                try {
                    result.put(f.getName(), getMailAmount(f.getAbsolutePath()));
                }catch(Exception e){
                    ;
                }
            }
        }
        return result;
    }

    private int getMailAmount(String paTh){
        return new File(paTh).listFiles().length - 2;
    }

}
