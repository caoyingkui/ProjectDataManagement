/**
 * Created by oliver on 2017/7/28.
 */

function append(){
    var li = $("sidebar-menu").append("li")
        .attr("class" , "treeview")
        .html("test");


    li.append("a")
        .attr("href" , "https://adminlte.io/themes/AdminLTE/pages/layout/boxed.html#");

}
