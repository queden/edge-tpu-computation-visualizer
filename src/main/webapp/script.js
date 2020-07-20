// Updates the time zone to be used throughout the website
async function submitTimeZoneIndex() {
  var timeTest = new Intl.DateTimeFormat();
  const locale = timeTest.resolvedOptions().locale;
  const options = { year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric'};
  const date = Date.now();
  timeTest = new Intl.DateTimeFormat(locale, options);
  let currentTime = timeTest.format(date);
  console.log(currentTime);



  // Retrieves the selected time zone
  // const select = document.getElementById("time-zone");
  // const zone = select.options[select.selectedIndex].value;

  /*
    /visualizer -> sends information to the report servlet
    time=true -> DOES update the time zone
    zone=zone -> sends the selected time zone information
  */
  await fetch('/visualizer?time=true&zone=' + currentTime, {method: 'GET'});
}

async function selectUserIndex() {
  const newUser = document.getElementById("new-user");

  if (newUser.value != "") {
    /*
      /visualizer -> sends information to the visualizer servlet
      time=false -> does NOT update the time zone
      process=loadfiles -> populates the drop down menu of the appropriate files
      user=true -> DOES update the current user
      new=true -> DOES create a new user
      user-name=newUser.value-> sends the name of the selected user
    */
    await fetch('/visualizer?time=false&user=true&new=true&user-name=' + newUser.value, {method: 'GET'});
  } else {
    const select = document.getElementById("users");
    const user = select.options[select.selectedIndex].text;

    /*
      /visualizer -> sends information to the visualizer servlet
      time=false -> does NOT update the time zone
      process=loadfiles -> populates the drop down menu of the appropriate files
      user=true -> DOES update the current user
      new=false -> does NOT create a new user
      user-name=user -> sends the name of the selected user
    */
    await fetch('/visualizer?time=false&user=true&new=false&user-name=' + user, {method: 'GET'});
  }
}

// Handles the upload of the selected file
async function uploadFile() {
  /*
    /visualizer -> sends information to the visualizer servlet
    time=false -> does NOT update the time zone
    user=false -> does NOT update the current user
  */
  const call = await fetch('/visualizer?time=false&user=false', {method: 'GET'});
  const response = await call.json();

  const box = document.getElementById("uploaded-file");
  box.innerHTML = '';

  if (response.fileName != "null") {
    // Adds the uploaded file information to be displayed
    addFileInfo(response);
  } else {
    // Alerts the user if they failed to select a file after clicking "upload"
    const p = document.createElement("p");

    p.innerHTML = "Please select a file.";
    p.style.color = "red";

    // Erases scroll bar message
    const scroll = document.getElementById("scroll");
    scroll.innerHTML = '';

    box.appendChild(p);
  }

  // Populates the drop down menu of all known users
  addUsers(response.users);

  // Adds the correct time zone information to the page
  const timeZoneBox = document.getElementById("time-zone-display");
  timeZoneBox.innerHTML = "Time Zone: " + response.zone;

  // Adds the current user information to the page
  const userNameBox = document.getElementById("user-name");
  userNameBox.innerHTML = "User: " + response.currentUser;
  userNameBox.title = response.currentUser;
}

// Adds the uploaded file information to be displayed
function addFileInfo(response) {
  const box = document.getElementById("uploaded-file");

  // File name and time
  var p = document.createElement("p");
  p.innerHTML = response.fileName + " at " + response.time;
  p.style.fontWeight = "bold";

  box.appendChild(p);

  // User who uploaded the file
  var p = document.createElement("p");
  p.innerHTML = "Uploaded by: " + response.uploadUser;
  p.style.fontWeight = "bold";

  box.appendChild(p);

  // Appropriate size of the file
  p = document.createElement("p");
  p.innerHTML = "File size: " + response.fileSize;

  box.appendChild(p);

  // Name of the simulation trace in the uploaded file
  p = document.createElement("p");
  p.innerHTML = "Simulation trace name: " + response.fileTrace;

  box.appendChild(p);

  // Number of tiles in the simulation trace
  p = document.createElement("p");
  p.innerHTML = "Number of tiles: " + response.fileTiles;

  box.appendChild(p);

  // Size of the narrow memory
  p = document.createElement("p");
  p.innerHTML = "Narrow memory size: " + response.narrowBytes + " Bytes";

  box.appendChild(p);

  // Size of the wide memory
  p = document.createElement("p");
  p.innerHTML = "Wide memory size: " + response.wideBytes + " Bytes";

  box.appendChild(p);

  // Alerts the user to the presence of a scroll bar should they choose to use it
  const scroll = document.getElementById("scroll");
  scroll.innerHTML = "*Scroll for more information";
}

// Populates the drop down menu of all known users
function addUsers(users) {
  const select = document.getElementById("users");
  select.innerHTML = '';

  // Creates the default "All" users option
  const selectOption = document.createElement("option");
  selectOption.text = "All";
  select.appendChild(selectOption);

  // Appends each user into the user drop down menu
  users.forEach((user) => {
    select.appendChild(createUser(user));
  });
}

// Updates the time zone
async function submitTimeZoneReport() {
  // Retrieves the selected time zone
  const select = document.getElementById("time-zone");
  const zone = select.options[select.selectedIndex].value;

  /*
    /report -> sends information to the report servlet
    time=true -> DOES update the time zone
    zone=zone -> sends the selected time zone information
  */
  await fetch('/report?time=true&zone=' + zone, {method: 'GET'});
}

// Updates current user
async function selectUserReport() {
  // Retrieves selected user
  const select = document.getElementById("users");
  const user = select.options[select.selectedIndex].text;

  /*
    /report -> sends information to the report servlet
    time=false -> does NOT update the time zone
    process=loadfiles -> populates the drop down menu of the appropriate files
    user=true -> DOES update the current user
    user-name=user -> sends the name of the selected user
  */
  await fetch('/report?time=false&process=loadfiles&user=true&user-name=' + user, {method: 'GET'});
}

// Shows the user which file they have currently selected
function displayFile() {
  // Retrieves the selected file
  const select = document.getElementById("uploaded-files");
  const file = select.options[select.selectedIndex].text;

  const selectedFileBox = document.getElementById("selected-file");
  selectedFileBox.innerHTML = '';
  selectedFileBox.innerHTML = "Selected file: " + file;
}

// Populates the select file list
async function loadFiles() {
  // Gets the current user
  const userNameBox = document.getElementById("user-name");
  const user = userNameBox.title;

  // var timeTest = new Intl.DateTimeFormat();
  // const locale = timeTest.resolvedOptions().locale;
  // const options = { year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric'};
  // const date = Date.now();
  // timeTest = new Intl.DateTimeFormat(locale, options);
  // let currentTime = timeTest.format(date);
  // console.log(currentTime);

  /*
    /report -> sends information to the report servlet
    time=false -> does NOT update the time zone
    process=loadfiles -> populates the drop down menu of the appropriate files
    user=false -> does NOT update the current user
    user-name=user -> sends the name of the current user
  */
  const call = await fetch('/report?time=false&process=loadfiles&user=false&user-name=' + user, {method: 'GET'});
  const files = await call.json();

  // Adds the current time zone information to the page
  const timeZoneBox = document.getElementById("time-zone-display");
  timeZoneBox.innerHTML = "Time Zone: " + files[0].zone;

  // Adds the current user information to the page
  userNameBox.innerHTML = "User: " + files[0].user;
  userNameBox.title = files[0].user;

  // Shows the user who's files they are currently viewing
  const displayUserFilesBox = document.getElementById("display-user-files");
  displayUserFilesBox.innerHTML = '';
  displayUserFilesBox.innerHTML = "Displaying files for: " + files[0].user;

  // Checks if the current user has uploaded files under their name
  const userFiles = files[0].userFilesExist;
  const userContainer = document.getElementById("user-files");

  if (userFiles == false) {
    // Displays "error" message if the user has not uploaded files under their name
    userContainer.style.display = "block";
  } else {
    // Hides "error" message
    userContainer.style.display = "none";
  }

  // Populates the drop down menu of all known users
  addUsers(files[0].users);

  const select = document.getElementById("uploaded-files");
  select.innerHTML = ''; 

  const option = document.createElement("option");
  option.text = "No file chosen";
  select.appendChild(option);

  // Appends each file into the file drop down menu
  files.forEach((file) => {
    select.appendChild(createFile(file));
  });
}

// Creates file options out of previously uploaded files to append to the select list
function createFile(file) {
  const selectOption = document.createElement("option");
  selectOption.value = file.id;
  selectOption.text = file.name + " at " + file.time;
  return selectOption;
}

// Creates user options out of all known users
function createUser(user) {
  const selectOption = document.createElement("option");
  selectOption.value = user.id;
  selectOption.text = user.userName;
  return selectOption;
}

// Runs the visualization of the chosen simulation trace
async function runVisualization() {
  // Retrieves the selected file
  const select = document.getElementById("uploaded-files");
  const file = select.options[select.selectedIndex];
  const fileText = file.text;

  if (file.text == "No file chosen") {
    alert("You must choose a file");
  } else {
    /*
      /report -> sends to report servlet
      process=pre -> performs preprocessing of the proto information
    */
    const preprocess = await fetch('/report?process=pre&time=false&fileId=' + file.value, {method: 'GET'});
    const preprocessResponse = await preprocess.json();

    // Shows the user which file they are viewing the visualization of  

    const selectedFileBox = document.getElementById("selected-file");
    selectedFileBox.innerHTML = '';
    selectedFileBox.innerHTML = "Selected file: " + fileText;

    // Process initial json information
    // TODO: Substitute
    const box = document.getElementById("test-box");
    box.innerHTML = '';
    const init = document.createElement("p");
    init.innerHTML = preprocessResponse.message;
    box.appendChild(init);

    for (i = 0; i < prepprocessResponse.numTraces; i += 1000) {
      // Run through the traces, information processing will happen within the function
      await runTraces(i);
    }
  }
}

/*
  Processes the different chunks of specified trace indicies
  start -> the beginning index of the traces to be processed
*/
async function runTraces(start) {
  // Retrieves box to display error/processing information
  const box = document.getElementById("test-box");

  /*
    /report -> sends information to report servlet
    process=post -> runs algorithm on selected proto
  */
  const traceResponse = await fetch('/report?process=post&start=' + start, {method: 'GET'});
  const traceProcess = await traceResponse.json();

  box.appendChild(responseMessage);

  // Process json trace information
  // TODO: Substitute
  var responseMessage = document.createElement("p");

  if (traceProcess.error == null) {
    responseMessage.innerHTML += "Traces validated";
  } else {
    responseMessage.innerHTML = traceProcess.error.message;
  }

  box.appendChild(responseMessage);
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

const realKonami = ["ArrowUp", "ArrowUp", "ArrowDown", "ArrowDown", "ArrowLeft", "ArrowRight", "ArrowLeft", "ArrowRight", "KeyA", "KeyB"];
var userKonami = [];

window.onkeydown = checkKonami;

function checkKonami(e) {
  userKonami.push(e.code);
  console.log(e.code);
  console.log(userKonami);

  if (userKonami.length > 10) {
    userKonami.shift();
  }

  let match = true;

  for (i = 0; i < 10; i += 1) {
    if (userKonami[i] != realKonami[i]) {
      match = false;
      break;
    }
  }
  console.log(match);

  if (match) {
    location.replace("/rec.html");
    alert("ENTER CURIOUS TRAVELER");
  }
}