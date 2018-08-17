/* mpc4s-player.js */

var currentSettingsVersion = 2;

elmApp.ports.updateProgress.subscribe(function(input) {
    var id = input[0];
    var range = input[1];
    var perc = (range.start * 100 / range.end)
    if (perc < 0) {
        perc = 0;
    }
    if (perc > 100) {
        perc = 100;
    }
    $('.'+ id).progress("set percent", perc);
});

elmApp.ports.initElements.subscribe(function() {
    console.log("Initialsing elements …");
    $('.ui.dropdown').dropdown();
    $('.ui.checkbox').checkbox();
    $('.ui.accordion').accordion();
    $('.ui.progress').off("click.progress").on("click.progress", function(ev) {
        var $target = $(ev.target);
        var offset = $target.offset().left;
        var total = $(ev.delegateTarget).width();
        if (total == 0) {
            total = 1;
        }
        var x = ev.clientX - offset;
        elmApp.ports.seekClick.send(x / total);
    });
});


elmApp.ports.setTitle.subscribe(function(title) {
    $("title").text(title);
});

elmApp.ports.getScroll.subscribe(function() {
    // from https://stackoverflow.com/questions/3464876/javascript-get-window-x-y-position-for-scroll
    var doc = document.documentElement;
    var left = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
    var top = (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0);
    elmApp.ports.currentScroll.send({left: left, top: top});
});

elmApp.ports.scrollTo.subscribe(function(scroll) {
    window.scrollTo(scroll.left, scroll.top);
});

elmApp.ports.storeSettings.subscribe(function(settings) {
    settings.version = currentSettingsVersion;
    localStorage.setItem('settings', JSON.stringify(settings));
    elmApp.ports.receiveSettings.send(settings);
});

elmApp.ports.loadSettings.subscribe(function() {
    var storedSettings = localStorage.getItem('settings');
    var settings = storedSettings ? JSON.parse(storedSettings) : null;
    if (settings && settings.version === currentSettingsVersion) {
        elmApp.ports.receiveSettings.send(settings);
    }
});
