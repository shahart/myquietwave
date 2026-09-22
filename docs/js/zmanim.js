function getNextSaturdayDate(date = new Date()) {
    const nextSaturday = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    nextSaturday.setDate(nextSaturday.getDate() + (6 - nextSaturday.getDay() + 7) % 7);
    return [
        nextSaturday.getFullYear(),
        String(nextSaturday.getMonth() + 1).padStart(2, '0'),
        String(nextSaturday.getDate()).padStart(2, '0'),
    ].join('-');
}

function getLocalDateString(date = new Date()) {
    return [
        date.getFullYear(),
        String(date.getMonth() + 1).padStart(2, '0'),
        String(date.getDate()).padStart(2, '0'),
    ].join('-');
}

async function calc() {
    document.getElementById('havdala').innerHTML = '';
    document.getElementById('lighting').innerHTML = '';
    var postfix = document.getElementById('locationSelect').value;
    saveInput("zmanim-location", postfix);

    if (postfix === 'other') {
        postfix = document.getElementById('otherLocation').value.trim();
        saveInput("zmanim-location-other", document.getElementById('otherLocation').value.trim());
    }

    var useElevationParam = "&ue=on";
    if (postfix.indexOf(",ue") >= 1 ) {
        useElevationParam = "&ue=off";
        postfix = postfix.replace(",ue", "");
    }
    if (postfix.indexOf(',') > 0) {
        postfix = "latitude=" + postfix.split(',')[0].trim() + "&longitude=" + postfix.split(',')[1].trim();
    } else if (postfix.charAt(0) >= '0' && postfix.charAt(0) <= '9') {
        postfix = "geonameid=" + postfix;
    } else {
        if (postfix.toLowerCase() === "il-yavne") {
            postfix = "geonameid=293222";
        }
        else if (postfix.toLowerCase() === "il-mitzpe ramon") {
            postfix = "geonameid=294166";
        }
        else if (postfix.toLowerCase() === "il-modiin ilit") {
            postfix = "geonameid=8199378";
        }
        else if (postfix.toLowerCase() === "il-betar ilit") {
            postfix = "geonameid=284375";
        }
        else if (postfix.toLowerCase() === "il-zefat") {
            postfix = "geonameid=293100";
        }
        else {
            postfix = "city=" + postfix;
        }
    }
    var url = `https://www.hebcal.com/zmanim?cfg=json&` + postfix + useElevationParam;
    if (postfix.startsWith('latitude=')) {
        url += "&tzid=Asia/Jerusalem";
    }
    var url2 = `https://www.hebcal.com/shabbat?cfg=json&` + postfix + useElevationParam;
    if (postfix.startsWith('latitude=')) {
        url2 += "&tzid=Asia/Jerusalem";
    }

    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Month is 0-indexed
    const day = date.getDate().toString().padStart(2, '0');
    var formattedDate = `${year}-${month}-${day}`;
    const url3 = `https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&min=on&o=on&start=` + formattedDate + `&end=` + formattedDate;

    document.getElementById('fast').innerHTML = '';
    document.getElementById('special').innerHTML = '';
    document.getElementById('roshchodesh').innerHTML = '';
    haftarahConnectionUrl = '';
    setHaftarahConnectionButtonState(false, 'טוען את נתוני השבת...');

    try {
        const [resp1, resp2, resp3] = await Promise.all([
            (await fetch(url,  { headers: { 'Accept': 'application/json' } })).json(),
            (await fetch(url2, { headers: { 'Accept': 'application/json' } })).json(),
            (await fetch(url3, { headers: { 'Accept': 'application/json' } })).json()
        ]);

        try {
            const data = resp1;
            if (data.error) {
                alert("Error in zmanim data: " + data.error);
                return;
            }
            document.getElementById('sunrise') .innerHTML = trim(data.times.sunrise.split('T')[1].substring(0,5));
            document.getElementById('sunset')  .innerHTML = trim(data.times.sunset.split('T')[1].substring(0,5));
            document.getElementById('foundLoc').innerHTML = data.location.title;

            document.getElementById('sunrise').onclick = function() {
                alert("chatzot Night - חצות הלילה: " + trim(data.times.chatzotNight.split('T')[1].substring(0,5)) + "\n" +
                      "alot HaShachar - עלות השחר: " + trim(data.times.alotHaShachar.split('T')[1].substring(0,5)) + "\n" +
                      "dawn: " + trim(data.times.dawn.split('T')[1].substring(0,5)) + "\n" +
                      "sof Zman Shma MGA: " + trim(data.times.sofZmanShmaMGA.split('T')[1].substring(0,5)) + "\n" +
                      "sof Zman Shma: " + trim(data.times.sofZmanShma.split('T')[1].substring(0,5)) + "\n" +
                      "sof Zman Tfilla MGA: " + trim(data.times.sofZmanTfillaMGA.split('T')[1].substring(0,5)) + "\n" +
                      "sof Zman Tfilla: " + trim(data.times.sofZmanTfilla.split('T')[1].substring(0,5)) + "\n" +
                      "chatzot - חצות היום: " + trim(data.times.chatzot.split('T')[1].substring(0,5)));
            }
            document.getElementById('sunset').onclick = function() {
                //
                alert("mincha Gedola -  מנחה גדולה: " + trim(data.times.minchaGedola.split('T')[1].substring(0,5) )+ "\n" +
                      "mincha Ketana -  מנחה קטנה: " + trim(data.times.minchaKetana.split('T')[1].substring(0,5)) + "\n" +
                      "plag HaMincha -  פלג המנחה: " + trim(data.times.plagHaMincha.split('T')[1].substring(0,5)) + "\n" +
                      "bein HaShmashos - בין השמשות : " + trim(data.times.beinHaShmashos.split('T')[1].substring(0,5)) + "\n" +
                      "Dusk -  חשיכה: " + trim(data.times.dusk.split('T')[1].substring(0,5)) + "\n" +
                      "Tzeit - צאת הכוכבים: " + trim(data.times.tzeit7083deg.split('T')[1].substring(0,5)) + "\n" +
                      "Tzeit 72' - צאת הכוכבים רבינו תם: " + trim(data.times.tzeit72min.split('T')[1].substring(0,5)));
                    }

            const now = new Date();
            const hours = now.getHours();
            const minutes = now.getMinutes();

            let hhmm = data.times.sunset.split('T')[1].substring(0,5).split(':');
            if (hours > parseInt(hhmm[0]) || (hours === parseInt(hhmm[0]) && minutes >= parseInt(hhmm[1]))) {
                let hdateStr = (new Date(Date.now()+ 86400000)).toLocaleString('he',{calendar:"hebrew"});
                let hdat = hdateStr.split(',')[0];
                let hebyy = parseInt(hdateStr.split(" ")[2]);
                hdat = hdat.substr(0, hdat.lastIndexOf(' '));
                document.getElementById('hdat').innerHTML = ' הערב אור ל- ' + no2gim(parseInt(hdat.split(' ')[0])) + ' ' + hdat.split(' ')[1] + ' ' + getYY(hebyy);
            }

        } catch (error) {
            alert("Error fetching zmanim data " + error);
        }

        try {
            const data = resp2;
            if (data.error) {
                setHaftarahConnectionButtonState(false, 'לא הצלחנו לטעון את נתוני השבת.');
                alert("Error in Shabbat data: " + data.error);
                return;
            }
            let ttip = '';
            let shabbatExists = false;
            let yomTovExists = false;
            let yomTovName = '';
            let majorHolidayOnNextSaturday = null;
            const nextSaturdayDate = getNextSaturdayDate();
            const todayDate = getLocalDateString();
            let days = "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת";
            for (let i = 0; i < data.items.length; i++) {
                const item = data.items[i];
                const isDailyCalendarItem = item.category === 'holiday' ||
                    item.category === 'roshchodesh' ||
                    item.title === 'Fast begins' || item.title === 'Fast ends';
                if (isDailyCalendarItem && item.date.substring(0, 10) < todayDate) {
                    continue;
                }
                if (data.items[i].category === 'parashat') {
                    document.getElementById('parasha').innerHTML = data.items[i].hebrew;
                    setHaftarahConnectionParasha(data.items[i].hebrew);
                    document.getElementById('haftarahUrl').innerHTML = 'הפטרה: ';
                    document.getElementById('haftarah').innerHTML = convertEng(data.items[i].leyning.haftarah.replaceAll('|', ' <br>')); // for example: "Pinchas occurring after 17 Tammuz"
                    document.getElementById('haftarahUrl').href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah.split(':')[0];
                    if (data.items[i].leyning.haftarah_sephardic) {
                        document.getElementById('haftarahSUrl').innerHTML = 'הפטרה ספרדים: ';
                        document.getElementById('haftarahS').innerHTML = convertEng(data.items[i].leyning.haftarah_sephardic.replaceAll('|', ' <br>'));
                        document.getElementById('haftarahSUrl').href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah_sephardic.split(':')[0];
                    }
                    let parashaUrl = "https://he.wikipedia.org/wiki/";
                    let parasha2Url = "";
                    if (data.items[i].hebrew.includes("-")) {
                        document.getElementById('parasha').innerHTML = data.items[i].hebrew.split("-")[0] + "<br>";
                        parashaUrl += data.items[i].hebrew.split("-")[0];
                        parasha2Url = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.split("-")[1];
                        document.getElementById('parasha2').innerHTML = 'פרשת ' + data.items[i].hebrew.split("-")[1];
                    }
                    // acc. to https://github.com/hebcal/hebcal-swift/blob/main/Sources/Hebcal/Translations.swift
                    else if (data.items[i].hebrew.includes("כי־תצא") ||
                        data.items[i].hebrew.includes("כי־תבוא") ||
                        data.items[i].hebrew.includes("שלח־לך") ||
                        data.items[i].hebrew.includes("לך־לך")) {
                        parashaUrl += data.items[i].hebrew.replace("־", "_");
                    }
                    else if (data.items[i].hebrew.includes("־")) {
                        document.getElementById('parasha').innerHTML = data.items[i].hebrew.split("־")[0] + "<br>";
                        parashaUrl += data.items[i].hebrew.split("־")[0];
                        parasha2Url = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.split("־")[1];
                        document.getElementById('parasha2').innerHTML = 'פרשת ' + data.items[i].hebrew.split("־")[1];
                    }
                    else {
                        parashaUrl += data.items[i].hebrew;
                    }
                    document.getElementById('parashaUrl').href = parashaUrl;
                    if (parasha2Url != "") {
                        document.getElementById('parasha2Url').href = parasha2Url;
                    }
                    shabbatExists = true;
                }
                else if (data.items[i].category === 'havdalah') {
                    if (new Date(data.items[i].date) <= new Date()) {
                        continue;
                    }
                    if (document.getElementById('havdala').innerHTML === '') {
                        document.getElementById('havdala').innerHTML = data.items[i].hebrew + " " + data.items[i].date.split('T')[1].substring(0,5);
                    }
                    else {
                        document.getElementById('havdala').innerHTML += "/ " + data.items[i].date.split('T')[1].substring(0,5);
                    }
                }
                else if (data.items[i].category === 'candles') {
                    if (document.getElementById('lighting').innerHTML === '') {
                        document.getElementById('lighting').innerHTML = data.items[i].hebrew + " " + data.items[i].date.split('T')[1].substring(0,5);
                    }
                    else {
                        document.getElementById('lighting').innerHTML += "/ " + data.items[i].date.split('T')[1].substring(0,5);
                    }
                }
                else if (data.items[i].category === 'roshchodesh') {
                    let parts = data.items[i].date.split('-');
                    let reverseYMD = parts[2] + "/" + parts[1] + "/" + parts[0];
                    document.getElementById('roshchodesh').innerHTML += data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + " " + reverseYMD + "<br><br>";
                    roshchodeshDate = data.items[i].date;
                    let today = new Date().toISOString().split('T')[0];
                    if (today > roshchodeshDate || new Date().getDate() > reverseYMD.split('/')[0]) {
                        document.getElementById('roshchodesh').innerHTML = "";
                    }
                    else {
                        document.getElementById('roshchodeshUrl').href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" ראש חודש ".length-1).replace("סיון", "סיוון") + (data.items[i].hebrew.includes("שבט") ? "_(חודש)" : "");
                    }
                    ttip += data.items[i].hebrew + ": " + data.items[i].memo + "\n\n";
                }
                else if (data.items[i].category === 'mevarchim') {
                    document.getElementById('lightingM').innerHTML =
                        // document.getElementById('lighting').innerHTML + "<br>" +
                        data.items[i].hebrew + "<br> המולד: " +
                        data.items[i].memo.
                            substring(data.items[i].memo.indexOf(": ") + 2).
                            replace("chalakim", "חלקים").
                            replace("and", "ו-").
                            // replace("Molad", "מולד").
                            replace("Sunday", "ראשון").
                            replace("Monday", "שני").
                            replace("Tuesday", "שלישי").
                            replace("Wednesday", "רביעי").
                            replace("Thursday", "חמישי").
                            replace("Friday", "שישי").
                            replace("Saturday", "שבת") + "<br>";
                    document.getElementById('lightingUrl').href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" מברכים חודש ".length-1).replace("סיון", "סיוון") + (data.items[i].hebrew.includes("שבט") ? "_(חודש)" : "");
                }
                else if (data.items[i].title == 'Fast begins') {
                    if (document.getElementById('fast').innerHTML.indexOf("ספירת העומר") < 0) {
                        document.getElementById('fast').innerHTML += " עלות השחר " + data.items[i].date.split('T')[1].substring(0,5) + "<br>";
                    }
                }
                else if (data.items[i].title == 'Fast ends') {
                    document.getElementById('fast').innerHTML += " צאת הכוכבים " + data.items[i].date.split('T')[1].substring(0,5) + " <br><br> ";
                    fastDate = data.items[i].date.split('T')[0];
                    let today = new Date().toISOString().split('T')[0];
                    if (today > fastDate) {
                        document.getElementById('fast').innerHTML = "";
                    }
                }
                else if (data.items[i].category == 'holiday') {
                    if (data.items[i].subcat === 'major' &&
                        data.items[i].date.substring(0, 10) === nextSaturdayDate) {
                        majorHolidayOnNextSaturday = data.items[i];
                    }
                    fastDate = data.items[i].date;
                    let today = new Date().toISOString().split('T')[0];
                    if (today <= fastDate) {
                        let d = new Date(data.items[i].date);
                        if (data.items[i].subcat == 'fast') {
                            document.getElementById('fast').innerHTML = d.getDate() + "/" + (d.getMonth()+1) + "/" + d.getFullYear() + " " + data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + "<br>" + document.getElementById('fast').innerHTML;
                        }
                        else {
                            document.getElementById('special').innerHTML += d.getDate() + "/" + (d.getMonth()+1) + "/" + d.getFullYear() + " " + data.items[i].hebrew + " - " + days.split(",")[new Date(data.items[i].date).getDay()] + "<br><br>";
                        }
                        if (ttip.indexOf(data.items[i].memo) < 0) {
                            ttip += data.items[i].hebrew + ": " + data.items[i].memo + "\n\n";
                        }
                    }
                    if (data.items[i].yomtov && data.items[i].yomtov === true) {
                        yomTovExists = true;
                        if (!yomTovName) yomTovName = data.items[i].hebrew;
                    }
                }
            }
            if (! shabbatExists) {
                document.getElementById('shabbathExists').innerText = '';
                if (majorHolidayOnNextSaturday) {
                    const holidayDisplay = majorHolidayOnNextSaturday.yomtov
                        ? 'יום טוב ' + majorHolidayOnNextSaturday.hebrew
                        : majorHolidayOnNextSaturday.hebrew;
                    document.getElementById('parasha').innerHTML = holidayDisplay;
                    document.getElementById('parasha2').innerHTML = '';
                    document.getElementById('parashaUrl').removeAttribute('href');
                    document.getElementById('parasha2Url').removeAttribute('href');
                }
                const reason = majorHolidayOnNextSaturday
                    ? 'השבת חל ' + majorHolidayOnNextSaturday.hebrew + ', ולכן אין פרשת שבוע רגילה.'
                    : yomTovName
                    ? 'השבת חל ' + yomTovName + ', ולכן אין פרשת שבוע רגילה.'
                    : 'אין פרשת שבוע רגילה בשבת הקרובה.';
                setHaftarahConnectionButtonState(false, reason);
                if (yomTovExists) {
                    // document.getElementById('shabbathExists').innerText = '🕯🕯 יום טוב';
                }
            }
            if (ttip != '') {
                document.getElementById('special').onclick = function() {
                    alert(ttip);
                }
            }
            else {
                document.getElementById('special').onclick = function() {
                }
            }

        } catch (error) {
            setHaftarahConnectionButtonState(false, 'לא הצלחנו לטעון את נתוני השבת.');
            alert("Error fetching Shabbat data " + error);
        }

        try {
            document.getElementById('dafYomiUrl').href = "https://daf-yomi.com/Dafyomi_Page.aspx"; // resp3.items[0].link;
            ttip = 'עוד לימודים יומיים:\n\n';
            for (let i = 0; i < resp3.items.length; i++) {
                if (resp3.items[i].category === 'mishnayomi') {
                    ttip += "משנה יומית: " + resp3.items[i].hebrew + "\n";
                }
                else if (resp3.items[i].category === 'nachyomi') {
                    ttip += "נ'ך יומי: " + resp3.items[i].hebrew + "\n";
                }
                else if (resp3.items[i].category === 'dailyPsalms') {
                    ttip += "תהלים יומי: " + resp3.items[i].hebrew + "\n";
                }
                else if (resp3.items[i].category === 'tanakhYomi') {
                    ttip += "תנ'ך יומי: " + resp3.items[i].hebrew + "\n";
                }
                else if (resp3.items[i].category === 'omer') {
                    // ttip += "ספירת העומר (בבוקר): " + resp3.items[i].hebrew.replace("עומר", "") + "\n";
                    document.getElementById('fast').innerHTML = " ספירת העומר (בבוקר): " + resp3.items[i].hebrew.replace("עומר", "") + "<br><br>" + document.getElementById('fast').innerHTML;
                    document.getElementById('fast').style.textDecoration = "underline";
                    document.getElementById('fast').style.color = "blue";
                    document.getElementById('fast').onclick = function() {
                        window.open(resp3.items[i].link, "_blank");
                    }
                }
                else if (resp3.items[i].category === 'holiday' && resp3.items[i].subcat === "minor" && resp3.items[i].title == "Leil Selichot") {
                    document.getElementById('fast').innerHTML = "ליל סליחות אשכנז/ ספרד" + " " + formattedDate;
                }
                else if (resp3.items[i].category === 'dafyomi') {
                    document.getElementById('dafYomi').innerHTML = resp3.items[1].hebrew;
                }
            }
            document.getElementById('dafYomi').onclick = function() {
                alert(ttip);
            }
        } catch (error) {
            alert("Error fetching DafYomi data " + error);
        }
    }
    catch (error) {
        setHaftarahConnectionButtonState(false, 'לא הצלחנו לטעון את נתוני השבת.');
        alert("General error fetching data from " + url + " >> " + error);
    }
}

function getLoc() {
    if (navigator.geolocation) {
        alert('הוסף ue, עבור do not use elevation בחישובים - כלומר שקיעה המישורית. אחרת, תוצג השקיעה הנראית');
        navigator.geolocation.getCurrentPosition(function(position) {
            document.getElementById('otherLocation').value =
                position.coords.latitude.toFixed(2) + ", " +
                position.coords.longitude.toFixed(2);
            document.getElementById('otherLocation').style.display = 'block';
            document.getElementById('locationSelect').value = 'other';
            calc();
        });
    } else {
        alert("GeoLocation is not supported by this browser.");
    }
}
