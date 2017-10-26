package cn.edu.pku.sei.projectDataManagement.servlets;

import cn.edu.pku.sei.projectDataManagement.util.Directory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by oliver on 2017/10/18.
 */
public class DownloadServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req , resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<File> files = new ArrayList<File>();
        files.add(new File("D:\\BaiduNetdiskDownload\\1.rar"));
        files.add(new File("D:\\BaiduNetdiskDownload\\2.rar"));
        /*zipFile(resp , files , "test");*/
        String requestType = request.getParameter("requestType");
        if(true || requestType.compareTo("downloadFiles") == 0){
            //String[] filePaths = request.getParameter("filePaths").split("|");
            ////List<File> files = getFiles(filePaths);
            if(files == null){
                //System.out.println("There is no files will be downloaded!(the filePaths=" + filePaths + ")");
            }else if(files.size() == 1 && files.get(0).isFile()){
                downloadSingleFile(response , files.get(0));
            }else{
                downloadMultiFiles(response, files);
            }
        }
    }

    private void downloadSingleFile(HttpServletResponse response , File file){
        response.setContentType("APPLICATION/OCTET-STREAM");
        response.setHeader("Content-Disposition" , "attachment;filename=" + file.getName());
        try {
            ServletOutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            while(in.read(buffer) != -1){
                out.write(buffer);
            }
            in.close();
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void downloadMultiFiles(HttpServletResponse response , List<File> files){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String zipName = format.format(new Date()) + ".zip";
        zipFile(response , files , zipName);
    }

    private void zipFile(HttpServletResponse resp, List<File> files, String zipName){
        resp.setContentType("APPLICATION/OCTET-STREAM");
        resp.setHeader("Content-Disposition" , "attachment;filename="+ zipName);
        try {
            ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream());

            System.out.println("Downloading...");
            for (File f : files) {
                zipFile(f, "", zos);
            }
            zos.flush();
            zos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void zipFile(File subs, String baseName, ZipOutputStream zos)throws IOException {
        if(subs.exists()) {
            if (subs.isFile()) {
                zos.putNextEntry(new ZipEntry(baseName + subs.getName()));
                FileInputStream fis = new FileInputStream(subs);
                byte[] buffer = new byte[1024];
                int r = 0;
                while ((r = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, r);
                }
                fis.close();
            } else {
                //如果是目录。递归查找里面的文件
                String dirName = baseName + subs.getName() + "/";
                zos.putNextEntry(new ZipEntry(dirName));
                File[] sub = subs.listFiles();
                for (File f : sub) {
                    zipFile(f, dirName, zos);
                }
            }
        }
    }

    private List<File> getFiles(String[] filePaths){
        List<File> result = new ArrayList<File>();
        String virtualPath = "";
        String realPath = "";
        File file = null;
        for(int i = 0 ; i < filePaths.length ; i ++){
            virtualPath = filePaths[i].trim();
            if(virtualPath.length() == 0) continue;
            realPath = Directory.virtualPathToRealPath(virtualPath);
            file = new File(realPath);
            try {
                if (!file.exists()) throw new FileNotExist(realPath);
                result.add(file);
            }catch(FileNotExist e){
                e.printStackTrace();
                continue;
            }
        }
        return result;
    }

    //region <Exception>
    private static class FileNotExist extends Exception{
        public FileNotExist(String filePath) {
            super(filePath + " does not exist");
        }
    }

    //endregion <Exception>
}
