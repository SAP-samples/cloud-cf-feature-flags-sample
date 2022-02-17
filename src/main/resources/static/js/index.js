var evaluationAncor = $('#evaluation-ancor');
var nameInput = $('#feature-flag-name-input');
nameInput.change(function () {
    evaluationAncor.attr('href', '/evaluate/' + $(this).val());
});
var identifierInput = $('#feature-flag-identifier-input');

$(document).keypress(function (event) {
    if(event.which == 13) { // Enter key
        event.preventDefault();

        var name = nameInput.val();
        var identifier = identifierInput.val();
        var href = `/evaluate/${name}?identifier=${identifier}`;
        evaluationAncor.attr('href', href)
        evaluationAncor[0].click();
    }
});
