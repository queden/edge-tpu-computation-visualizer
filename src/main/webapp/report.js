async function test() {
  const pre = await fetch('/report?process=pre&fileId=5348024557502464', {method: 'GET'});
  const preresp = await pre.json();

  const post = await fetch('/report?process=post&start=0', {method: 'GET'});
  const postresp = await post.json();
   console.log(preresp);
   console.log(postresp);
   return preresp;
}
;(async () => {
  const result = await test()
  console.log(result)
var narrowSize = result["narrowSize"] 
var data1 = new Array(narrowSize);
var data2 = [];
var narrow = "Narrow Memory";
var wide = "Wide Memory";
var narrowAlloc = result['tensorAllocationNarrow'];
console.log(narrowAlloc.length)
console.log(data1.length)
var max = 0;
for (var i = 0; i < narrowAlloc.length; i++){
    var allocs = narrowAlloc[i]["tensorTileAllocation_"][0]["tensorAllocation_"];
    var tileAllocs = narrowAlloc[i]["tensorTileAllocation_"];
    for (var tile = 0; tile < tileAllocs.length; tile++){
        allocs = tileAllocs[tile]["tensorAllocation_"]
        for (var j = 0; j < allocs.length; j++){
            var alloc = allocs[j];
            var start = 0;
            var end = 0;
            start = alloc["baseAddress_"];
            end = start + alloc["size_"];
            for (var k = start; k < end; k++){
                if (end > narrowSize){
                    break;
                }
                var datum = {}
                datum.location = k;
                datum.layer = narrowAlloc[i]["layer_"];
                datum.tile = tile;
                datum.label = alloc["tensorLabel_"]
                data1.push(datum)
            }
        }
    }
}


//change the memory location -- TODO
function change(value) {
    if (value === 1) {
        extractData(data1, wide);
    } else {
        extractData(data2, narrow);
    }
}
//filter the data based on the tile selected 
function filterJSON(json, key, value) {
    var result = [];
    json.forEach(function(val, idx, arr) {
        if (val[key] == value) {
            result.push(val)
        }
    })
    return result;
}

// Get the data
function extractData(rawData, memoryType) {
    var data;
    d3.select('#inds')
        .on("change", function() {
            var sect = document.getElementById("inds");
            var section = sect.options[sect.selectedIndex].value;
            console.log(section)
            data = filterJSON(rawData, 'tile', section);
            //debugger
            //data.forEach(function(d) {
            //    d.tile = +d.tile;
            //    d.active = true;
            // });
            var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location))
            displayChart(sortedData, memoryType, section);
        });

    // generate initial graph
    data = filterJSON(rawData, 'tile', '0');
    var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location))
    console.log(data)
    displayChart(sortedData,memoryType, '0');

}

//Set up the chart
var obj = document.getElementById('chart');
var divWidth = obj.offsetWidth;
var margin = {
        top: 10,
        right: 10,
        bottom: 100,
        left: 40
    },
    margin2 = {
        top: 430,
        right: 10,
        bottom: 20,
        left: 40
    },
    width = divWidth - 25,
    height = 500 - margin.top - margin.bottom,
    height2 = 500 - margin2.top - margin2.bottom;
var x = d3.scale.ordinal().rangeBands([0, width], 0),
    x2 = d3.scale.ordinal().rangeBands([0, width], 0),
    y = d3.scale.ordinal().rangeRoundBands([0, height], 0),
    y2 = d3.scale.linear().domain([1400, 0]).range([height2, 0]);
var svg = d3.select("body").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom);
var focus = svg.append("g")
    .attr("class", "focus")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
var context = svg.append("g")
    .attr("class", "context")
    .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");
var xAxis = d3.svg.axis().scale(x).orient("bottom"),
    xAxis2 = d3.svg.axis().scale(x2).orient("bottom").tickValues([]),
    yAxis = d3.svg.axis().scale(y).orient("left");

//Draws the chart
function displayChart(data, memoryType, section) {
    //Display the memory type
    const displayMemoryType = document.getElementById("memory-type");
    displayMemoryType.innerHTML = memoryType;
    //Display tile 
    const displayTile = document.getElementById("tile");
    displayTile.innerHTML = "Tile " + section;

    colorScale = d3.scale.ordinal().domain([0, d3.max(data, function(d) {
        return d.label;
    })]).range(['#FF5714', '#ccc', '#1BE7FF']);
    layerPosition = d3.scale.ordinal().domain(d3.map(data, function(d) {
        return d.layer;
    })).range([0, 1, 2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22]);

    console.log(focus.node().getBBox());
    var bars = focus.selectAll('.bar').remove();
    focus.select(".x.axis").remove();
    focus.select(".y.axis").remove();
    //update scales
    
    x.domain(data.map(function(d) {
        return d.location
    }));
    y.domain(data.map(function(d) {
        return d.layer
    }));
    x2.domain(x.domain());
    //make axis
    focus.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    focus.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    //add brush
    var brush = d3.svg.brush()
        .x(x2)
        .on("brush", brushed);
    //exit(data)
    enter(data)
    updateScale(data)

    var subBars = context.selectAll('.subBar')
        .data(data)
    subBars.enter().append("rect")
        .classed('subBar', true)

        .attr({
            height: function(d) {
                return 10;
            },
            width: function(d) {
                return x.rangeBand()
            },
            x: function(d) {
                return x2(d.location);
            },
            y: function(d) {
                return y2(d.layer) - 10
            }
        })

    context.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height2 + ")")
        .call(xAxis2);

    context.append("g")
        .attr("class", "x brush")
        .call(brush)
        .selectAll("rect")
        .attr("y", -10)
        .attr("height", 50);

    function brushed() {
        var selected = null;
        selected = x2.domain()
            .filter(function(d) {
                return (brush.extent()[0] <= x2(d)) && (x2(d) <= brush.extent()[1]);
            });

        var start;
        var end;

        if (brush.extent()[0] != brush.extent()[1]) {
            start = selected[0];
            end = selected[selected.length - 1] + 1;
        } else {
            start = 0;
            end = 5;
        }
        var updatedData = data.slice(start, end);

        update(updatedData);
        enter(updatedData);
        exit(updatedData);
        updateScale(updatedData)

    }
    
    function updateScale(data) {
        var tickScale = d3.scale.pow().range([data.length / 10, 0]).domain([data.length, 0]).exponent(.5)
        var brushValue = brush.extent()[1] - brush.extent()[0];
        if (brushValue === 0) {
            brushValue = width;
        }

        var tickValueMultiplier = Math.ceil(Math.abs(tickScale(brushValue)));
        var filteredTickValues = data.filter(function(d, i) {
            return i % tickValueMultiplier === 0
        }).map(function(d) {
            return d.location
        })
        focus.select(".x.axis").call(xAxis.tickValues(filteredTickValues));
        focus.select(".y.axis").call(yAxis);
    }

    function update(data) {
        x.domain(data.map(function(d) {
            return d.location
        }));
        //y.domain([0, 23]);
        y.domain(data.map(function(d) {
            return d.layer
        }));

        var bars = focus.selectAll('.bar')
            .data(data)
        bars
            .attr({
                height: function(d, i) {
                    if (data.length < 23) {
                        var newHeight = 410 / data.length;
                        return newHeight;
                    }

                    return 20;
                },
                width: function(d) {
                    return x.rangeBand()
                },
                x: function(d) {

                    return x(d.location);
                },
                y: function(d) {

                    return 379 - y(d.layer)

                },
                fill: function(d) {
                    return colorScale(d.label);
                }
            })
        
       console.log(focus.node().getBBox());

    }

    function exit(data) {
        var bars = focus.selectAll('.bar').data(data)
        bars.exit().remove()
    }

    function enter(data) {
        console.log(data.length);
        x.domain(data.map(function(d) {
            return d.location
        }));
        //y.domain([0, d3.max(data, function(d) { return d.layer;})]);
        y.domain(data.map(function(d) {
            return d.layer
        }));
        // var dataFilter = data.map(function(d){return {time: d.time, value:d[selectedGroup]} })

        var bars = focus.selectAll('.bar')
            .data(data)
        bars.enter().append("rect")
            .style("stroke-linejoin", "round")
            .classed('bar', true)
            .attr({
                height: function(d, i) {
                    var newHeight = 410 / data.length;
                    return 17;
                },
                width: function(d) {
                    return x.rangeBand()
                },
                x: function(d) {

                    return x(d.location);
                },
                y: function(d) {
                    return 379 - y(d.layer)
                },
                fill: function(d) {
                    return colorScale(d.label);
                },
                stroke: function(d) {
                    return colorScale(d.label);
                }
            })
    }

}

extractData(data1, wide)

})()
