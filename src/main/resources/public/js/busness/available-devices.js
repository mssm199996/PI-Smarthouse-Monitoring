var datatable = null;
var nextIndex = 1;

(function () {
    $(document).ready(function () {
        initDatatable();
        initWebSocket();
    });
})();

function initDatatable() {
    datatable = $("#dataTable").DataTable({});
}

function onLinesCallback(devices) {
    $.each(devices, function (index, device) {
        if (device.add == true) {
            datatable.row.add([
                nextIndex++,
                device.device.serialNumber,
                device.device.room,
                device.device.limit
            ]).node().id = device.device.serialNumber;
            datatable.draw(false);
        } else if (device.add == false) {
            datatable.row($("#" + device.device.serialNumber)).remove().draw();

            nextIndex--;
        }
    });
}

function initWebSocket() {
    var host = $("meta[name='host']").attr("value");
    var socket = new WebSocket("ws://" + host + "/monitoring-devices-list");

    socket.onopen = function (e) {
        setInterval(function () {
            socket.send("");
        }, 500);
    };

    socket.onmessage = function (message) {
        if (datatable != null) {
            onLinesCallback(JSON.parse(message.data));
        }
    };
}

