var tk = new verovio.toolkit();

tk.setOptions({
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
        console.log(typeof svg)
        console.log("Verovio svg rendering good", svg);
        console.log(typeof svg)
        return svg;
    } catch (e) {
        console.log("Verovio svg rendering error", e);
        return "";
    }

}

function getPageCount() {
    return tk.getPageCount();
}