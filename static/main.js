import Validate from './validate.js';
const validator = new Validate();

function getCurrentTimeZone() {
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    console.log("Detected timezone: " + tz);
    return tz;
}

function addRowToTable(res) {
    console.log("Adding row to table: ", res);

    var table = document.getElementById("res-table");
    var tbody = table.getElementsByTagName("tbody")[0] || table;
    var row = document.createElement("tr");

    var isHit = document.createElement("td");
    var x = document.createElement("td");
    var y = document.createElement("td");
    var r = document.createElement("td");
    var time = document.createElement("td");
    var worktime = document.createElement("td");

    if (res.result === "true") {
        isHit.innerText = "Точно в цель";
    } else {
        isHit.innerText = "Попробуйте ещё раз";
    }

    x.innerText = res.x;
    y.innerText = res.y;
    r.innerText = res.r;
    time.innerText = res.time;
    worktime.innerText = res.workTime;

    row.appendChild(isHit);
    row.appendChild(x);
    row.appendChild(y);
    row.appendChild(r);
    row.appendChild(time);
    row.appendChild(worktime);
    tbody.appendChild(row);

    document.getElementById("dot").setAttribute("cx", String(300 + Number(res.x) * (200 / Number(res.r))));
    document.getElementById("dot").setAttribute("cy", String(300 - Number(res.y) * (200 / Number(res.r))));
    document.getElementById("dot").setAttribute("visibility", "visible");
}

function loadSavedResults() {
    console.log("Loading saved results from sessionStorage");

    const savedResults = sessionStorage.getItem("results");
    if (savedResults) {
        try {
            const results = JSON.parse(savedResults);
            console.log("Found " + results.length + " saved results");

            results.forEach(result => {
                addRowToTable(result);
            });
        } catch (e) {
            console.error("Ошибка при загрузке сохраненных результатов:", e);
            sessionStorage.removeItem("results");
        }
    } else {
        console.log("No saved results found");
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM loaded, initializing application");
    loadSavedResults();
});

document.getElementById('send-btn').addEventListener('click', function(event) {
    event.preventDefault();
    console.log("Send button clicked");

    const x = document.querySelector('input[name="x"]:checked');
    const y = document.querySelector('#coord-y');
    const r = document.querySelector('#coord-r');

    console.log("Input values - x:", x ? x.value : "null",
        "y:", y ? y.value : "null",
        "r:", r ? r.value : "null");

    const check = validator.check(x, y, r);
    console.log("Validation result:", check);

    if (check.allOk) {
        const coords = validator.getCoords();
        const url = `/calculate?x=${coords.x}&y=${coords.y}&r=${coords.r}&timeZone=${getCurrentTimeZone()}`;


        console.log("Sending request to: " + url);

        fetch(url, {
            method: 'GET',
        })
            .then(response => {
                console.log("Received response, status: " + response.status);

                if (!response.ok) {
                    throw new Error(`${response.status}`);
                }
                return response.json();
            })
            .then(function(res) {
                console.log("Response data:", res);

                if (res.error === 'all ok') {
                    document.getElementById("input-log").innerText = '';
                    addRowToTable(res);

                    const savedResults = sessionStorage.getItem("results");
                    let resultsArray = [];

                    if (savedResults) {
                        try {
                            resultsArray = JSON.parse(savedResults);
                            if (!Array.isArray(resultsArray)) {
                                resultsArray = [];
                            }
                        } catch (e) {
                            console.error("Ошибка парсинга сохраненных данных:", e);
                            resultsArray = [];
                        }
                    }

                    resultsArray.push(res);
                    sessionStorage.setItem("results", JSON.stringify(resultsArray));
                    console.log("Result saved to sessionStorage");
                } else {
                    console.error("Server error:", res.error);

                    if (res.error === "fill") {
                        document.getElementById("input-log").innerText = "Заполните все поля";
                    } else if (res.error === "method") {
                        document.getElementById("input-log").innerText = "Только GET запросы";
                    } else {
                        document.getElementById("input-log").innerText = "Ошибка сервера: " + res.error;
                    }
                }
            })
            .catch(error => {
                console.error("Fetch error:", error);
                document.getElementById("input-log").innerText = "Ошибка соединения: " + error.message;
            });
    } else {
        console.error("Validation error:", check.log);
        document.getElementById("input-log").innerText = check.log;
    }
});