package cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.Exceptions;

/**
 * Created by oliver on 2017/10/25.
 */

import cn.edu.pku.sei.projectDataManagement.data.MetaInfoUtil.PathLevel;

/**
 *
 */
public class PathLevelInvalid extends Exception{
    public PathLevelInvalid(String path , PathLevel level) {
        super("The path does not match the pathLevel! path=" + path + "  " + "level=" + level.toString());
    }
}
