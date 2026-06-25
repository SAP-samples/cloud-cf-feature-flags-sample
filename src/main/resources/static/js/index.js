$(document).ready(function () {
    var evaluationAnchor = $('#evaluation-anchor');

    evaluationAnchor.click(function (event) {
        var name = $('#feature-flag-name-input').val();
        var identifier = $('#feature-flag-identifier-input').val();
        var encodedName = encodeURIComponent(name);
        var encodedIdentifier = encodeURIComponent(identifier);
        var href = `/evaluate/${encodedName}?identifier=${encodedIdentifier}`;
        evaluationAnchor.attr('href', href);
    });

    $(document).keypress(function (event) {
        if(event.which == 13) { // Enter key
            event.preventDefault();
            evaluationAnchor[0].click();
        }
    });
});
