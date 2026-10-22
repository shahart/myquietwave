let hdateStr = (new Date).toLocaleString('he',{calendar:"hebrew"});
let hdat = hdateStr.split(',')[0];
let hebyy = parseInt(hdateStr.split(" ")[2]);
hdat = hdat.substr(0, hdat.lastIndexOf(' '));
document.getElementById('hdat').innerHTML = 'היום 📅 ' + no2gim(parseInt(hdat.split(' ')[0])) + ' ' + hdat.split(' ')[1] + ' ' + getYY(hebyy);

function updateClock() {
    const now = new Date();
    const hours = now.getHours();
    const minutes = now.getMinutes();
    const seconds = now.getSeconds();
    const formattedTime = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    document.getElementById('clock').innerHTML = "🕰️ " + formattedTime + "<br>" +
        Intl.DateTimeFormat().resolvedOptions().timeZone + "<br>offset (Hours) " +
        new Date().getTimezoneOffset() / -60;
}

updateClock();

setInterval(updateClock, 500);

let days = "ראשון,שני,שלישי,רביעי,חמישי,שישי,שבת";
let d = new Date();
document.getElementById('dat').innerHTML = d.getDate() + "/" + (d.getMonth()+1) + "/" + d.getFullYear()  + " - " + days.split(",")[d.getDay()];


