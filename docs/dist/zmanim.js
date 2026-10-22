function getNextSaturdayDate(date = /* @__PURE__ */ new Date()) {
  const nextSaturday = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  nextSaturday.setDate(nextSaturday.getDate() + (6 - nextSaturday.getDay() + 7) % 7);
  return [
    nextSaturday.getFullYear(),
    String(nextSaturday.getMonth() + 1).padStart(2, "0"),
    String(nextSaturday.getDate()).padStart(2, "0")
  ].join("-");
}
function getLocalDateString(date = /* @__PURE__ */ new Date()) {
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0")
  ].join("-");
}
function getShabbatRangeEnd(date = /* @__PURE__ */ new Date()) {
  const end = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  end.setDate(end.getDate() + 7);
  return getLocalDateString(end);
}
function formatShabbatTime(item, includeLabel) {
  const time = item.date.split("T")[1].substring(0, 5);
  const pastPrefix = new Date(item.date) <= /* @__PURE__ */ new Date() ? "\u05D6\u05DE\u05DF \u05E2\u05D1\u05E8 " : "";
  return pastPrefix + (includeLabel ? item.hebrew + " " : "") + time;
}
const nachBooks = [
  ["\u05D9\u05D4\u05D5\u05E9\u05E2", 24],
  ["\u05E9\u05D5\u05E4\u05D8\u05D9\u05DD", 21],
  ["\u05E9\u05DE\u05D5\u05D0\u05DC \u05D0\u05F3", 31],
  ["\u05E9\u05DE\u05D5\u05D0\u05DC \u05D1\u05F3", 24],
  ["\u05DE\u05DC\u05DB\u05D9\u05DD \u05D0\u05F3", 22],
  ["\u05DE\u05DC\u05DB\u05D9\u05DD \u05D1\u05F3", 25],
  ["\u05D9\u05E9\u05E2\u05D9\u05D4\u05D5", 66],
  ["\u05D9\u05E8\u05DE\u05D9\u05D4\u05D5", 52],
  ["\u05D9\u05D7\u05D6\u05E7\u05D0\u05DC", 48],
  ["\u05D4\u05D5\u05E9\u05E2", 14],
  ["\u05D9\u05D5\u05D0\u05DC", 4],
  ["\u05E2\u05DE\u05D5\u05E1", 9],
  ["\u05E2\u05D5\u05D1\u05D3\u05D9\u05D4", 1],
  ["\u05D9\u05D5\u05E0\u05D4", 4],
  ["\u05DE\u05D9\u05DB\u05D4", 7],
  ["\u05E0\u05D7\u05D5\u05DD", 3],
  ["\u05D7\u05D1\u05E7\u05D5\u05E7", 3],
  ["\u05E6\u05E4\u05E0\u05D9\u05D4", 3],
  ["\u05D7\u05D2\u05D9", 2],
  ["\u05D6\u05DB\u05E8\u05D9\u05D4", 14],
  ["\u05DE\u05DC\u05D0\u05DB\u05D9", 3],
  ["\u05EA\u05D4\u05DC\u05D9\u05DD", 150],
  ["\u05DE\u05E9\u05DC\u05D9", 31],
  ["\u05D0\u05D9\u05D5\u05D1", 42],
  ["\u05E9\u05D9\u05E8 \u05D4\u05E9\u05D9\u05E8\u05D9\u05DD", 8],
  ["\u05E8\u05D5\u05EA", 4],
  ["\u05D0\u05D9\u05DB\u05D4", 5],
  ["\u05E7\u05D4\u05DC\u05EA", 12],
  ["\u05D0\u05E1\u05EA\u05E8", 10],
  ["\u05D3\u05E0\u05D9\u05D0\u05DC", 12],
  ["\u05E2\u05D6\u05E8\u05D0", 10],
  ["\u05E0\u05D7\u05DE\u05D9\u05D4", 13],
  ["\u05D3\u05D1\u05E8\u05D9 \u05D4\u05D9\u05DE\u05D9\u05DD \u05D0\u05F3", 29],
  ["\u05D3\u05D1\u05E8\u05D9 \u05D4\u05D9\u05DE\u05D9\u05DD \u05D1\u05F3", 36]
];
const totalNachChapters = nachBooks.reduce((total, [, chapters]) => total + chapters, 0);
function hebrewCalendarParts(date) {
  const formatter = new Intl.DateTimeFormat("en-u-ca-hebrew", {
    day: "numeric",
    month: "long",
    year: "numeric"
  });
  return Object.fromEntries(formatter.formatToParts(date).filter(({ type }) => type !== "literal").map(({ type, value }) => [type, value]));
}
function nachChapterAt(index) {
  if (index >= totalNachChapters) return "";
  let chapterIndex = index;
  for (const [book, chapters] of nachBooks) {
    if (chapterIndex < chapters) return `${book} ${chapterIndex + 1}`;
    chapterIndex -= chapters;
  }
}
function getNachYomi(date = /* @__PURE__ */ new Date()) {
  const today = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const roshHashana = new Date(today);
  while (true) {
    const { day, month } = hebrewCalendarParts(roshHashana);
    if (day === "22" && month === "Tishri") break;
    roshHashana.setDate(roshHashana.getDate() - 1);
  }
  const daysSinceRoshHashana = (today.getTime() - roshHashana.getTime()) / 864e5;
  const firstChapter = daysSinceRoshHashana * 2;
  if (firstChapter >= totalNachChapters) return "";
  return `${nachChapterAt(firstChapter)}\u2013${nachChapterAt(firstChapter + 1)}`;
}
async function calc() {
  document.getElementById("havdala").innerHTML = "";
  document.getElementById("lighting").innerHTML = "";
  var postfix = document.getElementById("locationSelect").value;
  saveInput("zmanim-location", postfix);
  if (postfix === "other") {
    postfix = document.getElementById("otherLocation").value.trim();
    saveInput("zmanim-location-other", document.getElementById("otherLocation").value.trim());
  }
  var useElevationParam = "&ue=on";
  if (postfix.indexOf(",ue") >= 1) {
    useElevationParam = "&ue=off";
    postfix = postfix.replace(",ue", "");
  }
  if (postfix.indexOf(",") > 0) {
    postfix = "latitude=" + postfix.split(",")[0].trim() + "&longitude=" + postfix.split(",")[1].trim();
  } else if (postfix.charAt(0) >= "0" && postfix.charAt(0) <= "9") {
    postfix = "geonameid=" + postfix;
  } else {
    if (postfix.toLowerCase() === "il-yavne") {
      postfix = "geonameid=293222";
    } else if (postfix.toLowerCase() === "il-mitzpe ramon") {
      postfix = "geonameid=294166";
    } else if (postfix.toLowerCase() === "il-lavi") {
      postfix = "geonameid=294427";
    } else if (postfix.toLowerCase() === "il-modiin ilit") {
      postfix = "geonameid=8199378";
    } else if (postfix.toLowerCase() === "il-betar ilit") {
      postfix = "geonameid=284375";
    } else if (postfix.toLowerCase() === "il-zefat") {
      postfix = "geonameid=293100";
    } else {
      postfix = "city=" + postfix;
    }
  }
  var url = `https://www.hebcal.com/zmanim?cfg=json&` + postfix + useElevationParam;
  if (postfix.startsWith("latitude=")) {
    url += "&tzid=Asia/Jerusalem";
  }
  const date = /* @__PURE__ */ new Date();
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  var formattedDate = `${year}-${month}-${day}`;
  var url2 = `https://www.hebcal.com/shabbat?cfg=json&` + postfix + useElevationParam + `&start=${formattedDate}&end=${getShabbatRangeEnd(date)}`;
  if (postfix.startsWith("latitude=")) {
    url2 += "&tzid=Asia/Jerusalem";
  }
  const url3 = `https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&min=on&o=on&dw=on&yyomi=on&yys=on&dr1=on&dr3=on&dsm=on&dksa=on&ahsy=on&dshl=on&dcc=on&dpa=on&start=` + formattedDate + `&end=` + formattedDate;
  document.getElementById("fast").innerHTML = "";
  document.getElementById("special").innerHTML = "";
  document.getElementById("roshchodesh").innerHTML = "";
  haftarahConnectionUrl = "";
  setHaftarahConnectionButtonState(false, "\u05D8\u05D5\u05E2\u05DF \u05D0\u05EA \u05E0\u05EA\u05D5\u05E0\u05D9 \u05D4\u05E9\u05D1\u05EA...");
  try {
    const [resp1, resp2, resp3] = await Promise.all([
      (await fetch(url, { headers: { "Accept": "application/json" } })).json(),
      (await fetch(url2, { headers: { "Accept": "application/json" } })).json(),
      (await fetch(url3, { headers: { "Accept": "application/json" } })).json()
    ]);
    try {
      const data = resp1;
      if (data.error) {
        alert("Error in zmanim data: " + data.error);
        return;
      }
      document.getElementById("sunrise").innerHTML = trim(data.times.sunrise.split("T")[1].substring(0, 5));
      document.getElementById("sunset").innerHTML = trim(data.times.sunset.split("T")[1].substring(0, 5));
      document.getElementById("foundLoc").innerHTML = data.location.title;
      document.getElementById("sunrise").onclick = function() {
        alert("chatzot Night - \u05D7\u05E6\u05D5\u05EA \u05D4\u05DC\u05D9\u05DC\u05D4: " + trim(data.times.chatzotNight.split("T")[1].substring(0, 5)) + "\nalot HaShachar - \u05E2\u05DC\u05D5\u05EA \u05D4\u05E9\u05D7\u05E8: " + trim(data.times.alotHaShachar.split("T")[1].substring(0, 5)) + "\ndawn: " + trim(data.times.dawn.split("T")[1].substring(0, 5)) + "\nsof Zman Shma MGA: " + trim(data.times.sofZmanShmaMGA.split("T")[1].substring(0, 5)) + "\nsof Zman Shma: " + trim(data.times.sofZmanShma.split("T")[1].substring(0, 5)) + "\nsof Zman Tfilla MGA: " + trim(data.times.sofZmanTfillaMGA.split("T")[1].substring(0, 5)) + "\nsof Zman Tfilla: " + trim(data.times.sofZmanTfilla.split("T")[1].substring(0, 5)) + "\nchatzot - \u05D7\u05E6\u05D5\u05EA \u05D4\u05D9\u05D5\u05DD: " + trim(data.times.chatzot.split("T")[1].substring(0, 5)));
      };
      document.getElementById("sunset").onclick = function() {
        alert("mincha Gedola -  \u05DE\u05E0\u05D7\u05D4 \u05D2\u05D3\u05D5\u05DC\u05D4: " + trim(data.times.minchaGedola.split("T")[1].substring(0, 5)) + "\nmincha Ketana -  \u05DE\u05E0\u05D7\u05D4 \u05E7\u05D8\u05E0\u05D4: " + trim(data.times.minchaKetana.split("T")[1].substring(0, 5)) + "\nplag HaMincha -  \u05E4\u05DC\u05D2 \u05D4\u05DE\u05E0\u05D7\u05D4: " + trim(data.times.plagHaMincha.split("T")[1].substring(0, 5)) + "\nbein HaShmashos - \u05D1\u05D9\u05DF \u05D4\u05E9\u05DE\u05E9\u05D5\u05EA : " + trim(data.times.beinHaShmashos.split("T")[1].substring(0, 5)) + "\nDusk -  \u05D7\u05E9\u05D9\u05DB\u05D4: " + trim(data.times.dusk.split("T")[1].substring(0, 5)) + "\nTzeit - \u05E6\u05D0\u05EA \u05D4\u05DB\u05D5\u05DB\u05D1\u05D9\u05DD: " + trim(data.times.tzeit7083deg.split("T")[1].substring(0, 5)) + "\nTzeit 72' - \u05E6\u05D0\u05EA \u05D4\u05DB\u05D5\u05DB\u05D1\u05D9\u05DD \u05E8\u05D1\u05D9\u05E0\u05D5 \u05EA\u05DD: " + trim(data.times.tzeit72min.split("T")[1].substring(0, 5)));
      };
      const now = /* @__PURE__ */ new Date();
      const hours = now.getHours();
      const minutes = now.getMinutes();
      let hhmm = data.times.sunset.split("T")[1].substring(0, 5).split(":");
      if (hours > parseInt(hhmm[0]) || hours === parseInt(hhmm[0]) && minutes >= parseInt(hhmm[1])) {
        let hdateStr = new Date(Date.now() + 864e5).toLocaleString("he", { calendar: "hebrew" });
        let hdat = hdateStr.split(",")[0];
        let hebyy = parseInt(hdateStr.split(" ")[2]);
        hdat = hdat.substr(0, hdat.lastIndexOf(" "));
        document.getElementById("hdat").innerHTML = " \u05D4\u05E2\u05E8\u05D1 \u05D0\u05D5\u05E8 \u05DC- " + no2gim(parseInt(hdat.split(" ")[0])) + " " + hdat.split(" ")[1] + " " + getYY(hebyy);
      }
    } catch (error) {
      alert("Error fetching zmanim data " + error);
    }
    try {
      const data = resp2;
      if (data.error) {
        setHaftarahConnectionButtonState(false, "\u05DC\u05D0 \u05D4\u05E6\u05DC\u05D7\u05E0\u05D5 \u05DC\u05D8\u05E2\u05D5\u05DF \u05D0\u05EA \u05E0\u05EA\u05D5\u05E0\u05D9 \u05D4\u05E9\u05D1\u05EA.");
        alert("Error in Shabbat data: " + data.error);
        return;
      }
      let ttip2 = "";
      let shabbatExists = false;
      let yomTovExists = false;
      let yomTovName = "";
      let majorHolidayOnNextSaturday = null;
      const nextSaturdayDate = getNextSaturdayDate();
      const todayDate = getLocalDateString();
      let days = "\u05E8\u05D0\u05E9\u05D5\u05DF,\u05E9\u05E0\u05D9,\u05E9\u05DC\u05D9\u05E9\u05D9,\u05E8\u05D1\u05D9\u05E2\u05D9,\u05D7\u05DE\u05D9\u05E9\u05D9,\u05E9\u05D9\u05E9\u05D9,\u05E9\u05D1\u05EA";
      for (let i = 0; i < data.items.length; i++) {
        const item = data.items[i];
        const isDailyCalendarItem = item.category === "holiday" || item.category === "roshchodesh" || item.title === "Fast begins" || item.title === "Fast ends";
        if (isDailyCalendarItem && item.date.substring(0, 10) < todayDate) {
          continue;
        }
        if (data.items[i].category === "parashat") {
          document.getElementById("parasha").innerHTML = data.items[i].hebrew;
          setHaftarahConnectionParasha(data.items[i].hebrew);
          document.getElementById("haftarahUrl").innerHTML = "\u05D4\u05E4\u05D8\u05E8\u05D4: ";
          document.getElementById("haftarah").innerHTML = convertEng(data.items[i].leyning.haftarah.replaceAll("|", " <br>")).replace("Shabbat Machar Chodesh", "\u05E9\u05D1\u05EA \u05DE\u05D7\u05E8 \u05D7\u05D5\u05D3\u05E9");
          document.getElementById("haftarahUrl").href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah.split(":")[0];
          if (data.items[i].leyning.haftarah_sephardic) {
            document.getElementById("haftarahSUrl").innerHTML = "\u05D4\u05E4\u05D8\u05E8\u05D4 \u05E1\u05E4\u05E8\u05D3\u05D9\u05DD: ";
            document.getElementById("haftarahS").innerHTML = convertEng(data.items[i].leyning.haftarah_sephardic.replaceAll("|", " <br>"));
            document.getElementById("haftarahSUrl").href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah_sephardic.split(":")[0];
          }
          let parashaUrl = "https://he.wikipedia.org/wiki/";
          let parasha2Url = "";
          if (data.items[i].hebrew.includes("-")) {
            document.getElementById("parasha").innerHTML = data.items[i].hebrew.split("-")[0] + "<br>";
            parashaUrl += data.items[i].hebrew.split("-")[0];
            parasha2Url = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.split("-")[1];
            document.getElementById("parasha2").innerHTML = "\u05E4\u05E8\u05E9\u05EA " + data.items[i].hebrew.split("-")[1];
          } else if (data.items[i].hebrew.includes("\u05E4\u05E8\u05E9\u05EA \u05DB\u05D9\u05BE\u05EA\u05E6\u05D0") || data.items[i].hebrew.includes("\u05E4\u05E8\u05E9\u05EA \u05DB\u05D9\u05BE\u05EA\u05D1\u05D5\u05D0") || data.items[i].hebrew.includes("\u05E4\u05E8\u05E9\u05EA \u05E9\u05DC\u05D7\u05BE\u05DC\u05DA") || data.items[i].hebrew.includes("\u05E4\u05E8\u05E9\u05EA \u05DC\u05DA\u05BE\u05DC\u05DA")) {
            parashaUrl += data.items[i].hebrew.replace("\u05BE", "_");
          } else if (data.items[i].hebrew.includes("\u05BE")) {
            document.getElementById("parasha").innerHTML = data.items[i].hebrew.split("\u05BE")[0] + "<br>";
            parashaUrl += data.items[i].hebrew.split("\u05BE")[0];
            parasha2Url = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.split("\u05BE")[1];
            document.getElementById("parasha2").innerHTML = "\u05E4\u05E8\u05E9\u05EA " + data.items[i].hebrew.split("\u05BE")[1];
          } else {
            parashaUrl += data.items[i].hebrew;
          }
          document.getElementById("parashaUrl").href = parashaUrl;
          if (parasha2Url != "") {
            document.getElementById("parasha2Url").href = parasha2Url;
          }
          shabbatExists = true;
        } else if (data.items[i].category === "havdalah") {
          if (document.getElementById("havdala").innerHTML === "") {
            document.getElementById("havdala").innerHTML = formatShabbatTime(data.items[i], true);
          } else {
            document.getElementById("havdala").innerHTML += "/ " + formatShabbatTime(data.items[i], false);
          }
        } else if (data.items[i].category === "candles") {
          if (document.getElementById("lighting").innerHTML === "") {
            document.getElementById("lighting").innerHTML = formatShabbatTime(data.items[i], true);
          } else {
            document.getElementById("lighting").innerHTML += "/ " + formatShabbatTime(data.items[i], false);
          }
        } else if (data.items[i].category === "roshchodesh") {
          let parts = data.items[i].date.split("-");
          let reverseYMD = parts[2] + "/" + parts[1] + "/" + parts[0];
          document.getElementById("roshchodesh").innerHTML += data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + " " + reverseYMD + "<br><br>";
          roshchodeshDate = data.items[i].date;
          let today = (/* @__PURE__ */ new Date()).toISOString().split("T")[0];
          if (today > roshchodeshDate || (/* @__PURE__ */ new Date()).getDate() > Number(reverseYMD.split("/")[0])) {
            document.getElementById("roshchodesh").innerHTML = "";
          } else {
            document.getElementById("roshchodeshUrl").href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" \u05E8\u05D0\u05E9 \u05D7\u05D5\u05D3\u05E9 ".length - 1).replace("\u05E1\u05D9\u05D5\u05DF", "\u05E1\u05D9\u05D5\u05D5\u05DF") + (data.items[i].hebrew.includes("\u05E9\u05D1\u05D8") ? "_(\u05D7\u05D5\u05D3\u05E9)" : "");
          }
          ttip2 += data.items[i].hebrew + ": " + data.items[i].memo + "\n\n";
        } else if (data.items[i].category === "mevarchim") {
          document.getElementById("lightingM").innerHTML = // document.getElementById('lighting').innerHTML + "<br>" +
          data.items[i].hebrew + "<br> \u05D4\u05DE\u05D5\u05DC\u05D3: " + data.items[i].memo.substring(data.items[i].memo.indexOf(": ") + 2).replace("chalakim", "\u05D7\u05DC\u05E7\u05D9\u05DD").replace("and", "\u05D5-").replace("Sunday", "\u05E8\u05D0\u05E9\u05D5\u05DF").replace("Monday", "\u05E9\u05E0\u05D9").replace("Tuesday", "\u05E9\u05DC\u05D9\u05E9\u05D9").replace("Wednesday", "\u05E8\u05D1\u05D9\u05E2\u05D9").replace("Thursday", "\u05D7\u05DE\u05D9\u05E9\u05D9").replace("Friday", "\u05E9\u05D9\u05E9\u05D9").replace("Saturday", "\u05E9\u05D1\u05EA") + "<br>";
          document.getElementById("lightingUrl").href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" \u05DE\u05D1\u05E8\u05DB\u05D9\u05DD \u05D7\u05D5\u05D3\u05E9 ".length - 1).replace("\u05E1\u05D9\u05D5\u05DF", "\u05E1\u05D9\u05D5\u05D5\u05DF") + (data.items[i].hebrew.includes("\u05E9\u05D1\u05D8") ? "_(\u05D7\u05D5\u05D3\u05E9)" : "");
        } else if (data.items[i].title == "Fast begins") {
          if (document.getElementById("fast").innerHTML.indexOf("\u05E1\u05E4\u05D9\u05E8\u05EA \u05D4\u05E2\u05D5\u05DE\u05E8") < 0) {
            document.getElementById("fast").innerHTML += " \u05E2\u05DC\u05D5\u05EA \u05D4\u05E9\u05D7\u05E8 " + data.items[i].date.split("T")[1].substring(0, 5) + "<br>";
          }
        } else if (data.items[i].title == "Fast ends") {
          document.getElementById("fast").innerHTML += " \u05E6\u05D0\u05EA \u05D4\u05DB\u05D5\u05DB\u05D1\u05D9\u05DD " + data.items[i].date.split("T")[1].substring(0, 5) + " <br><br> ";
          fastDate = data.items[i].date.split("T")[0];
          let today = (/* @__PURE__ */ new Date()).toISOString().split("T")[0];
          if (today > fastDate) {
            document.getElementById("fast").innerHTML = "";
          }
        } else if (data.items[i].category == "holiday") {
          if (data.items[i].subcat === "major" && data.items[i].date.substring(0, 10) === nextSaturdayDate) {
            majorHolidayOnNextSaturday = data.items[i];
          }
          fastDate = data.items[i].date;
          let today = (/* @__PURE__ */ new Date()).toISOString().split("T")[0];
          if (today <= fastDate) {
            let d = new Date(data.items[i].date);
            if (data.items[i].subcat == "fast") {
              document.getElementById("fast").innerHTML = d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear() + " " + data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + "<br>" + document.getElementById("fast").innerHTML;
            } else {
              document.getElementById("special").innerHTML += d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear() + " " + data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + "<br><br>";
            }
            if (ttip2.indexOf(data.items[i].memo) < 0) {
              ttip2 += data.items[i].hebrew + ": " + data.items[i].memo + "\n\n";
            }
          }
          if (data.items[i].yomtov && data.items[i].yomtov === true) {
            yomTovExists = true;
            if (!yomTovName) yomTovName = data.items[i].hebrew;
          }
        }
      }
      if (!shabbatExists) {
        document.getElementById("shabbathExists").innerText = "";
        if (majorHolidayOnNextSaturday) {
          const holidayDisplay = majorHolidayOnNextSaturday.yomtov ? "\u05D9\u05D5\u05DD \u05D8\u05D5\u05D1 " + majorHolidayOnNextSaturday.hebrew : majorHolidayOnNextSaturday.hebrew;
          const leyning = majorHolidayOnNextSaturday.leyning || {};
          const firstReading = leyning["1"];
          const haftarah = leyning.haftarah;
          document.getElementById("parasha").innerHTML = firstReading ? holidayDisplay + "<br>" + convertEng(firstReading.replaceAll("|", " <br>")) : holidayDisplay;
          document.getElementById("parasha2").innerHTML = "";
          document.getElementById("parashaUrl").removeAttribute("href");
          document.getElementById("parasha2Url").removeAttribute("href");
          if (haftarah) {
            document.getElementById("haftarahUrl").innerHTML = "\u05D4\u05E4\u05D8\u05E8\u05D4: ";
            document.getElementById("haftarah").innerHTML = convertEng(haftarah.replaceAll("|", " <br>"));
            document.getElementById("haftarahUrl").href = "https://shahart.github.io/heb-bible/index.html?b=" + haftarah.split(":")[0];
          }
        }
        const reason = majorHolidayOnNextSaturday ? "\u05D4\u05E9\u05D1\u05EA \u05D7\u05DC " + majorHolidayOnNextSaturday.hebrew + ", \u05D5\u05DC\u05DB\u05DF \u05D0\u05D9\u05DF \u05E4\u05E8\u05E9\u05EA \u05E9\u05D1\u05D5\u05E2 \u05E8\u05D2\u05D9\u05DC\u05D4." : yomTovName ? "\u05D4\u05E9\u05D1\u05EA \u05D7\u05DC " + yomTovName + ", \u05D5\u05DC\u05DB\u05DF \u05D0\u05D9\u05DF \u05E4\u05E8\u05E9\u05EA \u05E9\u05D1\u05D5\u05E2 \u05E8\u05D2\u05D9\u05DC\u05D4." : "\u05D0\u05D9\u05DF \u05E4\u05E8\u05E9\u05EA \u05E9\u05D1\u05D5\u05E2 \u05E8\u05D2\u05D9\u05DC\u05D4 \u05D1\u05E9\u05D1\u05EA \u05D4\u05E7\u05E8\u05D5\u05D1\u05D4.";
        setHaftarahConnectionButtonState(false, reason);
        if (yomTovExists) {
        }
      }
      if (ttip2 != "") {
        document.getElementById("special").onclick = function() {
          alert(ttip2);
        };
      } else {
        document.getElementById("special").onclick = function() {
        };
      }
    } catch (error) {
      setHaftarahConnectionButtonState(false, "\u05DC\u05D0 \u05D4\u05E6\u05DC\u05D7\u05E0\u05D5 \u05DC\u05D8\u05E2\u05D5\u05DF \u05D0\u05EA \u05E0\u05EA\u05D5\u05E0\u05D9 \u05D4\u05E9\u05D1\u05EA.");
      alert("Error fetching Shabbat data " + error);
    }
    try {
      document.getElementById("dafYomiUrl").href = "https://daf-yomi.com/Dafyomi_Page.aspx";
      ttip = "\u05E2\u05D5\u05D3 \u05DC\u05D9\u05DE\u05D5\u05D3\u05D9\u05DD \u05D9\u05D5\u05DE\u05D9\u05D9\u05DD:\n\n";
      for (let i = 0; i < resp3.items.length; i++) {
        if (resp3.items[i].category === "mishnayomi") {
          ttip += "\u05DE\u05E9\u05E0\u05D4 \u05D9\u05D5\u05DE\u05D9\u05EA: " + resp3.items[i].hebrew + "\n";
          ttip += '\u05E0"\u05DA \u05D9\u05D5\u05DE\u05D9 (2 \u05E4\u05E8\u05E7\u05D9\u05DD): ' + getNachYomi(date) + "\n";
        } else if (resp3.items[i].category === "nachyomi") {
          ttip += "\u05E0'\u05DA \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "dailyPsalms") {
          ttip += "\u05EA\u05D4\u05DC\u05D9\u05DD \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "tanakhYomi") {
          ttip += "\u05EA\u05E0'\u05DA \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].memo + " - " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "omer") {
          document.getElementById("fast").innerHTML = " \u05E1\u05E4\u05D9\u05E8\u05EA \u05D4\u05E2\u05D5\u05DE\u05E8 (\u05D1\u05D1\u05D5\u05E7\u05E8): " + resp3.items[i].hebrew.replace("\u05E2\u05D5\u05DE\u05E8", "") + "<br><br>" + document.getElementById("fast").innerHTML;
          document.getElementById("fast").style.textDecoration = "underline";
          document.getElementById("fast").style.color = "blue";
          document.getElementById("fast").onclick = function() {
            window.open(resp3.items[i].link, "_blank");
          };
        } else if (resp3.items[i].category === "holiday" && resp3.items[i].subcat === "minor" && resp3.items[i].title == "Leil Selichot") {
          document.getElementById("fast").innerHTML = "\u05DC\u05D9\u05DC \u05E1\u05DC\u05D9\u05D7\u05D5\u05EA \u05D0\u05E9\u05DB\u05E0\u05D6/ \u05E1\u05E4\u05E8\u05D3 " + formattedDate;
        } else if (resp3.items[i].category === "dafyomi") {
          document.getElementById("dafYomi").innerHTML = resp3.items[i].hebrew;
        } else if (resp3.items[i].category === "dailyRambam1") {
          ttip += "\u05E8\u05DE\u05D1\u05F4\u05DD \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "dailyRambam3") {
          ttip += "\u05E8\u05DE\u05D1\u05F4\u05DD \u05D9\u05D5\u05DE\u05D9 3 \u05E4\u05E8\u05E7\u05D9\u05DD: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "seferHaMitzvot") {
          ttip += "\u05E1\u05E4\u05E8 \u05D4\u05DE\u05E6\u05D5\u05D5\u05EA: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "yerushalmi" && resp3.items[i].subcat === "vilna") {
          ttip += "\u05D9\u05E8\u05D5\u05E9\u05DC\u05DE\u05D9 \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "shemiratHaLashon") {
          ttip += "\u05E9\u05DE\u05D9\u05E8\u05EA \u05D4\u05DC\u05E9\u05D5\u05DF \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "chofetzChaim") {
          ttip += "\u05D4\u05D7\u05E4\u05E5 \u05D7\u05D9\u05D9\u05DD \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "arukhHaShulchanYomi") {
          ttip += "\u05E2\u05E8\u05D5\u05DA \u05D4\u05E9\u05D5\u05DC\u05D7\u05DF \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        } else if (resp3.items[i].category === "kitzurShulchanAruch") {
          ttip += "\u05E7\u05D9\u05E6\u05D5\u05E8 \u05E9\u05D5\u05DC\u05D7\u05DF \u05E2\u05E8\u05D5\u05DA \u05D9\u05D5\u05DE\u05D9: " + resp3.items[i].hebrew + "\n";
        }
      }
      document.getElementById("dafYomi").onclick = function() {
        alert(ttip);
      };
      ttip += "\n";
      ttip += "\u05D7\u05D5\u05D1\u05D5\u05EA \u05D4\u05DC\u05D1\u05D1\u05D5\u05EA \u05D9\u05D5\u05DE\u05D9: TODO\n";
      ttip += "\u05DE\u05E9\u05E0\u05D4 \u05D1\u05E8\u05D5\u05E8\u05D4 \u05E2\u05DE\u05D5\u05D3 \u05D9\u05D5\u05DE\u05D9: TODO\n";
      ttip += "\u05D3\u05E3 \u05D1\u05D4\u05DC\u05DB\u05D4 \u05D3\u05E8\u05E9\u05D5 \u05D9\u05D5\u05DE\u05D9: TODO\n";
      ttip += "\u05E2\u05DE\u05D5\u05D3 \u05D1\u05D4\u05DC\u05DB\u05D4 \u05D3\u05E8\u05E9\u05D5 \u05D9\u05D5\u05DE\u05D9: TODO\n";
    } catch (error) {
      alert("Error fetching DafYomi data " + error);
    }
  } catch (error) {
    setHaftarahConnectionButtonState(false, "\u05DC\u05D0 \u05D4\u05E6\u05DC\u05D7\u05E0\u05D5 \u05DC\u05D8\u05E2\u05D5\u05DF \u05D0\u05EA \u05E0\u05EA\u05D5\u05E0\u05D9 \u05D4\u05E9\u05D1\u05EA.");
    alert("General error fetching data from " + url + " >> " + error);
  }
}
function getLoc() {
  if (navigator.geolocation) {
    alert("\u05D4\u05D5\u05E1\u05E3 ue, \u05E2\u05D1\u05D5\u05E8 do not use elevation \u05D1\u05D7\u05D9\u05E9\u05D5\u05D1\u05D9\u05DD - \u05DB\u05DC\u05D5\u05DE\u05E8 \u05E9\u05E7\u05D9\u05E2\u05D4 \u05D4\u05DE\u05D9\u05E9\u05D5\u05E8\u05D9\u05EA. \u05D0\u05D7\u05E8\u05EA, \u05EA\u05D5\u05E6\u05D2 \u05D4\u05E9\u05E7\u05D9\u05E2\u05D4 \u05D4\u05E0\u05E8\u05D0\u05D9\u05EA");
    navigator.geolocation.getCurrentPosition(function(position) {
      document.getElementById("otherLocation").value = position.coords.latitude.toFixed(2) + ", " + position.coords.longitude.toFixed(2);
      document.getElementById("otherLocation").style.display = "block";
      document.getElementById("locationSelect").value = "other";
      calc();
    });
  } else {
    alert("GeoLocation is not supported by this browser.");
  }
}
