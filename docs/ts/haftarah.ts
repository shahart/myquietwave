let haftarahConnectionUrl = "";
const haftarahConnectionCache = new Map();
const haftarahArticleCache = new Map();
const haftarahWeeklyUrl = "https://www.haftara.co.il/הפטרת-השבוע/";
let haftarahArticleParasha = "";

function getKolKoreParashaUrl(parashaName) {
    const parashaSlug = parashaName
        .replace(/^פרשת\s+/, "")
        .replace(/[\u0591-\u05BD\u05BF\u05C1-\u05C2\u05C4-\u05C5\u05C7]/g, "")
        .replace(/[־‐\-‒–—―\s]+/g, "-")
        .replace(/^-|-$/g, "");

    return "https://kol-kore.org/" + encodeURIComponent("פרשות") + "/" +
        encodeURIComponent("הפטרה-פרשת-" + parashaSlug) + "/";
}

async function fetchWithRetry(url, attempts = 2) {
    let lastError;

    for (let attempt = 0; attempt < attempts; attempt++) {
        try {
            const response = await fetch(url, { signal: AbortSignal.timeout(30000) });
            if (response.ok) return response;

            lastError = new Error("HTTP " + response.status);
            if (response.status < 500) break;
        } catch (error) {
            lastError = error;
        }
    }

    throw lastError;
}

function setHaftarahConnectionParasha(parashaName) {
    haftarahConnectionUrl = getKolKoreParashaUrl(parashaName);
    haftarahArticleParasha = parashaName.replace(/^פרשת\s+/, "");
    setHaftarahConnectionButtonState(true);
}

function normalizeHaftarahArticleText(text) {
    return text
        .replace(/[\u0591-\u05BD\u05BF\u05C1-\u05C2\u05C4-\u05C5\u05C7]/g, "")
        .replace(/[־‐\-‒–—―\s]+/g, " ")
        .trim();
}

function extractHaftarahArticleLink(html) {
    const page = new DOMParser().parseFromString(html, "text/html");
    const candidates = Array.from(page.querySelectorAll("a[href]"))
        .filter(link => /^הפטרת\s+פרשת/.test(link.textContent.replace(/\s+/g, " ").trim()));
    if (!candidates.length) throw new Error("The weekly article link was not found");

    const selected = normalizeHaftarahArticleText(haftarahArticleParasha);
    const matchingLink = selected && candidates.find(link =>
        normalizeHaftarahArticleText(link.textContent).includes(selected));
    return matchingLink ? new URL(matchingLink.getAttribute("href"), haftarahWeeklyUrl).href : "";
}

function buildHaftarahArticleUrl(parashaName) {
    const slug = normalizeHaftarahArticleText(parashaName.replace(/^פרשת\s+/, ""))
        .replace(/\s+/g, "-");
    if (!slug) return "";
    return new URL("/" + encodeURIComponent("הפטרת-פרשת-" + slug) + "/", haftarahWeeklyUrl).href;
}

function extractHaftarahArticle(html) {
    const page = new DOMParser().parseFromString(html, "text/html");
    const article = page.querySelector(".entry-content, .post-content, article");
    if (!article) throw new Error("The article text was not found");

    const content = article.cloneNode(true);
    content.querySelectorAll("script, style, nav, .sharedaddy, .entry-footer, .post-navigation")
        .forEach(element => element.remove());
    content.querySelectorAll("br").forEach(element => element.replaceWith("\n"));
    content.querySelectorAll("p, li, h1, h2, h3, h4").forEach(element => element.append("\n\n"));
    return content.textContent
        .replace(/\u00a0/g, " ")
        .replace(/[ \t]+\n/g, "\n")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
}

async function showHaftarahArticle() {
    const dialog = document.getElementById('haftarahArticleDialog');
    const content = document.getElementById('haftarahArticleContent');
    const source = document.getElementById('haftarahArticleSource');
    content.textContent = 'טוען...';
    if (typeof dialog.showModal === 'function') {
        if (!dialog.open) dialog.showModal();
    } else {
        dialog.setAttribute('open', '');
    }

    try {
        let articleUrl = haftarahArticleCache.get("url:" + haftarahArticleParasha);
        if (!articleUrl) {
            try {
                const weeklyProxyUrl = "https://myquietwave.lat-shahar.workers.dev/?url=" +
                    encodeURIComponent(haftarahWeeklyUrl);
                const weeklyResponse = await fetchWithRetry(weeklyProxyUrl);
                articleUrl = extractHaftarahArticleLink(await weeklyResponse.text());
            } catch (error) {
                console.warn("Could not resolve the article from the weekly page", error);
            }
            articleUrl = articleUrl || buildHaftarahArticleUrl(haftarahArticleParasha);
            articleUrl = articleUrl || haftarahWeeklyUrl;
            haftarahArticleCache.set("url:" + haftarahArticleParasha, articleUrl);
        }

        source.href = articleUrl;
        let articleText = haftarahArticleCache.get(articleUrl);
        if (!articleText) {
            const articleProxyUrl = "https://myquietwave.lat-shahar.workers.dev/?url=" +
                encodeURIComponent(articleUrl);
            const articleResponse = await fetchWithRetry(articleProxyUrl);
            articleText = extractHaftarahArticle(await articleResponse.text());
            haftarahArticleCache.set(articleUrl, articleText);
        }
        content.textContent = articleText;
    } catch (error) {
        console.error("Failed to fetch the haftarah article", error);
        content.textContent = 'לא הצלחנו לטעון את התוכן. אפשר לפתוח את המקור בקישור למטה.';
    }
}

function setHaftarahConnectionButtonState(enabled, disabledReason = '') {
    const button = document.getElementById('haftarahConnectionButton');
    const wrapper = document.getElementById('haftarahConnectionButtonWrapper');
    const tooltip = document.getElementById('haftarahConnectionDisabledReason');

    button.disabled = !enabled;
    tooltip.textContent = enabled ? '' : disabledReason;
    if (enabled) {
        wrapper.removeAttribute('tabindex');
        wrapper.removeAttribute('aria-describedby');
    } else {
        wrapper.tabIndex = 0;
        wrapper.setAttribute('aria-describedby', tooltip.id);
    }
}

function extractHaftarahConnection(html) {
    const page = new DOMParser().parseFromString(html, "text/html");
    const heading = Array.from(page.querySelectorAll("h1, h2, h3, h4"))
        .find(element => element.textContent.replace(/\s+/g, " ").trim() ===
            "על הקשר בין ההפטרה לפרשה");
    const content = heading && heading.closest(".row_four")?.querySelector(".content_right");

    if (!content) {
        throw new Error("The requested section was not found");
    }

    const textContent = content.cloneNode(true) as Element;
    textContent.querySelectorAll("br").forEach(element => element.replaceWith("\n"));
    textContent.querySelectorAll("p, li").forEach(element => element.append("\n\n"));

    let text = textContent.textContent
        .replace(/\u00a0/g, " ")
        .replace(/[ \t]+\n/g, "\n")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
    const start = text.indexOf("נושאים בפרשה:");
    if (start >= 0) {
        text = text.substring(start);
    }

    return text;
}

function openHaftarahConnectionDialog() {
    const dialog = document.getElementById('haftarahConnectionDialog');
    if (typeof dialog.showModal === 'function') {
        if (!dialog.open) dialog.showModal();
    } else {
        dialog.setAttribute('open', '');
    }
}

function closeHaftarahConnection() {
    const dialog = document.getElementById('haftarahConnectionDialog');
    if (typeof dialog.close === 'function') {
        dialog.close();
    } else {
        dialog.removeAttribute('open');
    }
}

async function showHaftarahConnection() {
    if (!haftarahConnectionUrl) return;

    var targetUrl = haftarahConnectionUrl;
    const button = document.getElementById('haftarahConnectionButton');
    const content = document.getElementById('haftarahConnectionContent');
    document.getElementById('haftarahConnectionSource').href = targetUrl;
    content.textContent = 'טוען...';
    button.disabled = true;
    openHaftarahConnectionDialog();

    try {
        let text = haftarahConnectionCache.get(targetUrl);
        if (!text) {
            const proxyUrl = "https://myquietwave.lat-shahar.workers.dev/?url=" +
                encodeURIComponent(targetUrl);
            const response = await fetchWithRetry(proxyUrl);
            text = extractHaftarahConnection(await response.text());
            haftarahConnectionCache.set(targetUrl, text);
        }
        content.textContent = text;
    } catch (error) {
        console.error("Failed to fetch the haftarah connection", error);
        content.textContent = 'לא הצלחנו לטעון את התוכן. אפשר לפתוח את המקור בקישור למטה.';
    } finally {
        if (haftarahConnectionUrl === targetUrl) button.disabled = false;
    }
}

