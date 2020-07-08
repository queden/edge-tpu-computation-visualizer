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
    const preprocess = await fetch("/report?process=pre", {method: "POST"});
    const preprocessResponse = preprocess.json();

    // Process json intializations

    for (i = 0; i < preprocessResponse.traces; i += 1000) {
        const traceProcess = await fetch("report?process=post&start=" + i, {method: "POST"});
        const traceResponse = traceProcess.json();

        // Process json trace information
    }
}

// Test if the simulation trace was correctly formed out of datastore
function getViz() {
    fetch('/visualizer', {method: 'GET'});
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
});