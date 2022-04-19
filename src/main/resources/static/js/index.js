$(document).ready(function () {
    var evaluationAnchor = $('#evaluation-anchor');

    evaluationAnchor.click(function (event) {
        var campaign = $('#flagship-campaign-input').val();
        var flagName = $('#flagship-flag-name-input').val();
        var visitorId = $('#flagship-visitor-id-input').val();
        var href = `/evaluate/?campaign=${campaign}&flagName=${flagName}&visitorId=${visitorId}`;
        evaluationAnchor.attr('href', href);
    });

    $(document).keypress(function (event) {
        if(event.which == 13) { // Enter key
            event.preventDefault();
            evaluationAnchor[0].click();
        }
    });
});
