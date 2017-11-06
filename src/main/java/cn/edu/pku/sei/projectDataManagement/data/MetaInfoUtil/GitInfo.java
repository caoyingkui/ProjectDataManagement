package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil;

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions.PathLevelInvalid;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2017/10/26.
 */
public class GitInfo extends MetaInfo{

    private static Pattern monthFilePattern;
    private static Matcher matcher;
    private static BufferedReader reader;

    String commitFileName = "commit.txt";
    String reporterFileName = "reporter.txt";

    static{
        monthFilePattern = Pattern.compile("[0-9]{4}-[0-9]{2}");
    }
    private static boolean isMonthDirectory(String fileName){
        matcher = monthFilePattern.matcher(fileName);
        return matcher.find();
    }

    public GitInfo(String path , PathLevel level){
        super(path , level);
    }

    @Override
    public JSONObject toJSONObject() {
        switch(pathLevel){
            case GIT_ROOT:{
                return getMetaInfo_ROOT();
            }
            case GIT_PROJECT:{
                return getMetaInfo_PROJECT();
            }
            case GIT_MONTH:{
                return getMetaInfo_MONTH();
            }
            default:{
                return null;
            }
        }
    }

    public JSONObject getMetaInfo_ROOT(){
        JSONObject result = new JSONObject();
        result.put("pathLevel" , PathLevel.GIT_ROOT.toString());
        //region <get project amount>
        int projectAmount = getProjectAmount();
        result.put("project amount" , projectAmount + "");
        //endregion <get project amount>

        return result;
    }

    public JSONObject getMetaInfo_PROJECT(){
        JSONObject result = new JSONObject();
        result.put("pathLevel" , PathLevel.GIT_PROJECT);

        Set<String> reporters = new HashSet<String>();
        JSONArray monthArray = new JSONArray();

        File[] files = new File(path).listFiles();
        String fileName;
        for(File f : files){
            fileName = f.getName();
            if( isMonthDirectory(fileName) ){
                //region <get the reporter of a month>
                Set<String> temp = getReporterList(f.getAbsolutePath() + "\\" + reporterFileName);
                for(String reporter : temp){
                    reporters.add(reporter);
                }
                //endregion<get the reporter of a month>
                //region <get the commit Amount of a month>
                JSONObject month = new JSONObject();
                month.put("month" , fileName);
                month.put("commitAmount" , getCommitAmount(f.getAbsolutePath() + "\\" + commitFileName));
                monthArray.put(month);
                //endregion<get the commit Amount of a month>
            }
        }

        //region <group all the reporter>
        String reporterList = "";
        for(String reporter : reporters){
            reporterList += (reporter + "|");
        }
        if(reporterList.length() > 0){
            reporterList = reporterList.substring(0 , reporterList.length());//remove the last char '|'
        }
        //endregion<group all the reporter>


        result.put("reporterList" , reporterList);
        result.put("month" , monthArray.toString());

        return result;
    }

    public JSONObject getMetaInfo_MONTH(){
        JSONObject result = new JSONObject();

        //region<get the commit amount>
        int commitAmount = getCommitAmount(path + "\\" + commitFileName);
        //endregion<get the commit amount>
        //region<get the reporter list>
        Set<String> reporters = getReporterList(path + "\\" + reporterFileName);
        String reporterList = "";
        for(String reporter : reporters){
            reporterList += (reporter + "|");
        }
        if(reporterList.length() > 0){
            reporterList = reporterList.substring(0 , reporterList.length());
        }
        //endregion<get the reporter list>

        result.put("pathLevel" , PathLevel.GIT_MONTH.toString());
        result.put("commitAmount" , commitAmount + "");
        result.put("reporterList" , reporterList);
        return result;

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

    private List<String> getReporterList(){
        List<String> reporterList = new ArrayList<String>();
        try{
            File file = new File(path + "\\" + reporterFileName);
            if(file.exists() && file.isFile()){
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                line = reader.readLine(); // the first line is the number of the reporter
                while((line = reader.readLine()) != null){
                    reporterList.add(line);
                }
            }else{
                throw new PathLevelInvalid(path , pathLevel);
            }
        }catch(PathLevelInvalid e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return reporterList;
    }

    private Map<String, Integer> getCommitAmountPerMonth(){
        Map<String ,Integer> result = new HashMap<String, Integer>();
        File file = new File(path);
        try{
            File[] files = file.listFiles();
            Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}");
            Matcher matcher = null;
            String fileName;
            BufferedReader reader;
            int count;
            for(File f : files){
                fileName = f.getName();
                matcher = pattern.matcher(fileName);
                if(matcher.find()){
                    File monthCommitFile = new File(fileName + "\\" + commitFileName);
                    reader = new BufferedReader(new FileReader(monthCommitFile));
                    count = Integer.parseInt(reader.readLine().trim());
                    result.put(fileName , count);
                    reader.close();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    private static int getCommitAmount(String path){
        int result = 0;
        try{
            File file = new File(path);
            reader = new BufferedReader(new FileReader(file));

            // the first line of this file will the number of the commit
            String line = reader.readLine();
            line = line.trim();
            result = Integer.parseInt(line);

            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private static Set<String> getReporterList(String path){
        Set<String> result = new HashSet<String>();
        try{
            File file = new File(path);
            reader = new BufferedReader(new FileReader(file));
            String line ;

            //the first line of the file will be the number of the reporter
            reader.readLine();
            while((line = reader.readLine()) == null){
                line = line.trim();
                result.add(line);
            }
            reader.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
