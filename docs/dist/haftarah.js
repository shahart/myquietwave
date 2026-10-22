let haftarahConnectionUrl = "";
const haftarahConnectionCache = /* @__PURE__ */ new Map();
function getKolKoreParashaUrl(parashaName) {
  const parashaSlug = parashaName.replace(/^פרשת\s+/, "").replace(/[\u0591-\u05BD\u05BF\u05C1-\u05C2\u05C4-\u05C5\u05C7]/g, "").replace(/[־‐\-‒–—―\s]+/g, "-").replace(/^-|-$/g, "");
  return "https://kol-kore.org/" + encodeURIComponent("\u05E4\u05E8\u05E9\u05D5\u05EA") + "/" + encodeURIComponent("\u05D4\u05E4\u05D8\u05E8\u05D4-\u05E4\u05E8\u05E9\u05EA-" + parashaSlug) + "/";
}
async function fetchWithRetry(url, attempts = 2) {
  let lastError;
  for (let attempt = 0; attempt < attempts; attempt++) {
    try {
      const response = await fetch(url, { signal: AbortSignal.timeout(3e4) });
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
  setHaftarahConnectionButtonState(true);
}
function setHaftarahConnectionButtonState(enabled, disabledReason = "") {
  const button = document.getElementById("haftarahConnectionButton");
  const wrapper = document.getElementById("haftarahConnectionButtonWrapper");
  const tooltip = document.getElementById("haftarahConnectionDisabledReason");
  button.disabled = !enabled;
  tooltip.textContent = enabled ? "" : disabledReason;
  if (enabled) {
    wrapper.removeAttribute("tabindex");
    wrapper.removeAttribute("aria-describedby");
  } else {
    wrapper.tabIndex = 0;
    wrapper.setAttribute("aria-describedby", tooltip.id);
  }
}
function extractHaftarahConnection(html) {
  const page = new DOMParser().parseFromString(html, "text/html");
  const heading = Array.from(page.querySelectorAll("h1, h2, h3, h4")).find((element) => element.textContent.replace(/\s+/g, " ").trim() === "\u05E2\u05DC \u05D4\u05E7\u05E9\u05E8 \u05D1\u05D9\u05DF \u05D4\u05D4\u05E4\u05D8\u05E8\u05D4 \u05DC\u05E4\u05E8\u05E9\u05D4");
  const content = heading && heading.closest(".row_four")?.querySelector(".content_right");
  if (!content) {
    throw new Error("The requested section was not found");
  }
  const textContent = content.cloneNode(true);
  textContent.querySelectorAll("br").forEach((element) => element.replaceWith("\n"));
  textContent.querySelectorAll("p, li").forEach((element) => element.append("\n\n"));
  let text = textContent.textContent.replace(/\u00a0/g, " ").replace(/[ \t]+\n/g, "\n").replace(/\n{3,}/g, "\n\n").trim();
  const start = text.indexOf("\u05E0\u05D5\u05E9\u05D0\u05D9\u05DD \u05D1\u05E4\u05E8\u05E9\u05D4:");
  if (start >= 0) {
    text = text.substring(start);
  }
  return text;
}
function openHaftarahConnectionDialog() {
  const dialog = document.getElementById("haftarahConnectionDialog");
  if (typeof dialog.showModal === "function") {
    if (!dialog.open) dialog.showModal();
  } else {
    dialog.setAttribute("open", "");
  }
}
function closeHaftarahConnection() {
  const dialog = document.getElementById("haftarahConnectionDialog");
  if (typeof dialog.close === "function") {
    dialog.close();
  } else {
    dialog.removeAttribute("open");
  }
}
async function showHaftarahConnection() {
  if (!haftarahConnectionUrl) return;
  var targetUrl = haftarahConnectionUrl;
  const button = document.getElementById("haftarahConnectionButton");
  const content = document.getElementById("haftarahConnectionContent");
  document.getElementById("haftarahConnectionSource").href = targetUrl;
  content.textContent = "\u05D8\u05D5\u05E2\u05DF...";
  button.disabled = true;
  openHaftarahConnectionDialog();
  try {
    let text = haftarahConnectionCache.get(targetUrl);
    if (!text) {
      const proxyUrl = "https://myquietwave.lat-shahar.workers.dev/?url=" + encodeURIComponent(targetUrl);
      const response = await fetchWithRetry(proxyUrl);
      text = extractHaftarahConnection(await response.text());
      haftarahConnectionCache.set(targetUrl, text);
    }
    content.textContent = text;
  } catch (error) {
    console.error("Failed to fetch the haftarah connection", error);
    content.textContent = "\u05DC\u05D0 \u05D4\u05E6\u05DC\u05D7\u05E0\u05D5 \u05DC\u05D8\u05E2\u05D5\u05DF \u05D0\u05EA \u05D4\u05EA\u05D5\u05DB\u05DF. \u05D0\u05E4\u05E9\u05E8 \u05DC\u05E4\u05EA\u05D5\u05D7 \u05D0\u05EA \u05D4\u05DE\u05E7\u05D5\u05E8 \u05D1\u05E7\u05D9\u05E9\u05D5\u05E8 \u05DC\u05DE\u05D8\u05D4.";
  } finally {
    if (haftarahConnectionUrl === targetUrl) button.disabled = false;
  }
}
