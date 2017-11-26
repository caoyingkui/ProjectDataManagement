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
                abPath : 'StackOverflow',
                name : 'StackOverflow',
                detail : 'details',
                size : "50M",
                isFolder : false,

            },
            {
                abPath : 'GitHub',
                name : 'GitHub',
                detail : 'details',
                size : "50M",
                isFolder : false,
            },
            {
                abPath : 'Mail',
                name : 'Mail',
                detail : 'details',
                size : "50M",
                isFolder : true,
            },
            {
                abPath : 'Bugzilla',
                name : 'Bugzilla',
                detail : 'details',
                size : '50M',
                isFolder : true,
            }
        ],
        isSearch: false,

    },
    components: {
        doc: {
            props: ['abPath', 'name', 'detail', 'size', 'isFolder', 'showPath'],
            template: '<tr v-if="isFolder">\n' +
            '              <td>\n' +
            '                  <span class="box" ><input type="checkbox" :value="abPath" name="checks"></span>\n' +
            '                  <span class="glyphicon glyphicon-folder-open" style="padding-right: 10px"></span>\n' +
            '                  <span class="tran-hand nexthref" onclick="nexthref(this.textContent)">{{ name }}</span>\n' +
            '                  <span style="color: #ccc; padding-left: 10px" v-if=showPath>{{ abPath }}</span>\n' +
            '                  <span style="color: #ccc; padding-left: 10px">{{ size }}</span>\n' +
            '              </td>\n' +
            '          </tr>\n' +
            '          <tr v-else>\n' +
            '              <td>\n' +
            '                  <span class="box" ><input type="checkbox" :value="abPath" name="checks"></span>\n' +
            '                  <span class="glyphicon glyphicon-file" style="padding-right: 10px"></span>\n' +
            '                  <span>{{ name }}</span>\n' +
            '                  <span style="color: #ccc; padding-left: 10px" v-if=showPath>{{ abPath }}</span>\n' +
            '                  <span style="color: #ccc; padding-left: 10px">{{ size }}</span>\n' +
            '              </td>\n' +
            '          </tr>\n'
        },



    }
});

var pages = new Vue({
    el : '#pages',
    data: {
        pages:[
            {
                text: '<<',
                isCurrent: false
            },
            {
                text: '3',
                isCurrent: false
            },
            {
                text: '4',
                isCurrent: false
            },
            {
                text: '5',
                isCurrent: true
            }
        ]
    },
    components: {
        page: {
            props: ['text', 'isCurrent'],
            template: '<li class="active" v-if="isCurrent"><a class="tran-hand">{{ text }}</a></li>\n' +
            '<li v-else><a class="tran-hand">{{ text }}</a></li>\n'
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

function download() {
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