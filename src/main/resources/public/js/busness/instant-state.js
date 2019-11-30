var CHARTS = {};

(function () {
    $(document).ready(function () {
        initWebSocket();
    });
})();

function initWebSocket() {
    var host = $("meta[name='host']").attr("value");
    var socket = new WebSocket("ws://" + host + "/monitoring-temperatures");

    socket.onopen = function (e) {
        setInterval(function () {
            socket.send("");
        }, 500);
    };

    socket.onmessage = function (message) {
        var temperatureEntity = JSON.parse(message.data);

        var serialNumber = temperatureEntity.serialNumber;
        var value = temperatureEntity.value;
        var time = temperatureEntity.instant;

        var chart = CHARTS[serialNumber];

        if (typeof chart == 'undefined') {
            $("#charts-container")
                .append($("<div>").addClass("chart-area")
                    .append($("<canvas>").attr("id", serialNumber)))
                .append($("<hr>"));

            chart = new Chart(document.getElementById(serialNumber), {
                type: 'line',
                data: {
                    datasets: [{
                        label: "Temperature: " + serialNumber,
                        lineTension: 0.3,
                        backgroundColor: "rgba(78, 115, 223, 0.05)",
                        borderColor: "rgba(78, 115, 223, 1)",
                        pointRadius: 3,
                        pointBackgroundColor: "rgba(78, 115, 223, 1)",
                        pointBorderColor: "rgba(78, 115, 223, 1)",
                        pointHoverRadius: 3,
                        pointHoverBackgroundColor: "rgba(78, 115, 223, 1)",
                        pointHoverBorderColor: "rgba(78, 115, 223, 1)",
                        pointHitRadius: 10,
                        pointBorderWidth: 2
                    }],
                },
                options: {
                    maintainAspectRatio: false,
                    layout: {
                        padding: {
                            left: 10,
                            right: 25,
                            top: 25,
                            bottom: 0
                        }
                    },
                    scales: {
                        xAxes: [{
                            time: {
                                unit: 'date'
                            },
                            gridLines: {
                                display: false,
                                drawBorder: false
                            },
                            ticks: {
                                maxTicksLimit: 7
                            }
                        }],
                        yAxes: [{
                            ticks: {
                                maxTicksLimit: 5,
                                padding: 10,
                                // Include a dollar sign in the ticks
                                callback: function (value, index, values) {
                                    return value;
                                }
                            },
                            gridLines: {
                                color: "rgb(234, 236, 244)",
                                zeroLineColor: "rgb(234, 236, 244)",
                                drawBorder: false,
                                borderDash: [2],
                                zeroLineBorderDash: [2]
                            }
                        }],
                    },
                    legend: {
                        display: false,
                    },
                    title: {
                        display: true,
                        position: 'top',
                        text: "Temperature: " + serialNumber
                    },
                    tooltips: {
                        backgroundColor: "rgb(255,255,255)",
                        bodyFontColor: "#858796",
                        titleMarginBottom: 10,
                        titleFontColor: '#6e707e',
                        titleFontSize: 14,
                        borderColor: '#dddfeb',
                        borderWidth: 1,
                        xPadding: 15,
                        yPadding: 15,
                        displayColors: false,
                        intersect: false,
                        mode: 'index',
                        caretPadding: 10,
                        callbacks: {
                            label: function (tooltipItem, chart) {
                                return chart.datasets[tooltipItem.datasetIndex].label || '';
                            }
                        }
                    }
                }
            });

            CHARTS[serialNumber] = chart;
        }

        chart.data.labels.push(time);
        chart.data.datasets.forEach(function (dataset) {
            dataset.data.push(value);
        });

        chart.update();
    };
}