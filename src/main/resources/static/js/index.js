var evaluationAncor = $('#evaluation-ancor');
var nameInput = $('#feature-flag-name-input');
nameInput.change(function () {
    evaluationAncor.attr('href', '/evaluate/' + $(this).val());
});

$(document).keypress(function (event) {
    if(event.which == 13) { // Enter key
        event.preventDefault();

        var name = nameInput.val();
        evaluationAncor.attr('href', '/evaluate/' + name)
        evaluationAncor[0].click();
    }
});
