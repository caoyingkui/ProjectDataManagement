package cn.edu.pku.sei.projectDataManagement.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<File> files = new ArrayList<File>();
        files.add(new File("D:\\BaiduNetdiskDownload\\1.rar"));
        files.add(new File("D:\\BaiduNetdiskDownload\\2.rar"));
        zipFile(resp , files , "test");
    }

    private void zipFile(HttpServletResponse resp, List<File> files, String zipName){
        resp.setContentType("APPLICATION/OCTET-STREAM");
        resp.setHeader("Content-Disposition" , "attachment;filename=test.zip");
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
        if(subs.exists()){
            if(subs.isFile()){
                zos.putNextEntry(new ZipEntry(baseName + subs.getName()));
                FileInputStream fis = new FileInputStream(subs);
                byte[] buffer = new byte[1024];
                int r = 0;
                while ((r = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, r);
                }
                fis.close();
            }else{
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
}
