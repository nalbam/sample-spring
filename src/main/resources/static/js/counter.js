// Counter API of sample-node (same host, "sample-<name>" replaced by "sample-node").
let API_URL = location.protocol + '//' + location.host.replace(/sample-([a-z]+)/, 'sample-node');

function _counter(name, method) {
    let url = API_URL + `/counter/${name}`;
    $.ajax({
        url: url,
        method: method,
        success: function (res, status) {
            // console.log(`_counter (${name}) : ${status}`);
            if (res) {
                // The response comes from another service: show it as text, not HTML.
                $(`#thumbs-${name}-count`).text(res);
            }
        }
    });
}

$(function () {
    _counter('up', 'GET');
    _counter('down', 'GET');
    setInterval(function () {
        _counter('up', 'GET');
        _counter('down', 'GET');
    }, 1000);

    $('.btn-thumbs-up').on('click', function () {
        _counter('up', 'POST');
    });
    $('.btn-thumbs-down').on('click', function () {
        _counter('down', 'DELETE');
    });
});
