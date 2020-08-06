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
	await fetch('/visualizer?time=true&zone=' + zone, {
		method: 'GET'
	});
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
		await fetch('/visualizer?time=false&user=true&new=true&user-name=' + newUser.value, {
			method: 'GET'
		});
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
		await fetch('/visualizer?time=false&user=true&new=false&user-name=' + user, {
			method: 'GET'
		});
	}
}

// Handles the upload of the selected file as well as the loading of the page.
async function loadMainPage() {
	/*
	  /visualizer -> sends information to the visualizer servlet
	  time=false -> does NOT update the time zone
	  user=false -> does NOT update the current user
	*/
	const call = await fetch('/visualizer?time=false&user=false', {
		method: 'GET'
	});
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
			await fetch('/visualizer?upload=false&purge=false&user=false&file-id=' + file.value, {
				method: 'POST'
			});
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
			await fetch('/visualizer?upload=false&purge=false&user=true&user-id=' + user.value + "&user-name=" + user.text, {
				method: 'POST'
			});
			await loadMainPage();

			alert("User deleted");
		} else {
			alert("Delete aborted");
		}
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
		await fetch('/visualizer?upload=false&purge=true&users=' + users + '&files=' + files, {
			method: 'POST'
		});
		await loadMainPage();
	} else {
		alert("Purge aborted");
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
		visualizerWindow.focus();

		// Listener to retrieve information from the main page.
		visualizerWindow.addEventListener("message", function (message) {
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

				// Adds the first tile 0 option, independent of how many tiles are present in the file,
				const option = document.createElement("option");
				option.value = 0;
				option.text = "Tile 0";

				tiles.appendChild(option);
			}
		});

		// Checks and executes the specified functions after the pop-up has finished loading.
		visualizerWindow.onload = function () {
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
	const preprocess = await fetch('/report?process=pre&fileId=' + traceBox.title, {
		method: 'GET'
	});
	const preprocessResponse = await preprocess.json();

	// Process initial json information.

	// Add the number of tile options to switch to.
	addTiles(preprocessResponse.numTiles);

	// Update visualizer with initial memory allocations.
	// chart(1, "pre", preprocessResponse);
	await fill(preprocessResponse);
	console.log(data1);

	const init = document.createElement("p");
	init.innerHTML = preprocessResponse.message;
	traceBox.appendChild(init);

	var numTraces = preprocessResponse.numTraces;

	var start = 0;

	if (!preprocessResponse.isError) {
		while (start < numTraces) {
			var runTracesEnd = await runTraces(start, stepSize);

			// Checks if the user has chosen to stop the visualization, either by
			// pressing "s" when prompted or pressing "q" after previously selecting "a" when prompted
			if (proceed == "s" || endVisualization == true) {
				break;
			}

			start = runTracesEnd + 1;
		}
	} else {
		alert("Error occurred in preprocessing, visualization aborted.");
	}

	proceed = "";
	done.style.display = "block";

	alert("Visualization completed.");
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
	const traceResponse = await fetch('/report?process=post&start=' + start + "&step-size=" + stepSize, {
		method: 'GET'
	});
	const traceProcess = await traceResponse.json();
	var end = traceProcess.validationEnd;

	// Process json trace information.
	if (!traceProcess.isError) {
		var responseMessage = document.createElement("p");

		responseMessage.innerHTML += `Traces ${start}-${end} validated.`;
		traceBox.appendChild(responseMessage);

		// Update visualizer
		// chart(1, "post", traceProcess);

		addDelta(traceProcess);

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
		// Will not occur if the user previously chose to run through all errors found.
		if (proceed != "a") {
			var promptString =
				"An error was encountered. Please choose how to continue:" +
				"\n\"a\": continue through all errors with no prompts" +
				"\n\"s\": abort visualization" +
				"\n\"d\": continue with prompts" +
				"\n\n*Default is \"d\" if invalid/no selection made";

			proceed = prompt(promptString, "d");

			// Sets the option chosen to "d" if the user failed to give a valid input.
			if (!(proceed != "d" || proceed != "a" || proceed != "s")) {
				proceed = "d";
			} else if (proceed == "a") {
				alert("Press \"q\" at any time to end the visualization.");
			}
		}

		if (proceed != "s") {
			// Continue visualization.

			// Update visualizer with current chunk of memory information.
			// chart(1, "post", traceProcess);
			addDelta(traceProcess);

			return end;
		} else {
			// Abort visualization.
			return end;
		}
	}
}

// Listener to terminate the visualization.
window.onkeyup = checkTerminate;

function checkTerminate(key) {
	if (key.code == "KeyQ") {
		endVisualization = true;
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

var layerBox = document.getElementById("layer-name");
var locationBox = document.getElementById("location");
var tileBox = document.getElementById("viewing-box");
var memoryBox = document.getElementById("memory");


var preResult;
var postResult;

var data1 = new Array();
var data2 = new Array();
var layersWide = new Set();
var layersNarrow = new Set();
var longestLayerName = 0;
var deltaProccessed = false;

async function chart(val, data1, hasFilled) {
	var narrow = "Narrow Memory";
	var wide = "Wide Memory";
	var layers

	// Proccess the narrow memory deltas
	if (val === 1) {
		var layersArray = [...layersWide];
		layers = layersWide
	} else {
		var layersArray = [...layersNarrow];
		layers = layersWide
		if (!deltaProccessed) {
			deltaProccess()
			deltaProccessed = true
		}
	}

	/**filter the data based on the tile selected 
	 */
	function filterJSON(json, key, value) {
		var result = [];
		json.forEach(function (val, idx, arr) {
			if (val[key] == value) {
				result.push(val);
			}
		})
		return result;
	}

	/**Get the data for the specific tile
	 */
	function extractData(rawData, memoryType) {
		var data;
		d3.select('#tile-select')
			.on("change", function () {
				var sect = document.getElementById("tile-select");
				var section = sect.options[sect.selectedIndex].value;
				data = filterJSON(rawData, 'tile', section);
				var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location));
				displayChart(sortedData, memoryType, section);
			});

		// generate initial graph
		data = filterJSON(rawData, 'tile', '0');
		var sortedData = data.slice().sort((a, b) => d3.ascending(a.location, b.location));
		displayChart(sortedData, memoryType, '0');
	}

	//Set up the chart
	var obj = document.getElementById('chart');
	var divWidth = obj.offsetWidth;
	// var divWidth = 2000;
	var margin = {
			top: 10,
			right: 10,
			bottom: 100,
			left: 10 + 2 * longestLayerName
		},
		margin2 = {
			top: 430,
			right: 10,
			bottom: 20,
			left: 10 + 2 * longestLayerName
		},
		width = divWidth - 25,
		height = 500 - margin.top - margin.bottom,
		height2 = 500 - margin2.top - margin2.bottom;

	var x = d3.scale.ordinal().rangeBands([0, width], 0),
		x2 = d3.scale.ordinal().rangeBands([0, width], 0),
		y = d3.scale.ordinal().rangeRoundBands([0, height], 0),
		y1 = d3.scale.ordinal().rangeRoundBands([0, height], 0);

	d3.select("svg").remove();

	var svg = d3.select("#chart").append("svg")
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

	//Draw the chart
	function displayChart(data, memoryType, section) {

		// Display memory type
		memoryBox.innerHTML = "Memory: " + memoryType;

		//Display tile 
		tileBox.innerHTML = "Viewing information for Tile: " + section;

		//Define color scales
		colorScale = d3.scale.ordinal().domain([0, d3.max(data, function (d) {
			return d.label;
		})]).range(['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080',
			'#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075'
		]);
		// grayColorScale = d3.scale.ordinal().domain([0, d3.max(data, function(d) {
		//     return d.label;
		// })]).range(['#DCDCDC', '#D3D3D3', '#C0C0C0', '#BEBEBE', '#989898', '#808080', '#696969', '#555555', '#E5E4E2',
		//     '#727472', '#928E85', '#708090', '#A9A9A9', '#acacac'
		// ]);

		var grayColorScale = d3.scale.linear()
			.domain([0, d3.max(data, function (d) {
				return d.label;
			})]).range(['#DCDCDC', '#343434']);

		// svg.append("g")
		// .attr("class", "legendLinear")
		// .attr("transform", "translate(0,0)")

		// var legendLinear = d3.legend.color()
		// .shapeWidth(30)
		// .cells(34)
		// .orient('horizontal')
		// .scale(grayColorScale);

		// svg.select(".legendLinear")
		// .call(legendLinear);


		// remove predrawn structures
		var bars = focus.selectAll('.bar').remove();
		focus.select(".x.axis").remove();
		focus.select(".y.axis").remove();

		//update scales
		x.domain(data.map(function (d) {
			return d.location
		}));

		y.domain(layersArray.map(function (d) {
			console.log(d)
			return d
		}));

		x2.domain(data.map(function (d) {
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
		focus.selectAll("text").each(function () {
			if (this.getBBox().width > maxw) maxw = this.getBBox().width;
		});
		svg.attr("transform", "translate(" + maxw + ",0)");

		//add brush
		var brush = d3.svg.brush()
			.x(x2)
			.on("brush", brushed);

		enter(data, focus)
		updateScale(data)

		// draw the subbars
		var subBars = context.selectAll('.subBar')
			.data(data)
		subBars.enter().append("rect")
			.classed('subBar', true)
			.attr({
				height: function (d) {
					return 10;
				},
				width: function (d) {
					return x.rangeBand()
				},
				x: function (d) {
					return x2(d.location);
				},
				y: function (d) {
					return 10
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

		/**function to update the chart based on the 
		 * portion of the graph zoomed into 
		 */
		function brushed() {
			var selected = null;
			selected = x2.domain()
				.filter(function (d) {
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
			updateScale(updatedData)
		}

		/** Update scale based on number of 
		 * data values
		 */
		function updateScale(data) {
			var tickScale = d3.scale.pow().range([data.length / 2, 0]).domain([data.length, 0]).exponent(.5)
			var brushValue = brush.extent()[1] - brush.extent()[0];
			if (brushValue === 0) {
				brushValue = width;
			}

			var tickValueMultiplier = Math.ceil(Math.abs(tickScale(brushValue)));

			var filteredTickValues = data.filter(function (d, i) {
				return i % tickValueMultiplier === 0
			}).map(function (d) {
				return d.location
			})
			focus.select(".x.axis").call(xAxis.tickValues(filteredTickValues));
		}

		/** Build the bars based on  
		 * updated data values
		 */
		this.update = function (data) {
			x.domain(data.map(function (d) {
				return d.location
			}));

			y.domain(layersArray.map(function (d) {
				return d
			}));

			var focusHeight = focus.node().getBoundingClientRect().height;
			var size = layers.size
			if (size === 0) {
				size = 1;
			}
			var newHeight = focusHeight / size;

			var bars = focus.selectAll('.bar')
				.data(data)
			bars
				.attr({
					height: function (d, i) {
						return newHeight;
					},
					width: function (d) {
						return x.rangeBand()
					},
					x: function (d) {
						return x(d.location);
					},
					y: function (d) {
						if (d.layer === "input") {
							console.log(9 + y(d.layer))
						}
						return 9 + y(d.layer)
					},
					fill: function (d) {
						if (d.filled) {
							return colorScale(d.label);
						}
						return grayColorScale(d.label);
					},
					stroke: function (d) {
						if (d.filled) {
							return colorScale(d.label);
						}
						return grayColorScale(d.label);
					}
				})
				.on("mousemove", function (d) {
					layerBox.innerHTML = "Layer: " + d.layer;
					locationBox.innerHTML = "Location: " + d.location;

				})
				.on("mouseout", function (d) {
					layerBox.innerHTML = "Layer: ";
					locationBox.innerHTML = "Location: ";
				});
		}

		/** remove data values  
		 */
		function exit(data) {
			var bars = focus.selectAll('.bar').data(data)
			bars.exit().remove()
		}


		/** Build the bars based on  
		 * initial data values
		 */
		function enter(data, focus) {

			x.domain(data.map(function (d) {
				return d.location
			}));

			y.domain(layersArray.map(function (d) {
				return d
			}));

			var focusHeight = focus.node().getBoundingClientRect().height;
			var size = layers.size
			if (size === 0) {
				size = 1;
			}
			var newHeight = focusHeight / size;

			var bars = focus.selectAll('.bar')
				.data(data)
			bars.enter().append("rect")
				.style("stroke-linejoin", "round")
				.classed('bar', true)
				.attr({
					height: function (d, i) {
						return newHeight;
					},
					width: function (d) {
						return x.rangeBand()
					},
					x: function (d) {
						return x(d.location);
					},
					y: function (d) {
						if (d.layer === "input") {
							console.log(9 + y(d.layer))
						}
						return 9 + y(d.layer)
					},
					fill: function (d) {
						if (d.filled) {
							return colorScale(d.label);
						}
						return grayColorScale(d.label);
					},
					stroke: function (d) {
						if (d.filled) {
							return colorScale(d.label);
						}
						return grayColorScale(d.label);
					}
				})
				.on("mousemove", function (d) {

					layerBox.innerHTML = "Layer: " + d.layer;
					locationBox.innerHTML = "Location: " + d.location;

				})
				.on("mouseout", function (d) {

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

/**Fill the array depending on which of the memory type is selected
 */
async function fill(preResult) {
	var narrowSize = preResult["narrowSize"];
	var wideSize = preResult["wideSize"];
	var narrowAlloc = preResult['tensorAllocationNarrow'];
	var wideAlloc = preResult['tensorAllocationWide'];
	populateData(wideAlloc, wideSize, data1, layersWide, true)
	populateData(narrowAlloc, narrowSize, data2, layersNarrow, false)
	chart(1, data1, false)
}

function populateData(memoryAlloc, memorySize, data, layerSet, wide) {
	for (var i = 0; i < memoryAlloc.length; i++) {
		var allocs = memoryAlloc[i]["tensorTileAllocation_"][0]["tensorAllocation_"];
		var tileAllocs = memoryAlloc[i]["tensorTileAllocation_"];
		var memoryType;
		if (wide) {
			memoryType = "wide memory"
		} else {
			memoryType = "narrow memory"
		}
		for (var tile = 0; tile < tileAllocs.length; tile++) {
			allocs = tileAllocs[tile]["tensorAllocation_"]
			for (var j = 0; j < allocs.length; j++) {
				var alloc = allocs[j];
				var start = 0;
				var end = 0;
				var size = alloc["size_"];
				start = alloc["baseAddress_"];
				end = start + size;
				for (var k = start; k < end; k++) {
					if (end > memorySize) {
						//Display the Error message
						const errorMessage = document.getElementById("error-report");
						errorMessage.innerHTML = "Allocation with label " + alloc["tensorLabel_"] + " has invalid memory address of " + end + " in " + memoryType + ".";
						break;
					}
					var datum = {}
					datum.location = k;
					datum.layer = memoryAlloc[i]["layer_"];
					datum.tile = tile;
					datum.filled = false;
					datum.label = alloc["tensorLabel_"]
					data.push(datum)

					layerSet.add(memoryAlloc[i]["layer_"])
					if (longestLayerName < memoryAlloc[i]["layer_"].length) {
						longestLayerName = memoryAlloc[i]["layer_"].length
					}

				}
			}
		}
	}
}
var deltaCount = 0;
var narrowDeltas = new Array();
async function addDelta(postResult) {
	var narrowDelta = postResult["narrowDeltas"];
	var deltas = postResult["wideDeltas"];
	for (var i = 0; i < deltas.length; i++) {
		var delta = deltas[i];
		for (var j = 0; j < data1.length; j++) {
			var entry = data1[j];
			if (entry.layer === delta.layer && entry.tile === delta.tile && entry.location === delta.memoryAddressChanged && entry.label === delta.tensor) {
				entry.filled = true;
			}
		}
	}
	if (narrowDelta.length != 0) {
		for (var i = 0; i < narrowDelta.length; i++) {
			narrowDeltas.push(narrowDelta[i])
		}

	}
	deltaCount = deltaCount + deltas.length

	if (deltaCount >= 1000) {
		chart(1, data1, true)
		deltaCount = 0;
	}
}

// 
function deltaProccess() {
	for (var i = 0; i < narrowDeltas.length; i++) {
		var delta = narrowDeltas[i]
		for (var k = 0; k < data2.length; k++) {
			var entry = data2[k];
			if (entry.layer === delta.layer && entry.tile === delta.tile && entry.location === delta.memoryAddressChanged && entry.label === delta.tensor) {
				entry.filled = true;
			}
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