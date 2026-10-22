let hdateStr = (/* @__PURE__ */ new Date()).toLocaleString("he", { calendar: "hebrew" });
let hdat = hdateStr.split(",")[0];
let hebyy = parseInt(hdateStr.split(" ")[2]);
hdat = hdat.substr(0, hdat.lastIndexOf(" "));
document.getElementById("hdat").innerHTML = "\u05D4\u05D9\u05D5\u05DD \u{1F4C5} " + no2gim(parseInt(hdat.split(" ")[0])) + " " + hdat.split(" ")[1] + " " + getYY(hebyy);
function updateClock() {
  const now = /* @__PURE__ */ new Date();
  const hours = now.getHours();
  const minutes = now.getMinutes();
  const seconds = now.getSeconds();
  const formattedTime = `${hours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
  document.getElementById("clock").innerHTML = "\u{1F570}\uFE0F " + formattedTime + "<br>" + Intl.DateTimeFormat().resolvedOptions().timeZone + "<br>offset (Hours) " + (/* @__PURE__ */ new Date()).getTimezoneOffset() / -60;
}
updateClock();
setInterval(updateClock, 500);
let days = "\u05E8\u05D0\u05E9\u05D5\u05DF,\u05E9\u05E0\u05D9,\u05E9\u05DC\u05D9\u05E9\u05D9,\u05E8\u05D1\u05D9\u05E2\u05D9,\u05D7\u05DE\u05D9\u05E9\u05D9,\u05E9\u05D9\u05E9\u05D9,\u05E9\u05D1\u05EA";
let d = /* @__PURE__ */ new Date();
document.getElementById("dat").innerHTML = d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear() + " - " + days.split(",")[d.getDay()];
