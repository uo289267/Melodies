// Inicializamos Verovio
var tk = new verovio.toolkit();
tk.setOptions({
    font: 'Bravura',
    scaleToPageSize: true,
    landscape: true,
    adjustPageWidth: true,
    scale: 180
});

// Funciones globales
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
        return svg;
    } catch (e) {
        console.log("Verovio svg rendering error", e);
        return "";
    }
}

window.getPageCount = function() {
    return tk.getPageCount();
}

/*var tk = new verovio.toolkit();

tk.setOptions({
    font: 'Bravura',
    scaleToPageSize: true,
    landscape: true,
    adjustPageWidth: true,
    scale: 180
});

function loadMusicXmlFromBase64(base64) {
    const xml = atob(base64);
    try {
        tk.loadData(xml);
        return true;
    } catch (e) {
        console.error("Verovio loadData error", e);
        return false;
    }
}

function renderPageToDom(page) {

    try {
        const svg = tk.renderToSVG(page, {});
        return svg;
    } catch (e) {
        console.log("Verovio svg rendering error", e);
        return "";
    }

}

function getPageCount() {
    return tk.getPageCount();
}
*/