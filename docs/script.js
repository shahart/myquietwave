function saveInput(cname, cvalue) {
    if (typeof (Storage) !== "undefined") {
        // ~5M max
        localStorage.setItem(cname, cvalue);
    } 
    else
    {
        // 4K
        const d = new Date();
        let expireInDays = 399;
        d.setTime(d.getTime() + (expireInDays * 24 * 60 * 60 * 1000));
        let expires = "expires=" + d.toUTCString();
        var myCookieValue = cvalue;
        document.cookie = cname + "=" + myCookieValue + ";" + expires + ";path=/";
    }
}

function loadInput(cname) {
    if (typeof (Storage) !== "undefined") {
        let res = localStorage.getItem(cname);
        return res || "";
    } 
    else 
    {
        let name = cname + "=";
        let decodedCookie = document.cookie;
        let ca = decodedCookie.split(';');
        for (let c of ca) {
            while (c.charAt(0) === ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) === 0) {
                return c.substring(name.length, c.length).split('\\').join('\n');
            }
        }
        return "";
    }
}

function no2gim(input) {
    const letters = ["ל\"","כ\"","י\"","ט","ח","ז","ו","ה","ד","ג","ב","א"];
    const values = [30,20,10,9,8,7,6,5,4,3,2,1];
    let output = "";
    while (input > 0) {
        for (let i = 0; i < letters.length; ++i) {
            if (input == 16) {
                return output + "ט\"ז";
            }
            if (input == 15) {
                return output + "ט\"ו";
            }
            if (input >= values[i]) {
                input -= values[i];
                output += letters[i];
                break;
            }
        }
    }
    if (output.endsWith("\""))
        output = output.slice(0, -1);
    return output;
}

function getYY(no) {
    let input = no
    const letters = ["ה'","ד'","ג'","ב'","א'","ת","ש","ר","ק","צ","פ","ע","ס","נ","מ","ל","כ","י","ט","ח","ז","ו","ה","ד","ג","ב","א"]
    const values = [5000,4000,3000,2000,1000,400,300,200,100,90,80,70,60,50,40,30,20,10,9,8,7,6,5,4,3,2,1]
    let output = "";
    while (input > 0) {
        for (let i = 0; i < letters.length; i++) {
            if (input == 16) {
                return output + "טז"
            }
            if (input == 15) {
                return output + "טו"
            }
            if (input >= values[i]) {
                input -= values[i]
                output += letters[i]
                break
            }
        }
    }
    return output
}

let hdateStr = (new Date).toLocaleString('he',{calendar:"hebrew"});
let hdat = hdateStr.split(',')[0];
let hebyy = parseInt(hdateStr.split(" ")[2]);
hdat = hdat.substr(0, hdat.lastIndexOf(' '));
document.getElementById('hdat').innerHTML = 'היום ' + no2gim(parseInt(hdat.split(' ')[0])) + ' ' + hdat.split(' ')[1] + ' ' + getYY(hebyy);

async function calc() {
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
        postfix = "city=" + postfix;
    }
    const url = `https://www.hebcal.com/zmanim?cfg=json&` + postfix + useElevationParam; 

    const url2 = `https://www.hebcal.com/shabbat?cfg=json&` + postfix + useElevationParam;

    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Month is 0-indexed
    const day = date.getDate().toString().padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;
    const url3 = `https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&myomi=on&nyomi=on&dty=on&dps=on&start=` + formattedDate + `&end=` + formattedDate;

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
            document.getElementById('sunrise') .innerHTML = data.times.sunrise.split('T')[1].substring(0,5);
            document.getElementById('sunset')  .innerHTML = data.times.sunset.split('T')[1].substring(0,5);
            document.getElementById('foundLoc').innerHTML = data.location.title;

            document.getElementById('sunrise').onclick = function() {
                alert("chatzotNight - חצות הלילה: " + data.times.chatzotNight.split('T')[1].substring(0,5) + "\n" +
                      "alotHaShachar - עלות השחר: " + data.times.alotHaShachar.split('T')[1].substring(0,5) + "\n" +
                      "dawn: " + data.times.dawn.split('T')[1].substring(0,5) + "\n" +
                      "chatzot - חצות היום: " + data.times.chatzot.split('T')[1].substring(0,5));
            }
            document.getElementById('sunset').onclick = function() {
                alert("beinHaShmashos - בין השמשות : " + data.times.beinHaShmashos.split('T')[1].substring(0,5) + "\n" +
                      "Dusk -  חשיכה: " + data.times.dusk.split('T')[1].substring(0,5) + "\n" +
                      "Tzeit - צאת הכוכבים: " + data.times.tzeit7083deg.split('T')[1].substring(0,5));
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
                alert("Error in Shabbat data: " + data.error);
                return;
            }
            let ttip = '';
            document.getElementById('special').innerHTML = '';
            document.getElementById('roshchodesh').innerHTML = '';
            document.getElementById('fast').innerHTML = '';
            let days = "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת";
            for (let i = 0; i < data.items.length; i++) {
                if (data.items[i].category === 'parashat') {
                    document.getElementById('parasha').innerHTML = data.items[i].hebrew;
                    document.getElementById('haftarahUrl').innerHTML = 'הפטרה: ';
                    document.getElementById('haftarah').innerHTML = data.items[i].leyning.haftarah.replaceAll('|', ' <br>');
                    document.getElementById('haftarahUrl').href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah.split(':')[0];
                    if (data.items[i].leyning.haftarah_sephardic) {
                        document.getElementById('haftarahSUrl').innerHTML = 'הפטרה ספרדים: ';
                        document.getElementById('haftarahS').innerHTML = data.items[i].leyning.haftarah_sephardic.replaceAll('|', ' <br>');
                        document.getElementById('haftarahSUrl').href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah_sephardic.split(':')[0];
                    }
                    let parashaUrl = "https://he.wikipedia.org/wiki/";
                    if (data.items[i].hebrew.includes("-"))
                        parashaUrl += data.items[i].hebrew.split("-")[0];
                    else 
                        parashaUrl += data.items[i].hebrew;
                    document.getElementById('parashaUrl').href = parashaUrl;
                }
                else if (data.items[i].category === 'havdalah') {
                    document.getElementById('havdala').innerHTML = data.items[i].hebrew + " " + data.items[i].date.split('T')[1].substring(0,5);
                }
                else if (data.items[i].category === 'candles') {
                    document.getElementById('lighting').innerHTML = data.items[i].hebrew + " " + data.items[i].date.split('T')[1].substring(0,5);
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
                        document.getElementById('roshchodeshUrl').href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" ראש חודש ".length-1) + (data.items[i].hebrew.includes("שבט") ? "_(חודש)" : "");
                    }
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
                    document.getElementById('lightingUrl').href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew.substring(" מברכים חודש ".length-1) + (data.items[i].hebrew.includes("שבט") ? "_(חודש)" : "");
                }
                else if (data.items[i].title == 'Fast begins') {
                    document.getElementById('fast').innerHTML += " עלות השחר " + data.items[i].date.split('T')[1].substring(0,5) + "<br>";
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
                        ttip += data.items[i].hebrew + ": " + data.items[i].memo + "\n\n";
                    }
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
            alert("Error fetching Shabbat data " + error);
        }

        try {
            document.getElementById('dafYomi').innerHTML = resp3.items[0].hebrew;
            document.getElementById('dafYomiUrl').href = "https://www.daf.today/"; // resp3.items[0].link;
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
            }
            document.getElementById('dafYomi').onclick = function() {
                alert(ttip);
            }
        } catch (error) {
            alert("Error fetching DafYomi data " + error);
        }
    }    
    catch (error) {
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

document.addEventListener('DOMContentLoaded', (event) => {
    const dropdown = document.getElementById('locationSelect');
    const otherInput = document.getElementById('otherLocation');

    dropdown.addEventListener('change', function() {
        if (this.value === 'other') {
            alert('הוסף ue, עבור do not use elevation בחישובים - כלומר שקיעה המישורית. אחרת, תוצג השקיעה הנראית');
            otherInput.style.display = 'block';
            otherInput.focus(); 
        } else {
            otherInput.style.display = 'none';
            otherInput.value = '';
        }
    });  

    document.getElementById("todo").addEventListener("focusout", function () {
        saveInput("todo", document.getElementById('todo').value);
}   );
})

let cookieInput = this.loadInput("zmanim-location");
if (cookieInput !== "") {
    document.getElementById('locationSelect').value = cookieInput;
    if (cookieInput === 'other') {
        document.getElementById('otherLocation').style.display = 'block';
        document.getElementById('otherLocation').value = this.loadInput("zmanim-location-other");
    }
    else {
        document.getElementById('otherLocation').style.display = 'none';
        document.getElementById('otherLocation').value = '';
    }
}

let todocookieInput = this.loadInput("todo");
if (todocookieInput !== "") {
    document.getElementById('todo').value = todocookieInput;
}

calc();
    
function updateClock() {
    const now = new Date();
    const hours = now.getHours();
    const minutes = now.getMinutes();
    const seconds = now.getSeconds();
    const formattedTime = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    document.getElementById('clock').innerHTML = formattedTime + "<br>" +
        Intl.DateTimeFormat().resolvedOptions().timeZone + "<br>offset (Hours) " + 
        new Date().getTimezoneOffset() / -60;
}

updateClock();

setInterval(updateClock, 500);

let days = "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת";
let d = new Date();
document.getElementById('dat').innerHTML = d.getDate() + "/" + (d.getMonth()+1) + "/" + d.getFullYear()  + " - " + days.split(",")[d.getDay()];
