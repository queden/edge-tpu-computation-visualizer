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
    const visualizerWindow = window.open("report.html", "Visualizer", "_blank", "toolbar=yes,scrollbars=yes,resizable=yes,width=400,height=400", "true");
    visualizerWindow.title = "Visualizer";
    visualizerWindow.focus();

    // Listener to retrieve information from the main page.
    visualizerWindow.addEventListener("message", function(message) {
      // Determines if the sent data is the file's id or information.
      if (!(isNaN(parseInt(message.data)))) {
        // Stores the selected file's id.

        const fileIdBox = visualizerWindow.document.getElementById("trace-info-box");
        fileIdBox.title = message.data;
        console.log(fileIdBox.title);
      } else {
        // Displays the selected file's information.

        const fileInfoBox = visualizerWindow.document.getElementById("file-info");
        fileInfoBox.innerHTML += message.data;
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

// Runs the visualization of the chosen simulation trace.
async function runVisualization() {
  alert("Visualization begun");

  const traceBox = document.getElementById("trace-info-box");
  traceBox.innerHTML = '';
  console.log(traceBox.title);

  /*
    /report -> sends to report servlet
    process=pre -> performs preprocessing of the proto information
    fileId=traceBox.title -> the id of the file to retrieve from datastore
  */
  const preprocess = await fetch('/report?process=pre&fileId=' + traceBox.title, {method: 'GET'});
  const preprocessResponse = await preprocess.json();

  // Process initial json information.
  // TODO: Substitute
  const init = document.createElement("p");
  init.innerHTML = preprocessResponse.message;
  traceBox.appendChild(init);

  var numTraces = preprocessResponse.numTraces

  if (!preprocessResponse.isError) {
    for (i = 0; i < numTraces ; i += 1000) {
      // Run through the traces, information processing will happen within the function.

      await runTraces(i, numTraces);
    }
  } else {
    // Handle error?
  }

  const done = document.createElement("p");

  done.innerHTML = "Validation finished.";

  traceBox.appendChild(done);

  alert("Visualization completed");
}

/*
  Processes the different chunks of specified trace indicies.

  start -> the beginning index of the traces to be processed
  numTraces -> the total number of traces
*/
async function runTraces(start, numTraces) {
  // Retrieves box to display error/processing information.
  const traceBox = document.getElementById("trace-info-box");

  /*
    /report -> sends information to report servlet
    process=post -> runs trace validation algorithm on selected proto
    start=start -> the index of the traces to start processing
  */
  const traceResponse = await fetch('/report?process=post&start=' + start, {method: 'GET'});
  const traceProcess = await traceResponse.json();

  // Process json trace information.
  // TODO: Substitute
  var responseMessage = document.createElement("p");

  var end = (start + 999 < numTraces) ? start + 999 : numTraces;

  if (traceProcess.error.stackTrace.length == 0) {
    responseMessage.innerHTML += `Traces ${start}-${end} validated.`;
  } else {
    responseMessage.innerHTML += `Trace Validation Error: ${traceProcess.message}`;
  }

  traceBox.appendChild(responseMessage);
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

// Updates the visualizer state.
function loadMemory() { 
 // TODO: Substitute with visualizer function
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