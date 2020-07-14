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

function loadMemory(){ 
// set the dimensions of the chart
    var obj = document.getElementById('chart');
    var divWidth = obj.offsetWidth;
    var margin = {top: 30, right: 0, bottom: 20, left: 0},
        width = divWidth - 25,
        height = 600 - margin.top - margin.bottom,
        formatNumber = d3.format(","),
        transitioning;
// set the dimensions of the internal rectangles by adding scalers so that the object is rendered to the size we want it to. 
    var x = d3.scaleLinear()
        .domain([0, width])
        .range([0, width]);
    var y = d3.scaleLinear()
        .domain([0, height])
        .range([0, height]);
// add the treemap
    // sets the dimensions of the treemap
    var treemap = d3.treemap()
            .size([width, height])
            .paddingInner(0)
            .round(false);
    //appends the treemap as a svg object -- if you want to understand svg more, look at https://www.tutorialspoint.com/d3js/d3js_introduction_to_svg.htm
    var svg = d3.select('#'+'chart').append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.bottom + margin.top)
        .style("margin-left", -margin.left + "px")
        .style("margin.right", -margin.right + "px")
        .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .style("shape-rendering", "crispEdges");
    // add a "grandparent" rectangle to the svg which will be the start rectangle 
    var grandparent = svg.append("g")
            .attr("class", "grandparent");
        grandparent.append("rect")
            .attr("y", -margin.top)
            .attr("width", width)
            .attr("height", margin.top)
            .attr("fill", '#023e8a');
        grandparent.append("text")
            .attr("x", 6)
            .attr("y", 6 - margin.top)
            .attr("dy", ".75em");
    // function to retrieve the data from the json file and add it to the treemap
    d3.json("data.json",function(data) {
        // because the data is arranged in hierarchical format, we can pass it to d3.hierarchy to display it in such format.
        // by passing the data in to d3.heirarchy, we get the option to access each node from their links (parents/children).
        // line 82 will basically return an array of nodes associated with a specified root node. 
        var root = d3.hierarchy(data);
        console.log(root); //view this in your console
        // pass in root (the array of nodes) into the treemap. 
        //Call sum on the root to add all the quanities in "value" of the desendant nodes of each node. 
        // call sort to order all of the node based on the sum of "value"s (in our case all of the nodes have equal value but we want it to be ordered by start.)
        treemap(root
            .sum(function (d) {
                return d.value;
            })
            .sort(function (a, b) {
                return b.height - a.height || b.value - a.value
            })
        );
        display(root);
        //pass in root into display function
        function display(d) {
            // write text into grandparent and activate click handler
            grandparent
                .datum(d.parent)
                .on("click", transition)
                .select("text")
                .text(name(d));
            // grandparent color
            grandparent
                .datum(d.parent)
                .select("rect")
                .attr("fill", function () {
                    return '#bbbbbb'
                })
                .style("color", "white");
            // insert an object g1 before the "grandparents"
            var g1 = svg.insert("g", ".grandparent")
                .datum(d)
                .attr("class", "depth"); //depth of the node
            // Adds g1 who are children of the "grandparent". 
            var g = g1.selectAll("g")
                .data(d.children)
                .enter().
                append("g");
            // add class and click handler to all g's with children
            g.filter(function (d) { //extract only the "children" 
                return d.children;
            })
                .classed("children", true)  // classname = children
                .on("click", transition); // add the click transition
            g.selectAll(".child")
                .data(function (d) {
                    return d.children || [d]; //return the children of the parents or just itself
                })
                .enter().append("rect")  //add a rectangle for the child
                .attr("class", "child") 
                .call(rect);
            // add title to parents
            g.append("rect")
                .attr("class", "parent")
                .call(rect)
                .append("title")
                .text(function (d){
                    return d.data.name;
                });
            /* Adding a foreign object instead of a text object, allows for text wrapping */
            // to be printed inside each rectangle
            g.append("foreignObject")
                .call(rect)
                .attr("class", "foreignobj")
                .append("xhtml:div")
                .attr("dy", ".75em")
                .html(function (d) {
                    return '' +
                        '<p class="title"> ' + d.data.name + '</p>' +
                        '<p>' + formatNumber(d.value) + '</p>'
                    ;
                })
                .attr("class", "textdiv"); //textdiv class allows us to style the text easily with CSS
            
            // defines a recursive call to display the desendants of a node
            function transition(d) {
                if (transitioning || !d) return;
                transitioning = true;
                var g2 = display(d), //recursive call 
                    t1 = g1.transition().duration(650),
                    t2 = g2.transition().duration(650);
                // Update the domain sizing only after entering new elements.
                x.domain([d.x0, d.x1]);
                y.domain([d.y0, d.y1]);
                // Enable anti-aliasing during the transition -- smoother rectangles.
                svg.style("shape-rendering", null);
                // Draw child nodes on top of parent nodes.
                svg.selectAll(".depth").sort(function (a, b) {
                    return a.depth - b.depth;
                });
                // Fade-in entering text.
                g2.selectAll("text").style("fill-opacity", 0);
                g2.selectAll("foreignObject div").style("display", "none");
                /*added*/
                // Transition to the new view.
                t1.selectAll("text").call(text).style("fill-opacity", 0);
                t2.selectAll("text").call(text).style("fill-opacity", 1);
                t1.selectAll("rect").call(rect);
                t2.selectAll("rect").call(rect);
                /* Foreign object */
                t1.selectAll(".textdiv").style("display", "none");
                /* added */
                t1.selectAll(".foreignobj").call(foreign);
                /* added */
                t2.selectAll(".textdiv").style("display", "block");
                /* added */
                t2.selectAll(".foreignobj").call(foreign);
                /* added */
                // Remove the old node when the transition is finished.
                t1.on("end.remove", function(){
                    this.remove();
                    transitioning = false;
                });
            }
            return g;
        }
        // set positioning of the text
        function text(text) {
            text.attr("x", function (d) {
                return x(d.x) + 6;
            })
                .attr("y", function (d) {
                    return y(d.y) + 6;
                });
        }
        // set the positioning and dimensions of the rectangles
        function rect(rect) {
            rect
                .attr("x", function (d) {
                    return x(d.x0);
                })
                .attr("y", function (d) {
                    return y(d.y0);
                })
                .attr("width", function (d) {
                    return x(d.x1) - x(d.x0);
                })
                .attr("height", function (d) {
                    return y(d.y1) - y(d.y0);
                })
                .attr("fill", function (d) {
                    return '#bbbbbb';
                });
        }
        // set the positioning and dimension of the foreign objects
        function foreign(foreign) { /* added */
            foreign
                .attr("x", function (d) {
                    return x(d.x0);
                })
                .attr("y", function (d) {
                    return y(d.y0);
                })
                .attr("width", function (d) {
                    return x(d.x1) - x(d.x0);
                })
                .attr("height", function (d) {
                    return y(d.y1) - y(d.y0);
                });
        }
        // set the text to be displayed by the root -- does not get removed during transitioning 
        function name(d) {
            return breadcrumbs(d) +
                (d.parent
                ? " -  Click to zoom out"
                : " - Click inside square to zoom in");
        }
        // extract the root name. In our case it is "Narrow Memory"
        function breadcrumbs(d) {
            var res = "";
            var sep = " > ";
            d.ancestors().reverse().forEach(function(i){
                res += i.data.name + sep;
            });
            return res
                .split(sep)
                .filter(function(i){
                    return i!== "";
                })
                .join(sep);
        }
    });
}