var page = new WebPage(), address, output, size;
 
if (phantom.args.length < 2 || phantom.args.length > 3) {
    console.log('Usage: google_adx_snapshot.js URL filename');
    phantom.exit();
} else {
    address = phantom.args[0];
    output = phantom.args[1];
    console.log('Taking snapshot of ' + address );
    
	page.onAlert = function(msg) { console.log('<WebPage> ' + msg); };
    page.settings.loadPlugins = true;
	//page.viewportSize = { width: 600, height: 600 };
	
    page.open(address, function (status) {
        if (status !== 'success') {
            console.log('Unable to load the address!');
            phantom.exit();
        } else {
            window.setTimeout(function () {
                page.render(output);
                phantom.exit();
            }, 10000);
        }
    });
    console.log('Snapshot done');
}
