/**
 * Created by oliver on 2017/8/1.
 */

function getNewPath(path){
    var parameters = {
        requestType:"browseDirectory",
        directory:path
    }

    $.ajax({
        type:"Post",
        url:"Download",
        data:parameters,
        async: false,
        //success:function(data){
            //newPath(data);
            //pathNavi(data);
        //}

    });
}

function handle(){
    var test = $("#test").html();
    var template = Handlebars.compile(test);
    var context = {
        url:"https://v.qq.com",
        content:"tencent"
    }
    var temp = template(context);
    $("#test-div").html(temp);
}

function newPath(pathInfo){
    var pathTemplate = $("#pathTemplate").html();
    var template = Handlebars.compile(pathTemplate);
    var string = "";
    var datas = pathInfo.data;
    for(var i = 0 ; i < datas.length ; i++){
        var context = {
            newPath:datas[i].dir.replace(new RegExp(/\\/g) , "\\\\"),
            pathName:datas[i].dir,
            pathType:datas[i].type
        }
        string += template(context);
    }
    $("#taskBroswer").html(string);
}

function pathNavi(pathInfo){
    var pathNaviTemplate = $("#pathNaviTemplate").html();
    var template = Handlebars.compile(pathNaviTemplate);
    var absolutePath = pathInfo.absolutePath;
    var paths = absolutePath.split(new RegExp(/\\/g));
    var path = "";
    var string = "";
    for(var i = 1 ; i < paths.length ; i++){
        path += ("\\\\" + paths[i]);
        var context = {
            absolutePath:path,
            pathName:"\\" + paths[i]
        }
        string += template(context);
    }

    $("#pathNavigator").html(string);
}