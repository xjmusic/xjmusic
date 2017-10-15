function afterBodyLoad() {
    //Hamburger menu toggle
    $(".navbar-nav li a").click(function (event) {
        var toggle = $(".navbar-toggler").is(":visible");
        if (toggle) {
            $(".navbar-collapse").collapse('hide');
        }
    });
}

function capitalized(text) {
    return text.substr(0, 1).toUpperCase() + text.substr(1);
}

function goto(url) {
    window.location.href = url;
}