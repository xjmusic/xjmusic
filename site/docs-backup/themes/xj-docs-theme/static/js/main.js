function afterBodyLoad() {
    //Hamburger menu toggle
    $(".navbar-nav li a").click(function (event) {
        // check if window is small enough so dropdown is created
        var toggle = $(".navbar-toggler").is(":visible");
        console.info("Menu clicked", toggle);
        if (toggle) {
            $(".navbar-collapse").collapse('hide');
        }
    });
}

function capitalized(text) {
    return text.substr(0, 1).toUpperCase() + text.substr(1);
}
