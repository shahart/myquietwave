let myWindow;
let countdownInterval;
let nowPlayingInterval;
let isRadioPending = false;
function fetchOnAirXml(station) {
  const target = "http://glzxml.blob.core.windows.net/dalet/" + station + "-onair/onair.xml";
  const proxy = "https://myquietwave.lat-shahar.workers.dev/?url=" + encodeURIComponent(target);
  return fetch(proxy, { signal: AbortSignal.timeout(8e3) }).then((r) => {
    if (!r.ok) throw new Error(String(r.status));
    return r.text();
  });
}
function fetchAndShowNowPlaying(station, isNewWin) {
  fetchOnAirXml(station).then((xml) => {
    const doc = new DOMParser().parseFromString(xml, "text/xml");
    const title = doc.querySelector("Current > titleName");
    const artist = doc.querySelector("Current > artistName");
    const year = doc.querySelector("Current > year");
    if (title) {
      let msg = "\u{1F3B5} " + (artist ? artist.textContent + " - " : "") + title.textContent + " " + (year ? year.textContent : "");
      const ntitle = doc.querySelector("Next > titleName");
      const nartist = doc.querySelector("Next > artistName");
      const nyear = doc.querySelector("Next > year");
      if (ntitle) {
        const nmsg = "\u05D4\u05E9\u05D9\u05E8 \u05D4\u05D1\u05D0:  " + (nartist ? nartist.textContent + " - " : "") + ntitle.textContent + " " + (nyear ? nyear.textContent : "");
        if (isNewWin) {
          msg += "\n\n" + nmsg;
        } else {
          const nel = document.getElementById("next-title");
          nel.textContent = nmsg;
        }
      }
      if (isNewWin) {
        window.alert(msg);
      } else {
        const el = document.getElementById("now-title");
        el.textContent = msg;
      }
    }
  }).catch(() => {
    console.error("failed");
  });
}
function showGlglzNowPlaying() {
  const url = document.getElementById("stationSelect").value;
  let station = null;
  if (url === "https://glzwizzlv.bynetcdn.com/glglz_mp3") station = "glglz";
  else if (url === "https://glzwizzlv.bynetcdn.com/glz_mp3") station = "glz";
  if (!station) return;
  clearInterval(nowPlayingInterval);
  fetchAndShowNowPlaying(station, false);
  nowPlayingInterval = setInterval(() => fetchAndShowNowPlaying(station, false), 9e4);
}
function stopNowPlaying() {
  clearInterval(nowPlayingInterval);
}
function openWin() {
  const url = document.getElementById("stationSelect").value;
  myWindow = window.open(url, "_blank", "width=500,height=500");
}
function whatsNext() {
  fetchAndShowNowPlaying("glglz", true);
}
function listenNow() {
  isRadioPending = false;
  if (myWindow && !myWindow.closed) {
    alert("A window is already open. Close it first.");
    return;
  }
  showGlglzNowPlaying();
  openWin();
  clearInterval(countdownInterval);
  document.getElementById("status").textContent = "Playing";
  document.getElementById("timer").textContent = "";
  document.getElementById("listenNowButton").disabled = true;
  document.getElementById("radioButton").disabled = true;
  let checkInterval = setInterval(() => {
    if (myWindow && myWindow.closed) {
      stopNowPlaying();
      document.getElementById("now-title").textContent = "";
      document.getElementById("next-title").textContent = "";
      document.getElementById("listenNowButton").disabled = false;
      document.getElementById("radioButton").disabled = false;
      document.getElementById("status").textContent = "";
      document.getElementById("timer").textContent = "";
      clearInterval(checkInterval);
    }
  }, 1e3);
}
function sleep(time) {
  return new Promise((resolve) => setTimeout(resolve, time));
}
function setCountdown(label, durationMs) {
  const statusEl = document.getElementById("status");
  const timerEl = document.getElementById("timer");
  const endTime = Date.now() + durationMs;
  clearInterval(countdownInterval);
  function render() {
    const remainingMs = Math.max(0, endTime - Date.now());
    const totalSeconds = Math.floor(remainingMs / 1e3);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    if (remainingMs >= 1e3) {
      statusEl.textContent = label;
      timerEl.textContent = `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}  `;
    } else {
      statusEl.textContent = "";
      timerEl.textContent = "";
    }
    if (label === " \u05D4\u05D7\u05D3\u05E9\u05D5\u05EA \u05D9\u05D5\u05E9\u05EA\u05E7\u05D5 \u05D1\u05E2\u05D5\u05D3 " && myWindow && myWindow.closed) {
      statusEl.textContent = "";
      timerEl.textContent = "";
      document.getElementById("radioButton").disabled = false;
    }
    if (remainingMs <= 0) {
      clearInterval(countdownInterval);
    }
  }
  render();
  countdownInterval = setInterval(render, 1e3);
}
function listenToNextNews() {
  isRadioPending = true;
  const now = /* @__PURE__ */ new Date();
  const minutes = now.getMinutes();
  let msToNextHour = ((60 - minutes) * 60 - now.getSeconds()) * 1e3;
  document.getElementById("radioButton").disabled = true;
  setCountdown(" \u05D4\u05D7\u05D3\u05E9\u05D5\u05EA \u05D9\u05D5\u05E9\u05DE\u05E2\u05D5 \u05D1\u05E2\u05D5\u05D3 ", msToNextHour);
  sleep(msToNextHour).then(() => {
    if (!isRadioPending) {
      document.getElementById("radioButton").disabled = false;
      return;
    }
    if (myWindow && !myWindow.closed) {
      alert("A window is already open. Close it first.");
      document.getElementById("radioButton").disabled = false;
      return;
    }
    openWin();
    isRadioPending = false;
    if (myWindow) {
      let newsLength = 4 * 60 * 1e3;
      setCountdown(" \u05D4\u05D7\u05D3\u05E9\u05D5\u05EA \u05D9\u05D5\u05E9\u05EA\u05E7\u05D5 \u05D1\u05E2\u05D5\u05D3 ", newsLength);
      sleep(newsLength).then(() => {
        myWindow.close();
        stopNowPlaying();
        document.getElementById("now-title").textContent = "";
        document.getElementById("next-title").textContent = "";
        document.getElementById("status").textContent = "Closed";
        document.getElementById("timer").textContent = "";
        document.getElementById("radioButton").disabled = false;
      });
    } else {
      console.error("Failed to open pop-up window");
      window.alert("Failed to open pop-up window");
      document.getElementById("status").textContent = "Failed to open pop-up window";
      document.getElementById("timer").textContent = "";
      document.getElementById("radioButton").disabled = false;
    }
  });
}
