var tk = new verovio.toolkit();

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


class VerovioFunctions {
    constructor() {
        this.tk = new verovio.toolkit();

        this.tk.setOptions({
            font: 'Bravura',
            scaleToPageSize: true,
            landscape: true,
            adjustPageWidth: true,
            scale: 180
        });

    }

    loadMusicXmlFromBase64(base64) {
        const xml = atob(base64);
        try {
            this.tk.loadData(xml);
            return true;
        } catch (e) {
            console.error("Verovio loadData error", e);
            return false;
        }
    }

    renderPageToDom(page) {
        try {
            const svg = this.tk.renderToSVG(page, {});
            return svg;
        } catch (e) {
            console.log("Verovio svg rendering error", e);
            return "";
        }
    }

    getPageCount() {
        return this.tk.getPageCount();
    }
}