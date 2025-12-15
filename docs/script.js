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

async function calc() {
    var postfix = document.getElementById('locationSelect').value;
    saveInput("zmanim-location", postfix);

    if (postfix === 'other') {
        postfix = document.getElementById('otherLocation').value.trim();
        saveInput("zmanim-location-other", document.getElementById('otherLocation').value.trim());
    }

    var useElevationParam = "&ue=off"; 
    if (postfix.indexOf(",ue") >= 1 ) {
        useElevationParam = "&ue=on";
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
    const url3 = `https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&start=` + formattedDate + `&end=` + formattedDate;

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
            document.getElementById('sunrise') .innerHTML = data.times.sunrise;
            document.getElementById('sunset')  .innerHTML = data.times.sunset;
            document.getElementById('foundLoc').innerHTML = data.location.title;
        } catch (error) {
            alert("Error fetching zmanim data " + error);
        }

        try {
            const data = resp2;
            if (data.error) {
                alert("Error in Shabbat data: " + data.error);
                return;
            }
            for (let i = 0; i < data.items.length; i++) {
                if (data.items[i].category === 'havdalah') {
                    document.getElementById('havdala').innerHTML = data.items[i].title;
                }
                else if (data.items[i].category === 'parashat') {
                    document.getElementById('parasha').innerHTML = data.items[i].hebrew;
                    document.getElementById('haftarahUrl').innerHTML = 'הפטרה: ';
                    document.getElementById('haftarah').innerHTML = data.items[i].leyning.haftarah.replaceAll('|', ' <br>');
                    document.getElementById('haftarahUrl').href = "https://shahart.github.io/heb-bible/index.html?b=" + data.items[i].leyning.haftarah.split(':')[0];
                    document.getElementById('parashaUrl').href = "https://he.wikipedia.org/wiki/" + data.items[i].hebrew;
                }
                else if (data.items[i].category === 'candles') {
                    document.getElementById('lighting').innerHTML = data.items[i].title;
                } 
                else if (data.items[i].category === 'mevarchim') {
                    document.getElementById('lighting').innerHTML = document.getElementById('lighting').innerHTML + "<br>" + data.items[i].hebrew + "<br>" + data.items[i].memo;
                } 
            }
        } catch (error) {
            alert("Error fetching Shabbat data " + error);
        }

        try {
            document.getElementById('dafYomi').innerHTML = resp3.items[0].hebrew;
            document.getElementById('dafYomiUrl').href = resp3.items[0].link;
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
        alert('הוסף ue, עבור use elevation בחישובים - כלומר שקיעה הנראית. אחרת, תוצג השקיעה המישורית');
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
            alert('הוסף ue, עבור use elevation בחישובים - כלומר שקיעה הנראית. אחרת, תוצג השקיעה המישורית');
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