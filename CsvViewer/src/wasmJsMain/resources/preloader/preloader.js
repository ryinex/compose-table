const list = []
const moduleScript = "ComposeApp"
var map = new Map();
var totalMap = new Map();
var loadedMap = new Map();
var launched = false
if (list.length == 0) {
    launchApplication();
} else {
    list.forEach(loadEach);
}

function loadEach(asset) {
    map.set(asset, 0);

    var loader = new Preload()
    loader.onload = function (event) {
        map.set(asset, 100);
        updateProgress();
    };
    loader.onprogress = function (event) {
        if (!isNaN(parseFloat(event.progress))) {
            map.set(asset, Math.round(event.progress * 100));
            totalMap.set(asset, parseFloat(event.total))
            loadedMap.set(asset, parseFloat(event.loaded))
            updateProgress();
        }
    };
    loader.open(asset);
    loader.read();
}

function updateProgress() {
    let total = Math.floor((Array.from(map.values()).reduce((sum, num) => sum + num, 0)) / list.length);

    if (totalMap.size == map.size) {
        let totalBytes = (Array.from(totalMap.values()).reduce((sum, num) => sum + num, 0));
        let loadedBytes = (Array.from(loadedMap.values()).reduce((sum, num) => sum + num, 0));
        let loadedPercentage =  Math.floor(parseFloat(loadedBytes) / totalBytes * 100);
        
        document.getElementById("progress").textContent = loadedPercentage + "%";
    }

    if (total >= 100 || list.length == 0) {
        launchApplication();
    }
}

function launchApplication() {
    if (launched) return
    launched = true
    var sc = document.createElement("script");
    sc.setAttribute("src", `${moduleScript}.js`);
    sc.setAttribute("type", "application/javascript");
    setTimeout(() => {
        document.head.appendChild(sc);
//        document.body.getElementsByClassName("loader-container").item(0)?.remove()
    }, 1000);
}