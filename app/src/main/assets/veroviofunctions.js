
var tk = new verovio.toolkit();
tk.setOptions({
    font: 'Bravura',
    scaleToPageSize: true,
    landscape: true,
    adjustPageWidth: true,
    scale: 180
});

window.loadMusicXmlFromBase64 = function(base64) {
    const xml = atob(base64);
    try {
        tk.loadData(xml);
        return true;
    } catch (e) {
        console.error("Verovio loadData error", e);
        return false;
    }
}

window.renderPageToDom = function(page) {
    try {
        const svg = tk.renderToSVG(page, {});
        //console.log(svg);
        return svg;
    } catch (e) {
        console.log("Verovio svg rendering error", e);
        return "";
    }
}

window.getPageCount = function() {
    return tk.getPageCount();
}
