/**
 * Created by oliver on 2017/8/10.
 */



function addAnnotation(userID , pageIndex , dataIndex){

    var annotation = $("#addAnnotation").val();

    var parameters ={
        requestType:"addAnnotation",
        userID: userID,
        pageIndex: pageIndex,
        dataIndex: dataIndex,
        annotation:annotation
    };

    $.ajax({
        type:"Post",
        url:"LabelTask",
        data:parameters,
        async: false,
        success:function (data) {
            window.location.href="labelTask.jsp?pageIndex=" + pageIndex + "&" + "dataIndex=" + dataIndex;
        }
    });

}

function deleteAnnotation(userID , pageIndex , dataIndex , annotation){
    var parameters = {
        requestType:"deleteAnnotation",
        userID:userID ,
        pageIndex: pageIndex,
        dataIndex: dataIndex,
        annotation:annotation
    };

    $.ajax({
        type:"Post" ,
        url:"LabelTask",
        data:parameters,
        async:false,
        success:function (data) {
            window.location.href="labelTask.jsp?pageIndex=" + pageIndex + "&" + "dataIndex=" + dataIndex;
        }
    });
}