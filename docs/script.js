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
    var postfix = document.getElementById('location').value;

    saveInput("zmanim-location", postfix);

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
    try {
        const response = await fetch(url, { headers: { 'Accept': 'application/json' } });
        if (!response.ok) throw new Error('Network response was not ok - is the location valid? ' + response.status);
        const data = await response.json();
        document.getElementById('sunrise') .innerHTML = data.times.sunrise;
        document.getElementById('sunset')  .innerHTML = data.times.sunset;
        document.getElementById('foundLoc').innerHTML = data.location.title;
    } catch (error) {
        alert("Error fetching zmanim data " + error);
    }

    //
    const url2 = `https://www.hebcal.com/shabbat?cfg=json&` + postfix + useElevationParam;
    try {
        const response = await fetch(url2, { headers: { 'Accept': 'application/json' } });
        if (! response.ok) throw new Error('Network response was not ok - is the location valid? ' + response.status);
        const data = await response.json();
        for (let i = 0; i < data.items.length; i++) {
            if (data.items[i].category === 'havdalah') {
                document.getElementById('havdala').innerHTML = data.items[i].title;
            }
            else if (data.items[i].category === 'parashat') {
                document.getElementById('parasha').innerHTML = data.items[i].hebrew;
            }
            else if (data.items[i].category === 'candles') {
                document.getElementById('lighting').innerHTML = data.items[i].title;
            } 
        }
    } catch (error) {
        alert("Error fetching Shabbat data " + error);
    }

    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Month is 0-indexed
    const day = date.getDate().toString().padStart(2, '0');
    const formattedDate = `${year}-${month}-${day}`;
    const url3 = `https://www.hebcal.com/hebcal?v=1&cfg=json&F=on&start=` + formattedDate + `&end=` + formattedDate;
    try {
        const response = await fetch(url3, { headers: { 'Accept': 'application/json' } });
        if (! response.ok) throw new Error('Network response was not ok - ' + response.status);
        const data = await response.json();
        document.getElementById('dafYomi').innerHTML = data.items[0].hebrew;
        document.getElementById('dafYomiUrl').href = data.items[0].link;
    } catch (error) {
        alert("Error fetching DafYomi data " + error);
    }
    
}
