$(document).ready(function () {
    var evaluationForm = $('#evaluation-form');

    evaluationForm.submit(function (event) {
        event.preventDefault();
        var campaign = $('#flagship-campaign-input').val();
        var flagName = $('#flagship-flag-name-input').val();
        var visitorId = $('#flagship-visitor-id-input').val();
        var evaluateUrl = `${window.location.origin}/evaluate/?campaign=${campaign}&flagName=${flagName}&visitorId=${visitorId}`;
        window.location.replace(evaluateUrl);
    });

    $(document).keypress(function (event) {
        if(event.which == 13) { // Enter key
            evaluationForm.submit();
        }
    });
});
