// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

async function runSimulation() {
    // console.log("clicked");
    const preprocess = await fetch('/report?process=pre', {method: 'GET'});
    const preprocessResponse = await preprocess.json();

    // Process initial json information
    const box = document.getElementById("test-box");
    box.innerHTML = '';
    const init = document.createElement("p");
    // console.log(preprocessResponse.total);
    init.innerHTML = preprocessResponse.message;
    box.appendChild(init);

    for (i = 0; i < preprocessResponse.total; i += 1000) {
        // Run through the traces, information processing will happen within the function
        runTraces(i);
    }
}

// Async function, wasn't sure if the asynchronous calls were messing up the sequential calls
// async function runTraces(start) {
//     const box = document.getElementById("test-box");
//     const traceResponse = await fetch('/report?process=post&start=' + start, {method: 'GET'});
//     const traceProcess = await traceResponse.json();

//     console.log(traceProcess.traces);

//     // Process json trace information
//     var responseMessage = document.createElement("p");
//     responseMessage.innerHTML = "Call " + (traceProcess.call + 1) + ": " + traceProcess.traces;
//     box.appendChild(responseMessage);
// }

// Auxillary run function to try and stabilize the calls, didn't work, still out of order
// function auxillaryRun(start) {
//     runTraces(start);
// }


// Promise handling method using arrow functions, didn't work to stabilize
function runTraces(start) {
    fetch('/report?process=post&start=' + start, {method: 'GET'}).then(response => response.json()).then((traceProcess) => {
        const box = document.getElementById("test-box");
        console.log(traceProcess.traces);

        // Process json trace information
        var responseMessage = document.createElement("p");
        responseMessage.innerHTML = "Call " + (traceProcess.call + 1) + ": " + traceProcess.traces;
        box.appendChild(responseMessage);
    });
}

// Test if the simulation trace was correctly formed out of datastore
function getViz() {
    fetch('/visualizer', {method: 'GET'});
}

var ele = document.getElementById('container');
if(ele) {
    ele.style.visibility = "visible";
 }  
var heatmapData =  [
        { 'Region': '1', 'Tile 0': 93, 'Tile 1': 45, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '2', 'Tile 0': 100, 'Tile 1': 14, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '3', 'Tile 0': 67, 'Tile 1': 45, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '4', 'Tile 0': 32, 'Tile 1': 98, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '5', 'Tile 0': 32, 'Tile 1': 44, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '6', 'Tile 0': 53, 'Tile 1': 65, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '7', 'Tile 0': 23, 'Tile 1': 25, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '8', 'Tile 0': 23, 'Tile 1': 85, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '9', 'Tile 0': 84, 'Tile 1': 69, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '10', 'Tile 0': 88, 'Tile 1': 36, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '11', 'Tile 0': 58, 'Tile 1': 62, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '12', 'Tile 0': 87, 'Tile 1': 53, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '13', 'Tile 0': 78, 'Tile 1': 24, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '14', 'Tile 0': 48, 'Tile 1': 15, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '15', 'Tile 0': 84, 'Tile 1': 57, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '16', 'Tile 0': 78, 'Tile 1': 48, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '17', 'Tile 0': 84, 'Tile 1': 86, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '18', 'Tile 0': 8, 'Tile 1': 59, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '19', 'Tile 0': 78, 'Tile 1': 91, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '20', 'Tile 0': 15, 'Tile 1': 27, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '21', 'Tile 0': 94, 'Tile 1': 28, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '22', 'Tile 0': 97, 'Tile 1': 39, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '23', 'Tile 0': 98, 'Tile 1': 86, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '24', 'Tile 0': 54, 'Tile 1': 45, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '25', 'Tile 0': 15, 'Tile 1': 19, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '26', 'Tile 0': 54, 'Tile 1': 86, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '27', 'Tile 0': 94, 'Tile 1': 48, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '28', 'Tile 0': 59, 'Tile 1': 25, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '29', 'Tile 0': 84, 'Tile 1': 14, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '30', 'Tile 0': 69, 'Tile 1': 86, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '31', 'Tile 0': 14, 'Tile 1': 95, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121},
        { 'Region': '32', 'Tile 0': 78, 'Tile 1': 48, 'Tile 2': 101, 'Tile 3': 112, 'Tile 4': 103, 'Tile 5': 121, 'Tile 6': 121, 'Tile 7': 121, 'Tile 8': 121, 'Tile 9': 93, 'Tile 10': 101, 'Tile 11': 112, 'Tile 12': 103, 'Tile 13': 121, 'Tile 14': 121, 'Tile 15': 121}
        ];

var heatmap = new ej.heatmap.HeatMap({
    titleSettings: {
            text: 'Tile Visualization',
            textStyle: {
                size: '15px',
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
