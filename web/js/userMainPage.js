/**
 * Created by oliver on 2017/7/29.
 */
function getTablesInfo(){
    var tablesInfo;
    var parameters ={
        requestType:"getTablesInfo",
        database_url:$("#database_url").val(),
        database_port:$("#database_port").val(),
        database_database:$("#database_database").val(),
        database_user:$("#database_user").val(),
        database_pwd:$("#database_pwd").val()
    };

    $.ajax({
        type: 'Post',
        url:'DatabaseBrowser',
        data:parameters,
        async:false,
        success:function(data){
            tablesInfo = data;
        }
    });
    appendTableSelectorToTimeline(tablesInfo);
}

function getColumnsInfo(){
    console.log("getColumnsInfo");
    var columnsInfo;
    var tableName = $("#tableOptions").val();
    var parameters={
        requestType:"getColumnsInfo",
        database_url:$("#database_url").val(),
        database_port:$("#database_port").val(),
        database_database:$("#database_database").val(),
        database_user:$("#database_user").val(),
        database_pwd:$("#database_pwd").val(),
        database_tableName:tableName
    };
    $.ajax({
        type: 'Post',
        url: 'DatabaseBrowser',
        data: parameters,
        async: false,
        success: function (data) {
            columnsInfo = data;
        }
    });

    displayColumnsInfo(columnsInfo);
}

function displayColumnsInfo(columnsInfo){
    columns = columnsInfo.columns;
    $("#displayElements").empty();
    appendTableHeadToDisplayElmentsTable();
    $("#displayCandidate").empty();
    for(var i = 0; i < columns.length ; i ++){
        var element = {
            name:  columns[i].columnName
        }
        appendElementToDisplayCandidate(element);
    }
    reorderElements();
}

function appendTableHeadToDisplayElmentsTable(){
    var head =
        "<tr>" +
            "<th class='column_index'>#</th>" +
            "<th class='column_name'>test</th>"+
            "<th class='column_label'>设置为标注列</th>"+
            "<th class='column_operation'>操作</th>"+
        "</tr>";
    $("#displayElements").append(head);
}

function appendElementToDisplayCandidate(element){
    var item =
        "<tr id='column_"+ element.name +"' name='" + element.name +"'>" +
            "<td class='index column_index'>" + "0" + "</td>" +
            "<td class='name column_name'>" + element.name+ "</td>" +
            "<td class='column_label'><input type='radio'/></td>" +
            "<td class='column_operation'>" +
                "<i class='operation fa fa-plus bg-yellow' onclick='add(column_"+ element.name + ");'></i>" +
            "</td>" +
        "</tr>";
    $("#displayCandidate").append(item);
}

function appendElementToDisplayElement( element ){
    var item =
        "<tr id='column_"+  element.name +"' name='" + element.name + "'>" + // 加column_的原因是，有些name可能是特殊值（如id, body）导致异常，
            "<td class='index column_index' id='index'>" + "0" + "</td>" +
            "<td class='name column_name' id='name'>" + element.name+ "</td>" +
            "<td class='column_primaryKey'><input name='" + element.name +"' type='checkbox' onclick='setAsPrimaryKey(" + element.name + ");'/></td>" +
            "<td class='column_operation'>" +
                "<i class='operation fa fa-minus bg-yellow' onclick='remove(column_"+ element.name +");'></i>" +
                "<i class='operation fa fa-arrow-up bg-yellow' onclick='moveUp(column_"+ element.name +");'></i>" +
                "<i class='operation fa fa-arrow-down bg-yellow' onclick='moveDown(column_"+ element.name +");'></i>" +
            "</td>" +
        "</tr>";
    $("#displayElements").append(item);
}

function reorderElements(){
    var displayElements = $("#displayElements").find("tr>td.index");
    var count = 1;
    for(var i = 0 ; i < displayElements.length ; i ++){
        displayElements[i].textContent = count;
        count ++;
    }

    var candidateElements = $("#displayCandidate").find("tr>td.index");
    for(var i = 0 ; i < candidateElements.length ; i++){
        candidateElements[i].textContent = count;
        count ++;
    }
}


function appendTableSelectorSectionToTimeline(tables){

}


function appendTableSelectorToTimeline(tables){
    console.log("test");
    $("#tableSelector").css("opacity" , 1);

    $("#tableOptions").empty();
    for(var i = 0 ; i < tables.tables.length ; i++){
        var option = "<option value='"+ tables.tables[i].table +"'>" + tables.tables[i].table + "</option>";
        $("#tableOptions").append(option);
    }
}

function getElementName(element){
    var attributes = element.attributes;
    for(var i = 0 ; i < attributes.length ; i++){
        if(attributes[i].name == "name"){
            return attributes[i].value;
        }
    }
    return "";
}

function add(element){
    var temp = document.getElementById(element.id);
    if(temp){

        element.remove();
        var elementParameter={
            name : getElementName(temp)
        };
        appendElementToDisplayElement(elementParameter);
        reorderElements();
    }
}

function remove(element){
    var temp = document.getElementById(element.id);
    if(temp){
        element.remove();
        var elementParameter={
            name : getElementName(temp)
        };
        appendElementToDisplayCandidate(elementParameter);
        reorderElements();
    }

}

function moveUp(element){
    var previousSibling =  element.previousSibling;
    if(previousSibling) {
        var parentNode = element.parentNode;
        parentNode.insertBefore(element, previousSibling);
        reorderElements();
    }
}

function moveDown(element){
    var nextSibling = element.nextSibling;
    if(nextSibling){
        var parentNode = element.parentNode;
        parentNode.insertBefore(nextSibling , element);
    }
    reorderElements();
}

function createTask(){
    var database_url = "jdbc:mysql://" + $("#database_url").val() + ":" + $("#database_port").val() + "/" + $("#database_database").val();
    var database_user = $("#database_user").val();
    var database_pwd = $("#database_pwd").val();


    var tableName = $("#tableOptions").val();
    var columns = $("#displayElements").find("tr");
    var primaryKey = getPrimaryKey();
    var displayColumns = primaryKey ;// the primary key will be put on the first place

    if(columns.length > 1){
        // the columns[0] is the head of the table
        for(var i = 1 ; i < columns.length ; i++){
            var name = getElementName(columns[i]);
            if(name && name != primaryKey){
                displayColumns += "," + name ;
            }
        }
    }

    var parameters = {
        requestType: 'createTask',
        databaseURL: database_url,
        databaseUser:database_user,
        databasePwd: database_pwd,
        databaseTableName:tableName,
        primaryKey:primaryKey,
        displayColumns: displayColumns
    }

    $.ajax({
        type:'Post',
        url:'ConstructLabelTask',
        data: parameters,
        async: false,
        success:function(data) {
            if(data["condition"] == "succeed"){
                alert("创建成功");
            }else{
                alert("创建失败");
            }
        }
    });
}

function setAsPrimaryKey(itemID){
    var itemName = itemID.name;
    var items = document.getElementsByClassName("column_primaryKey");
    var operation ;
    for(var i = 0 ; i < items.length ; i++){
        var temp = items[i].childNodes[0];
        if(temp.name == itemName){
            operation = temp.checked;
            break;
        }
    }
    if(operation == true) {
        for (var i = 0; i < items.length; i++) {
            var temp = items[i].childNodes[0];
            if(temp.name != itemName){
                temp.checked = false;
            }
        }
    }
}

function getPrimaryKey(){
    var items = document.getElementsByClassName("column_primaryKey");
    for(var i = 0 ; i < items.length ; i ++){
        var temp = items[i].childNodes[0] ; // get the <input> element
        if(temp.checked == true)
        {
            return temp.name ;
        }
    }
    return "";
}