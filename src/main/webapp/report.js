var data1 = [];
var data2 = [];
var narrow = "Narrow Memory";
var wide = "Wide Memory";

for (var i = 0; i < 1400; i++) {
    var datum = {};
    datum.location = i;
    datum.layer = (1400 - i) % 23;
    datum.tile = i % 2;
    data1.push(datum);
}
for (var i = 0; i < 1400; i++) {
    var datum = {};
    datum.location = i;
    datum.layer = i % 23;
    datum.tile = 0;
    data2.push(datum);
}

//change the memory location
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

            displayChart(data, memoryType, section);
        });

    // generate initial graph
    data = filterJSON(rawData, 'tile', '0');
    console.log(data)
    displayChart(data,memoryType, '0');
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
        return d.layer;
    })]).range(['#FF5714', '#ccc', '#1BE7FF']);
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
                        var newHeight = (27 * data.length) / 15
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
                    return colorScale(d.layer);
                }
            })


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
                    var newHeight = (3 * data.length) / 280
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
                    return colorScale(d.layer);
                },
                stroke: function(d) {
                    return colorScale(d.layer);
                }
            })
    }

}
extractData(data1, wide)