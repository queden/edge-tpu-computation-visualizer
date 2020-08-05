// Updates the time zone to be used throughout the website.
async function submitTimeZone() {
  // Retrieves the selected time zone.
  const select = document.getElementById("time-zone");
  const zone = select.options[select.selectedIndex].value;

  /*
    /visualizer -> sends information to the report servlet
    time=true -> DOES update the time zone
    zone=zone -> sends the selected time zone information
  */
  await fetch('/visualizer?time=true&zone=' + zone, {method: 'GET'});
}

async function selectUser() {
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

// Handles the upload of the selected file as well as the loading of the page.
async function loadMainPage() {
  /*
    /visualizer -> sends information to the visualizer servlet
    time=false -> does NOT update the time zone
    user=false -> does NOT update the current user
  */
  const call = await fetch('/visualizer?time=false&user=false', {method: 'GET'});
  const response = await call.json();

  const uploadFile = response.uploadFile;
  const files = response.files;
  const users = response.users;
  const currentUser = response.currentUser;
  const zone = response.zone;
  const errorMessage = response.errorMessage;

  // Gives feedback to the user if they were attempting to add a duplicate user
  // (in which case the duplicate addition would be prevented).
  if (errorMessage != '') {
    alert("User already exists");
  }

  const box = document.getElementById("uploaded-file");
  box.innerHTML = '';

  if (uploadFile.fileName != "null") {
    // Adds the uploaded file information to be displayed.
    addFileInfo(uploadFile);
  } else {
    // Alerts the user if they failed to select a file after clicking "upload".
    const p = document.createElement("p");

    p.innerHTML = "Please select a file.";
    p.style.color = "red";

    box.appendChild(p);
  }

  // Adds the correct time zone information to the page.
  const timeZoneBox = document.getElementById("time-zone-display");
  timeZoneBox.innerHTML = "Time Zone: " + zone;

  // Adds the current user information to the page.
  const userNameBox = document.getElementById("user-name");
  userNameBox.innerHTML = "User: " + currentUser;
  userNameBox.title = currentUser;

  // Shows the user who's files they are currently viewing.
  const displayUserFilesBox = document.getElementById("display-user-files");
  displayUserFilesBox.innerHTML = '';
  displayUserFilesBox.innerHTML = "Displaying files for: " + currentUser;

  if (files.length > 0) {
    // Checks if the current user has uploaded files under their name.
    const userFiles = files[0].userFilesExist;
    const userContainer = document.getElementById("user-files");

    if (userFiles == false) {
      // Displays "error" message if the user has not uploaded files under their name.
      userContainer.style.display = "block";
    } else {
      // Hides "error" message.
      userContainer.style.display = "none";
    }
  }

  // Populates the drop down menu of all known users.
  addUsers(users);

  const select = document.getElementById("uploaded-files");
  select.innerHTML = ''; 

  const option = document.createElement("option");
  option.text = "No file chosen";
  select.appendChild(option);

  // Appends each file into the file drop down menu.
  files.forEach((file) => {
    select.appendChild(createFile(file));
  });
}

// Adds the uploaded file information to be displayed.
function addFileInfo(response) {
  const box = document.getElementById("uploaded-file");

  // File name and time.
  var p = document.createElement("p");
  p.innerHTML = response.fileName + " at " + response.time;
  p.style.fontWeight = "bold";

  box.appendChild(p);

  // User who uploaded the file.
  var p = document.createElement("p");
  p.innerHTML = "Uploaded by: " + response.uploadUser;
  p.style.fontWeight = "bold";

  box.appendChild(p);

  // Appropriate size of the file
  p = document.createElement("p");
  p.innerHTML = "File size: " + response.fileSize;

  box.appendChild(p);

  // Name of the simulation trace in the uploaded file.
  p = document.createElement("p");
  p.innerHTML = "Simulation trace name: " + response.fileTrace;

  box.appendChild(p);

  // Number of tiles in the memaccess checker.
  p = document.createElement("p");
  p.innerHTML = "Number of tiles: " + response.fileTiles;

  box.appendChild(p);

  // Size of the narrow memory.
  p = document.createElement("p");
  p.innerHTML = "Narrow memory size: " + response.narrowBytes + " Bytes";

  box.appendChild(p);

  // Size of the wide memory.
  p = document.createElement("p");
  p.innerHTML = "Wide memory size: " + response.wideBytes + " Bytes";

  box.appendChild(p);
}

// Populates the drop down menu of all known users.
function addUsers(users) {
  const select = document.getElementById("users");
  select.innerHTML = '';

  // Creates the default "All" users option.
  const selectOption = document.createElement("option");
  selectOption.text = "All";
  select.appendChild(selectOption);

  // Appends each user into the user drop down menu.
  users.forEach((user) => {
    select.appendChild(createUser(user));
  });
}

// Creates file options out of previously uploaded files to append to the select list.
function createFile(file) {
  const selectOption = document.createElement("option");
  selectOption.value = file.id;
  selectOption.text = file.name + " at " + file.time;
  return selectOption;
}

// Creates user options out of all known users.
function createUser(user) {
  const selectOption = document.createElement("option");
  selectOption.value = user.id;
  selectOption.text = user.userName;
  return selectOption;
}

// Shows the user which file they have currently selected.
function displayFile() {
  // Retrieves the selected file.
  const select = document.getElementById("uploaded-files");
  const file = select.options[select.selectedIndex].text;

  const selectedFileBox = document.getElementById("selected-file");
  selectedFileBox.innerHTML = '';
  selectedFileBox.innerHTML = "Selected file: " + file;

  // Checks if the user has selected a file and changes feedback message accordingly.
  if (file == "No file chosen") {
    selectedFileBox.style.color = "red";
  } else {
    selectedFileBox.innerHTML = "Selected file: " + file;
    selectedFileBox.style.color = "limegreen";
  } 
}

// Deletes a single file.
async function deleteFile() {
  // Retrieves the selected file.
  const select = document.getElementById("uploaded-files");
  const file = select.options[select.selectedIndex];

  // Prevents the user from attempting to delete a non-selected file.
  if (file.text == "No file chosen") {
    alert("You must choose a file to delete.");
  } else {
    // Confirms if the user wants to delete the selected file.
    var purge = confirm("You are about to delete file: " + file.text + ". Do you wish to continue?");

    if (purge == true) {
      /*
        /visualizer -> sends the information to the visualizer servlet
        upload=false -> user is NOT uploading a file
        purge=false -> user is NOT doing a blanket delete operation
        user=false -> user is NOT deleting a user
        file-id=file.value -> the id of the file's entity in datastore
      */
      await fetch('/visualizer?upload=false&purge=false&user=false&file-id=' + file.value, {method: 'POST'});
      await loadMainPage();

      alert("File deleted");
    } else {
      alert("Delete aborted");
    }
  }
}

// Deletes a single user.
async function deleteUser() {
  // Retrieves the selected user.
  const select = document.getElementById("users");
  const user = select.options[select.selectedIndex];

  // Prevents the user from attempting to delete the default option.
  if (user.text == "All") {
    alert("Cannot delete user \"All\"");
  } else {
    // Confirms if the user wants to delete the selected user.
    var purge = confirm("You are about to delete user \"" + user.text + "\". Do you wish to continue?");

    if (purge == true) {
      /*
        /visualizer -> sends the information to the visualizer servlet
        upload=false -> user is NOT uploading a file
        purge=user -> user is NOT doing a blanket delete operation
        user=true -> user IS deleting a user
        user-id=user.value -> the selected user's key id in datastore
        user-name=user.text -> the name of the selected user
      */
      await fetch('/visualizer?upload=false&purge=false&user=true&user-id=' + user.value + "&user-name=" + user.text, {method: 'POST'});
      await loadMainPage();

      alert("User deleted");
    } else {
      alert("Delete aborted");
    }
  }
}

// Opens a pop-up window containing the visualization page.
function openVisualization() {
  // Retrieves the selected file.
  const select = document.getElementById("uploaded-files");
  const file = select.options[select.selectedIndex];

  if (file.text == "No file chosen") {
    // Prevents the user from trying to access the visualizer without selecting a file.
    alert("You must choose a file");
  } else {
    // Creates and opens the pop-up window.
    const visualizerWindow = window.open("report.html", "Visualizer", "_blank", "toolbar=yes,scrollbars=yes,width=2200,height=10000,resizable=yes");
    // visualizerWindow.focus();

    // Listener to retrieve information from the main page.
    visualizerWindow.addEventListener("message", function(message) {
      // Determines if the sent data is the file's id or information.
      if (!(isNaN(parseInt(message.data)))) {
        // Stores the selected file's id.

        const fileIdBox = visualizerWindow.document.getElementById("trace-info-box");
        fileIdBox.title = message.data;
      } else {
        // Displays the selected file's information.

        const fileInfoBox = visualizerWindow.document.getElementById("file-info");
        fileInfoBox.innerHTML += message.data;

        const tiles = visualizerWindow.document.getElementById("tile-select");
        tiles.innerHTML = '';

        const option = document.createElement("option");
        option.value = 0;
        option.text = "Tile 0";

        tiles.appendChild(option);
      }
    });

    // Checks and executes the specified functions after the pop-up has finished loading.
    visualizerWindow.onload = function() {
      // Lets the user know the window has completed its loading.
      visualizerWindow.alert("Visualizer loaded.");

      // Passes the file id and the file information to the pop-up window.

      visualizerWindow.postMessage(file.value, "*"); 
      visualizerWindow.postMessage(file.text, "*");
    };
  } 
}

// Variable to dictate whether the visualization should finish
var endVisualization = false;

// Variable to hold how the visualization should continue:
// -"a" to continue past all errors
// -"d" to continue as is with prompts after each error
// -"s" to abort the visualization
var proceed = "";

var tile = 0;
var layerName = "";
var curLocation = "";

// Runs the visualization of the chosen simulation trace.
async function runVisualization() {
  alert("Visualization begun");

  endVisualization = false;

  const done = document.getElementById("done");
  done.style.display = "none";

  const traceBox = document.getElementById("trace-info-box");
  traceBox.innerHTML = '';

  const errorBox = document.getElementById("error-box");
  errorBox.style.display = "none";

  const errorMessages = document.getElementById("error-messages");
  errorMessages.innerHTML = '';

  // Makes the information about what the user is viewing on the visualizer
  const viewInfoBox = document.getElementsByClassName("selection");
  
  for (i = 0; i < viewInfoBox.length; i++) {
    viewInfoBox[i].style.display = "block";
  }

  const stepSize = parseInt(document.getElementById("step-size").value);

  /*
    /report -> sends to report servlet
    process=pre -> performs preprocessing of the proto information
    fileId=traceBox.title -> the id of the file to retrieve from datastore
  */
  const preprocess = await fetch('/report?process=pre&fileId=' + traceBox.title, {method: 'GET'});
  const preprocessResponse = await preprocess.json();

  // Process initial json information.

  // Add the number of tile options to switch to.
  addTiles(preprocessResponse.numTiles);

  // Update visualizer.
  chart(1, "pre", preprocessResponse);

  const init = document.createElement("p");
  init.innerHTML = preprocessResponse.message;
  traceBox.appendChild(init);

  var numTraces = preprocessResponse.numTraces;

  var start = 0; 

  if (!preprocessResponse.isError) {
    
    while (start < numTraces) {
      var runTracesEnd = await runTraces(start, stepSize);

      if (proceed == "s" || endVisualization == true) {
        break;
      }

      start = runTracesEnd + 1;
    }
  } else {
    alert("Error occurred in preprocessing, visualization aborted.");
  }
  
  alert("Visualization completed.");

  proceed = "";
  done.style.display = "block";
}

/*
  Processes the different chunks of specified trace indicies.
  
  start -> the beginning index of the traces to be processed
  numTraces -> the total number of traces
*/
async function runTraces(start, stepSize) {
  // Retrieves box to display error/processing information.
  const traceBox = document.getElementById("trace-info-box");

  /*
    /report -> sends information to report servlet
    process=post -> runs trace validation algorithm on selected proto
    start=start -> the index of the traces to start processing
  */
  const traceResponse = await fetch('/report?process=post&start=' + start + "&step-size=" + stepSize, {method: 'GET'});
  const traceProcess = await traceResponse.json();
  var end = traceProcess.validationEnd;

  // Process json trace information.
  if (!traceProcess.isError) {
    var responseMessage = document.createElement("p");

    responseMessage.innerHTML += `Traces ${start}-${end} validated.`;
    traceBox.appendChild(responseMessage);

    // Update visualizer
    chart(1, "post", traceProcess);

    // Continues visualization.
    return end;
  } else {
    document.getElementById("error-box").style.display = "block";

    // Appends error information.

    const errorMessages = document.getElementById("error-messages");
    errorMessages.style.display = "block";

    const tracesError = document.createElement("p");
    tracesError.className = "trace-error-interval";
    tracesError.innerHTML = "Traces " + start + "-" + end;
    errorMessages.appendChild(tracesError);

    const p = document.createElement("p");
    p.innerHTML = traceProcess.message;
    errorMessages.appendChild(p);

    // Checks if the user wants to continue or abort the visualization after an error is found.
    if (proceed != "a") {
      var promptString = 
          "An error was encountered. Please choose how to continue:" +
          "\n\"a\": continue through all errors with no prompts" +
          "\n\"s\": abort visualization" +
          "\n\"d\": continue with prompts" +
          "\n\n*Default is \"d\" if invalid/no selection made";

      proceed = prompt(promptString, "d");

      if (!(proceed != "d" || proceed != "a" || proceed != "s")) {
        proceed = "d";
      } else if (proceed == "a") {
        alert("Press \"q\" at any time to end the visualization.");
      }
    }

    if (proceed != "s") {
      // Continue visualization.

      // Update visualizer
      chart(1, "post", traceProcess);

      return end;
    } else {
      // Abort visualization.
      return end;
    }
  }
}

// Listener to terminate the visualization
window.onkeyup = checkTerminate;

function checkTerminate(key) {
  if (key.code == "KeyQ") {
    endVisualization = true;
  }
}

// Deletes the specified elements from datastore.
async function purgeAll(users, files) {
  var message = "";

  if (users == true && files == false) {
    message = "all users";
  } else if (users == false && files == true) {
    message = "all files";
  } else {
    message = "all users and files";
  }

  // Double checks if the user actually wants to delete elements from datastore.
  var purge = confirm("You are about to delete " + message + ". Do you wish to continue?");

  if (purge == true) {
    /*
      /visualizer -> sends information to visualizer servlet
      upload=false -> does NOT change the last uploaded file information
      purge=true -> DOES delete the specified type of elements from datastore
      users=users -> does/does not delete all users
      files=files -> does/does not delete all files
    */
    await fetch('/visualizer?upload=false&purge=true&users=' + users + '&files=' + files, {method: 'POST'});
    await loadMainPage();
  } else {
    alert("Purge aborted");
  }
}

// Adds the number of tiles in the selected file to the tile dropdown menu
function addTiles(numTiles) {
  for (var i = 1; i <= numTiles; i += 1) {
    createTile(i);
  }
}

// Creates a tile option and its specified number
function createTile(numTile) {
  const tiles = document.getElementById("tile-select");
  const option = document.createElement("option");

  option.value = numTile;
  option.text = "Tile " + numTile;

  tiles.appendChild(option);
}

/**
 * chart() hosts the general setup of the visualizer
 */
var preResult;
var postResult;
var layerBox = document.getElementById("layer-name");
var locationBox = document.getElementById("location");
var tileBox = document.getElementById("viewing-box");
var memoryBox = document.getElementById("memory");

function chart(memory) {
  chart(memory, "post", postResult);
}

async function chart(val, process, json) {
  var narrow = "Narrow";
  var wide = "Wide";

  if (process == "pre") {
    preResult = json;
  } else {
    postResult = json;
    console.log(postResult["narrowDeltas"]);

    var narrowDelta = postResult["narrowDeltas"];
    var narrowSize = preResult["narrowSize"];
    var wideDelta = postResult["wideDeltas"];
    var wideSize = preResult["wideSize"];
    var narrowAlloc = preResult['tensorAllocationNarrow'];
    var wideAlloc = preResult['tensorAllocationWide'];

    var data1 = new Array();
    var layers = new Set();
    var longestLayerName = 0;

    /**depending on which of the memory types are selected
    fill the array*/
    function fill(memoryAlloc, memorySize) {
      for (var i = 0; i < memoryAlloc.length; i++) {
        var allocs = memoryAlloc[i]["tensorTileAllocation_"][0]["tensorAllocation_"];
        var tileAllocs = memoryAlloc[i]["tensorTileAllocation_"];

        for (var tile = 0; tile < tileAllocs.length; tile++) {
          allocs = tileAllocs[tile]["tensorAllocation_"];

          for (var j = 0; j < allocs.length; j++) {
            var alloc = allocs[j];
            var start = 0;
            var end = 0;
            var size;
            start = alloc["baseAddress_"];

            if (memoryAlloc === wideAlloc){
              size = alloc["size_"]/32;
            } else {
              size = alloc["size_"];
            }

            end = start + size;

            for (var k = start; k < end; k++) {
              if (end > memorySize) {
                //Display the Error message
                const errorMessage = document.getElementById("error-report");
                errorMessage.innerHTML = "Allocation with label " + alloc["tensorLabel_"] + " has invalid memory address of " + end + ".";
                break;
              }

              var datum = {}
              datum.location = k;
              datum.layer = memoryAlloc[i]["layer_"];
              datum.tile = tile;
              datum.filled = false;
              datum.label = alloc["tensorLabel_"];
              data1.push(datum);

              layers.add(memoryAlloc[i]["layer_"]);

              if (longestLayerName < memoryAlloc[i]["layer_"].length){
                longestLayerName = memoryAlloc[i]["layer_"].length
              }
            }
          }
        }
      }
    }

    console.log(layers);
    console.log(longestLayerName);

    /**add changes made by the deltas
    */
    function addDelta(data1, deltas) {
      for (var i = 0; i < deltas.length; i++) {
        var delta = deltas[i];
        console.log(deltas[i])
                
        for (var j = 0; j < data1.length; j++) {
          var entry = data1[j];
          
          if (entry.location === delta.memoryAddress && entry.tile == delta.tile && entry.location == delta.memoryAddress && entry.label == delta.tensor) {
            entry.filled = true;
          }
        }
      }
    }

    if (val == 1) {
      fill(wideAlloc, wideSize);

      if (wideDelta != undefined) {
        addDelta(data1, wideDelta)
      }
    } else {
      fill(narrowAlloc, narrowSize);

      if (narrowDelta != undefined){
        addDelta(data1, narrowDelta)
      }
    }

    /**filter the data based on the tile selected 
    */
    function filterJSON(json, key, value) {
      var result = [];
      json.forEach(function(val, idx, arr) {
        if (val[key] == value) {
          result.push(val)
        }
      });

      return result;
    }

    /**Get the data for the specific tile
    */
    function extractData(rawData, memoryType) {
      var data;
      d3.select('#tile-select')
      .on("change", function() {
        var sect = document.getElementById("tile-select");
        var section = sect.options[sect.selectedIndex].value;
        data = filterJSON(rawData, 'tile', section);
        var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location))
        displayChart(sortedData, memoryType, section);
      });

      // generate initial graph
      data = filterJSON(rawData, 'tile', '0');
      var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location))
      displayChart(sortedData, memoryType, '0');
    }

    //Set up the chart
    var obj = document.getElementById('chart');
    var divWidth = obj.offsetWidth;
    var margin = {
        top: 10,
        right: 10,
        bottom: 100,
        left: 10 + 2*longestLayerName
    },
    margin2 = {
        top: 430,
        right: 10,
        bottom: 20,
        left: 10 + 2*longestLayerName
    },
    width = divWidth - 25,
    height = 500 - margin.top - margin.bottom,
    height2 = 500 - margin2.top - margin2.bottom;

    var x = d3.scale.ordinal().rangeBands([0, width], 0),
        x2 = d3.scale.ordinal().rangeBands([0, width], 0),
        y = d3.scale.ordinal().rangeRoundBands([0, height], 0),
        y2 = d3.scale.linear().domain([narrowSize, 0]).range([height2, 0]);

    d3.select("svg").remove();

    var svg = d3.select("#chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom);

    // var tooltip = d3.select("#tooltip").append("div").attr("class", "toolTip");

    var focus = svg.append("g")
        .attr("class", "focus")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        
    var context = svg.append("g")
        .attr("class", "context")
        .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");

    var xAxis = d3.svg.axis().scale(x).orient("bottom"),
        xAxis2 = d3.svg.axis().scale(x2).orient("bottom").tickValues([]),
        yAxis = d3.svg.axis().scale(y).orient("left")

    //Draw the chart
    function displayChart(data, memoryType, section) {          
      //Display the memory type
      // const displayMemoryType = document.getElementById("memory-type");
      // displayMemoryType.innerHTML = memoryType;

      memoryBox.innerHTML = "Memory: " + memoryType;

      //Display tile 
      // const displayTile = document.getElementById("tile");
      // displayTile.innerHTML = "Tile " + section;
      tileBox.innerHTML = "Viewing information for Tile: " + section;

      //Define color scales
      colorScale = d3.scale.ordinal().domain([0, d3.max(data, function(d) {
        return d.label
      })]).range(['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080',
          '#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075'
      ]);
      grayColorScale = d3.scale.ordinal().domain([0, d3.max(data, function(d) {
        return d.label
      })]).range(['#DCDCDC', '#D3D3D3', '#C0C0C0', '#BEBEBE', '#989898', '#808080', '#696969', '#555555', '#E5E4E2',
          '#727472', '#928E85', '#708090', '#A9A9A9', '#acacac'
      ]);
      layerPosition = d3.scale.ordinal().domain(d3.map(data, function(d) {
        return d.layer
      })).range([22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]);

      // remove predrawn structures
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
      x2.domain(data.map(function(d) {
        return d.location
      }));

      //draw axis
      focus.append("g")
          .attr("class", "x axis")
          .attr("transform", "translate(0," + height + ")")
          .call(xAxis);

      focus.append("g")
          .attr("class", "y axis")
          .call(yAxis);

      var maxw = 0;

      focus.selectAll("text").each(function() {
        if(this.getBBox().width > maxw) maxw = this.getBBox().width;
      });

      // svg.attr("transform", "translate(" + maxw + ",0)");
      // console.log(maxw)
      //add brush
      var brush = d3.svg.brush()
          .x(x2)
          .on("brush", brushed);

      enter(data, focus);
      updateScale(data);

      // draw the subbars
      var subBars = context.selectAll('.subBar')
          .data(data);
      subBars.enter().append("rect")
          .classed('subBar', true)
          .attr({
              height: function(d) {
                return 10
              },
              width: function(d) {
                return x.rangeBand()
              },
              x: function(d) {
                return x2(d.location)
              },
              y: function(d) {
                return 10
              }
          });
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
    
      /**function to update the chart based on the 
        * portion of the graph zoomed into 
        */
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
          end = data.length;
        }

        var updatedData = new Array();

        for (var i = 0; i < data.length; i++) {
          if (data[i].location <= end && data[i].location >= start) {
            updatedData.push(data[i]);
          }
        }

        update(updatedData);
        enter(updatedData, focus);
        exit(updatedData);
        updateScale(updatedData);
      }

      /** Update scale based on number of 
        * data values
        */
      function updateScale(data) {
        var tickScale = d3.scale.pow().range([data.length / 2, 0]).domain([data.length, 0]).exponent(.5);
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
      }

      /** Build the bars based on  
        * updated data values
        */
      function update(data) {
        x.domain(data.map(function(d) {
          return d.location
        }));

        y.domain(data.map(function(d) {
          return d.layer
        }));

        var focusHeight = focus.node().getBoundingClientRect().height;
        var size = layers.size;

        if (size === 0) {
          size = 1;
        }

        var newHeight = focusHeight / size;

        var bars = focus.selectAll('.bar')
          .data(data);
        bars
            .attr({
              height: function(d, i) {
                return newHeight;
              },
              width: function(d) {
                return x.rangeBand();
              },
              x: function(d) {
                return x(d.location);
              },
              y: function(d) {
                return 9 + y(d.layer);
              },
              fill: function(d) {
                if (d.filled) {
                  return colorScale(d.label);
                  }

                return grayColorScale(d.label);
              },
              stroke: function(d) {
                if (d.filled) {
                  return colorScale(d.label);
                }

                return grayColorScale(d.label);
              }
            })
            .on("mousemove", function(d) {
              // tooltip
              //     .style("left", d3.event.pageX - 50 + "px")
              //     .style("top", d3.event.pageY - 70 + "px")
              //     .style("display", "inline-block");
              //     .html((d.layer) + "<br>" + (d.location));

              layerBox.innerHTML = "Layer: " + d.layer;
              locationBox.innerHTML = "Location: " + d.location;

            })
            .on("mouseout", function(d) {
                // tooltip.style("display", "none");

              layerBox.innerHTML = "Layer: ";
              locationBox.innerHTML = "Location: ";
            });
      }

      /** remove data values  
      */
      function exit(data) {
        var bars = focus.selectAll('.bar').data(data);
        bars.exit().remove();
      }


      /** Build the bars based on  
        * initial data values
        */
      function enter(data, focus) {    
        x.domain(data.map(function(d) {
          return d.location;
        }));
                
        y.domain(data.map(function(d) {
          return d.layer;
        }));
                
        var focusHeight = focus.node().getBoundingClientRect().height;
        var size = layers.size;

        if (size === 0){
          size = 1;
        }

        var newHeight = focusHeight / size;

        var bars = focus.selectAll('.bar')
            .data(data)
        bars.enter().append("rect")
            .style("stroke-linejoin", "round")
            .classed('bar', true)
            .attr({
              height: function(d, i) {
                return newHeight;
              },
              width: function(d) {
                return x.rangeBand()
              },
              x: function(d) {
                return x(d.location);
              },
              y: function(d) {
                return 9 + y(d.layer)
              },
              fill: function(d) {
                if (d.filled) {
                  return colorScale(d.label);
                }

                return grayColorScale(d.label);
              },
              stroke: function(d) {
                if (d.filled) {
                  return colorScale(d.label);
                }

                return grayColorScale(d.label);
              }
            })
            .on("mousemove", function(d) {
              // tooltip
              //     .style("left", d3.event.pageX - 50 + "px")
              //     .style("top", d3.event.pageY - 70 + "px")
              //     .style("display", "inline-block")
              //     .html((d.layer) + "<br>" + (d.location));
              layerBox.innerHTML = "Layer: " + d.layer;
              locationBox.innerHTML = "Location: " + d.location;
            })
            .on("mouseout", function(d) {
                // tooltip.style("display", "none");

              layerBox.innerHTML = "Layer: ";
              locationBox.innerHTML = "Location: ";
            });

      }
    }
    if (val == 2) {
          extractData(data1, narrow)
        } else {
          extractData(data1, wide)
        }
  }
}


// Cool rectangle easter egg ;D
var realKonami = ["ArrowUp", "ArrowUp", "ArrowDown", "ArrowDown", "ArrowLeft", "ArrowRight", "ArrowLeft", "ArrowRight", "KeyA", "KeyB"];
var userKonami = [];

window.onkeydown = checkKonami;

function checkKonami(e) {
  userKonami.push(e.code);

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

  if (match) {
    location.replace("/rec.html");
    alert("ENTER CURIOUS TRAVELER");
  }
}