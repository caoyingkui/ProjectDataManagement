var sidebar = new Vue({
    el : '#labels',
    data: {
        isSources: true,
        isProjects: false
    }
});
var path = new Vue({
    el : '#path',
    data: {
        paths: [
            'Projects',
            'Project1',
            'Folders'
        ],
        cur: 'Documents'
    },
    components : {
        drc: {
            props: ['name'],
            template: '<li><a class="tran-hand prevhref" onclick="prevhref(this.textContent)">{{name}}</a></li>'
        },
        cur: {
            props: ['name'],
            template: '<li class="active">{{name}}</li>'
        }

    }
});

var lists = new Vue({
    el : '#lists',
    data: {
        items: [
            {
                headId : 'head1',
                abPath : 'StackOverflow',
                name : 'StackOverflow',
                detail : 'details',
                collapseHref : '#collapse1',
                collapseId : 'collapse1',
                size : "50M",
                isFolder : false,

            },
            {
                headId : 'head2',
                abPath : 'GitHub',
                name : 'GitHub',
                detail : 'details',
                collapseHref : '#collapse2',
                collapseId : 'collapse2',
                size : "50M",
                isFolder : false,
            },
            {
                headId : 'head3',
                abPath : 'Mail',
                name : 'Mail',
                detail : 'details',
                collapseHref : '#collapse3',
                collapseId : 'collapse3',
                size : "50M",
                isFolder : true,
            },
            {
                headId : 'head4',
                abPath : 'Bugzilla',
                name : 'Bugzilla',
                detail : 'details',
                collapseHref : '#collapse4',
                collapseId : 'collapse4',
                size : '50M',
                isFolder : true,
            }
        ],
        isSearch: false


    },
    components: {
        doc: {
            props: ['headId', 'abPath', 'name', 'detail', 'collapseHref', 'collapseId', 'icon', 'titleClass', 'size', 'isFolder', 'showPath'],
            template: '<div class="panel panel-default" v-if=isFolder>\n' +
            '                    <div class="panel-heading" :id="headId">\n' +
            '                        <span class="box" ><input type="checkbox" :value="abPath" name="checks"></span>\n' +
            '                        <span class="glyphicon glyphicon-folder-open" style="padding-right: 10px"></span>\n' +
            '                        <span class="panel-title tran-hand nexthref" style="width:100px" onclick="nexthref(this.textContent)">{{ name }}</span>\n' +
            '                        <span class="panel-title" style="color: #ccc; padding-left: 10px" v-if=showPath>{{ abPath }}</span>' +
            '                        <span class="panel-title" style="color: #ccc; padding-left: 10px">{{ size }}</span>' +
            '                        <div class="navbar-right chevron-down" >\n' +
            '                            <span class="glyphicon glyphicon-chevron-down tran-hand" data-toggle="collapse" data-parent="#lists" :href="collapseHref"></span>\n' +
            '                        </div>\n' +
            '                        \n' +
            '                    </div>\n' +
            '                    <div :id="collapseId" class="panel-collapse collapse">\n' +
            '                        <div class="panel-body" v-html="detail">\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '         </div>\n' +
            '         <div class="panel panel-default" v-else>\n' +
            '                    <div class="panel-heading" :id="headId">\n' +
            '                        <span class="box" ><input type="checkbox" :value="abPath" name="checks"></span>\n' +
            '                        <span class="glyphicon glyphicon-file" style="padding-right: 10px"></span>\n' +
            '                        <span class="panel-title" style="width:100px">{{ name }}</span>\n' +
            '                        <span class="panel-title" style="color: #ccc; padding-left: 10px" v-if=showPath>{{ abPath }}</span>' +
            '                        <span class="panel-title" style="color: #ccc; padding-left: 10px">{{ size }}</span>' +
            '                        <div class="navbar-right chevron-down" >\n' +
            '                            <span class="glyphicon glyphicon-chevron-down tran-hand" data-toggle="collapse" data-parent="#lists" :href="collapseHref"></span>\n' +
            '                        </div>\n' +
            '                        \n' +
            '                    </div>\n' +
            '                    <div :id="collapseId" class="panel-collapse collapse">\n' +
            '                        <div class="panel-body" v-html="detail">\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '          </div>'
        }

    }
});

function requestBrowse(pathStr) {
    var obj = {};
    var para = {
        requestType: "browseDirectory",
        directory: pathStr
    };
    $.ajax({
        type: "Post",
        url: "dataBrowser",
        data: para,
        async: false,
        success: function (data) {
            obj = data;
        }
    });

    return obj;
}

function showPath(pathStr) {
    var arr = pathStr.split('\\');
    path.paths=[];
    for (var i = 1; i < arr.length - 1; i++){
        path.paths.push(arr[i]);
    }
    path.cur = arr[arr.length-1];
    if (path.paths[0] == "dataType") path.paths[0] = "Sources";
    else if (path.paths[0] == "projects") path.paths[0] = "Projects";
    else if (path.cur == "dataType") path.cur = "Sources";
    else if (path.cur == "projects") path.cur = "Projects";
}

function showList(arr) {
    lists.items=[];

    for (var i = 0; i < arr.length; i++) {
        var tmp = {
            headId : '',
            abPath : '',
            name : '',
            detail : '',
            collapseHref : '',
            collapseId : '',
            size : '',
            isFolder : true,
        };
        tmp.headId = "head" + i;
        tmp.abPath = arr[i].dir;
        tmp.name = arr[i].fileName;
        tmp.size = arr[i].dataSize;
        //tmp.detail = JSON.stringify(arr[i].metaInfo);
        tmp.detail = change(arr[i].metaInfo);
        tmp.collapseHref = "#collapse" + i;
        tmp.collapseId = "collapse" + i;
        if (arr[i].type == "directory") {
            tmp.isFolder = true;
        } else if (arr[i].type == "file") {
            tmp.isFolder = false;
        }
        lists.items.push(tmp);
    }
    lists.isSearch=false;

    var checkboxes = document.getElementsByName("checks");
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked)
            checkboxes[i].checked = false;
    }


}
function change(obj) {
    var result="<pre>";
    for(var item in obj){
        if(typeof obj[item] == 'object') {
            result += item + ":\n";

            var inner = obj[item];
            for(var i = 0; i < inner.length; i++){
                for(var ikey in inner[i]) {
                    result += "\t" + ikey + ":" + inner[i][ikey] + ",";
                }
                result += "\n";
            }

        } else {
            result += item + ":" + obj[item] + "\n";
        }

    }

    return result + "</pre>";
}

$(document).ready(function(){

    obj = requestBrowse("\\dataType\\");
    path.paths = [];
    path.cur = "Sources";
    showList(obj.data);

});

function sidebutton(text){
    var obj;

    if (text == "Sources") {
        sidebar.isSources = true;
        sidebar.isProjects = false;

        obj = requestBrowse("\\dataType\\");
        path.paths = [];
        path.cur = "Sources";
        showList(obj.data);

    } else if (text == "Projects") {
        sidebar.isSources = false;
        sidebar.isProjects = true;

        obj = requestBrowse("\\projects");
        path.paths = [];
        path.cur = "Projects";
        showList(obj.data);
    }

};

function prevhref(name){
    var pathstr = "";
    var i = path.paths.length - 1;

    while(i > 0 && name != path.paths[i]) {
        i--;
    }
    while (i > 0) {
        pathstr = "\\" + path.paths[i] + pathstr;
        i--;
    }

    if (path.paths[0] == "Sources") {
        pathstr = "\\dataType" + pathstr;
    } else if (path.paths[0] == "Projects") {
        pathstr = "\\projects" + pathstr;
    }

    var obj = requestBrowse(pathstr);

    showList(obj.data);
    showPath(obj.absolutePath);

};

function nexthref(name) {
    for (var i = 0; i < lists.items.length; i++) {
        if (name == lists.items[i].name) {
            var obj = requestBrowse(lists.items[i].abPath);
            showList(obj.data);
            showPath(obj.absolutePath);
            break;
        }
    }
};

function submit() {
    var checkboxes = document.getElementsByName("checks");
    var value = null;

    for (var i = 0; i < checkboxes.length; i++){
        if (checkboxes[i].checked) {
            if (value == null) {
                var tmp = checkboxes[i].value;
                value = tmp.replace(/\\/g,"%5C");
            } else {
                value = value + "%7C" + checkboxes[i].value.replace(/\\/g,"%5C");
            }
        }
    }
    alert(value);
    if (value == null) {
        alert("No file chosen.");
    } else {
        window.open('Download?requestType=downloadFiles&filePaths='+value, '_blank');
    }

};

function search () {
    var searchStr = document.getElementById("searchword").value;
    var para = {
        requestType: "searchDirectory",
        query: searchStr
    };
    $.ajax({
        type: "Post",
        url: "dataBrowser",
        data: para,
        async: false,
        success: function (data) {
            sidebar.isSources = false;
            sidebar.isProjects = false;
            lists.items = [];
            path.paths = [];
            path.cur = "";
            if (data.dataType == "searchFailed") {
                path.cur = data.errorLog;
            } else if (data.dataType == "searchResult") {
                showList(data.data);
                path.cur = "search: " + searchStr;
                lists.isSearch=true;

            }
        }
    });
};