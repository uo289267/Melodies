var tk = new verovio.toolkit();

tk.setOptions({
    scaleToPageSize: true,
    landscape: true,
    adjustPageWidth: true,
    scale: 200
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
    const svg = tk.renderToSVG(page, {});
    return svg;
}

function getPageCount() {
    return tk.getPageCount();
}