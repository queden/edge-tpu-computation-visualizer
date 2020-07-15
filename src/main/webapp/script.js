async function submitTimeZone() {
    console.log("submit");
    const select = document.getElementById("time-zone");
    const zone = select.options[select.selectedIndex].value;

    await fetch('visualizer?time=true&zone=' + zone, {method: 'GET'});
}

async function uploadFile() {
    const call = await fetch('/visualizer?time=false', {method: 'GET'});
    const response = await call.json();

    const box = document.getElementById("uploaded-file");
    box.innerHTML = '';

    if (response.name != "null") {
        addFileInfo(response);
    } else {
        const p = document.createElement("p");

        p.innerHTML = "Please select a file.";
        p.style.color = "red";

        box.appendChild(p);
    }

    const timeZoneBox = document.getElementById("time-zone-box");
    timeZoneBox.innerHTML = "Time Zone: " + response.zone;
}

function addFileInfo(response) {
    const box = document.getElementById("uploaded-file");

    var p = document.createElement("p");
    p.innerHTML = response.name + " at " + response.time;
    p.style.fontWeight = "bold";

    box.appendChild(p);

    p = document.createElement("p");
    p.innerHTML = "File size: " + response.size;

    box.appendChild(p);

    p = document.createElement("p");
    p.innerHTML = "Simulation trace name: " + response.trace;

    box.appendChild(p);

    p = document.createElement("p");
    p.innerHTML = "Number of tiles: " + response.tiles;

    box.appendChild(p);

    p = document.createElement("p");
    p.innerHTML = "Narrow memory size: " + response.narrow + " Bytes";

    box.appendChild(p);

    p = document.createElement("p");
    p.innerHTML = "Wide memory size: " + response.wide + " Bytes";

    box.appendChild(p);
}

// Populates the select list
async function loadFiles() {
    const call = await fetch('/report?process=loadfiles', {method: 'GET'});
    const files = await call.json();

    const timeZoneBox = document.getElementById("time-zone-box");
    timeZoneBox.innerHTML = "Time Zone: " + files[0].zone;

    const select = document.getElementById("uploaded-files");
    select.innerHTML = '';

    files.forEach((file) => {
        select.appendChild(createFile(file));
    });
}

// Creates file options out of previously uploaded files to append to the select list
function createFile(file) {
    const selectOption = document.createElement("option");
    selectOption.value = file.id;
    selectOption.text = file.name + " at " + file.dateTime;
    return selectOption;
}

// Runs the visualization of the chosen simulation trace
async function runVisualization() {
    const select = document.getElementById("uploaded-files");
    const file = select.options[select.selectedIndex].value;

    const preprocess = await fetch('/report?process=pre&file=' + file, {method: 'GET'});
    const preprocessResponse = await preprocess.json();

    // Process initial json information
    // TODO: Substitute
    const box = document.getElementById("test-box");
    box.innerHTML = '';
    const init = document.createElement("p");
    init.innerHTML = preprocessResponse.message;
    box.appendChild(init);

    // i < prepprocessResponse.numTraces
    for (i = 0; i < prepprocessResponse.numTraces; i += 1000) {
        // Run through the traces, information processing will happen within the function
        await runTraces(i);
    }
}

// Processes the different chunks of specified trace indicies
// start is the beginning index of the traces to be processed
async function runTraces(start) {
    const box = document.getElementById("test-box");
    const traceResponse = await fetch('/report?process=post&start=' + start, {method: 'GET'});
    const traceProcess = await traceResponse.json();

    box.appendChild(responseMessage);

    // Process json trace information
    // TODO: Substitute
    var responseMessage = document.createElement("p");

    if (traceProcess.error == null) {
        responseMessage.innerHTML += "Traces validated";
    }
    else {
        responseMessage.innerHTML = traceProcess.error.message;
    }

    box.appendChild(responseMessage);
}

var ele = document.getElementById('container');
if(ele) {
    ele.style.visibility = "visible";
 }  
fetch("heatmapData.json")
  .then(response => response.json())
  .then((heatmapData) => {

var heatmap = new ej.heatmap.HeatMap({
    titleSettings: {
            text: 'Tile Visualization',
            textStyle: {
                size: '25px',
                fontWeight: '500',
                fontStyle: 'Normal',
                fontFamily: 'Montserrat'
            }
        },
        xAxis: {
            labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11','12','13','14','15',
            '16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31','32'],
            labelRotation: 45,
            labelIntersectAction: 'None',
        },
        yAxis: {
            title : {text: 'Tiles'},
            labels: ['Tile 0', 'Tile 1', 'Tile 2', 'Tile 3', 'Tile 4', 'Tile 5', 'Tile 6', 'Tile 7', 'Tile 8', 
            'Tile 9', 'Tile 10', 'Tile 11', 'Tile 12', 'Tile 13', 'Tile 14', 'Tile 15'],
        },
        dataSource: heatmapData,
        dataSourceSettings: {
            isJsonData: true,
            adaptorType: 'Table',
            xDataMapping: 'Region',
        }, paletteSettings: {
            palette: [
            { color: '#caf0f8' },
            { color: '#ade8f4' },
            { color: '#90e0ef' },
            { color: '#48cae4' },
            { color: '#00b4d8' },
            { color: '#0096c7' },
            { color: '#0077b6' },
            { color: '#023e8a' },
            { color: '#03045e' }
            ],
        },
        cellSettings: {
            border: {
                width: 1,
                radius: 4,
                color: 'white'
            }
        }
});
        
heatmap.appendTo('#element');
});