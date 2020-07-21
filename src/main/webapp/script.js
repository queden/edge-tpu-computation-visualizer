async function goUpload() {
  location.replace("/index.html");
}

async function goVisualizer() {
  location.replace("/report.html");
}

// Updates the time zone to be used throughout the website
async function submitTimeZoneIndex() {
  // Retrieves the selected time zone
  const select = document.getElementById("time-zone");
  const zone = select.options[select.selectedIndex].value;

  /*
    /visualizer -> sends information to the report servlet
    time=true -> DOES update the time zone
    zone=zone -> sends the selected time zone information
  */
  await fetch('/visualizer?time=true&zone=' + zone, {method: 'GET'});
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

    for (i = 0; i < preprocessResponse.numTraces; i += 1000) {
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
  const traceResponse = await fetch('/report?time=false&process=post&start=' + start, {method: 'GET'});
  const traceProcess = await traceResponse.json();

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

function loadMemory() { 
 // TODO: Substitute with visulizer function
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