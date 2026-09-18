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

function convertEng(hebrew) {
    hebrew = hebrew.replace("Joshua", "יהושע");
    hebrew = hebrew.replace("Judges", "שופטים");
    hebrew = hebrew.replace("II Samuel", "שמואל ב");
    hebrew = hebrew.replace("I Samuel", "שמואל א");
    hebrew = hebrew.replace("II Kings", "מלכים ב");
    hebrew = hebrew.replace("I Kings", "מלכים א");
    hebrew = hebrew.replace("Isaiah", "ישעיהו");
    hebrew = hebrew.replace("Jeremiah", "ירמיהו");
    hebrew = hebrew.replace("Ezekiel", "יחזקאל");
    hebrew = hebrew.replace("Hosea", "הושע");
    hebrew = hebrew.replace("Joel", "יואל");
    hebrew = hebrew.replace("Amos", "עמוס");
    hebrew = hebrew.replace("Obadiah", "עובדיה")
    hebrew = hebrew.replace("Jonah", "יונה");
    hebrew = hebrew.replace("Micah", "מיכה");
    hebrew = hebrew.replace("Nachum", "נחום");
    hebrew = hebrew.replace("Habakkuk", "חבקוק");
    hebrew = hebrew.replace("Zephaniah", "צפניה");
    hebrew = hebrew.replace("Haggai", "חגי");
    hebrew = hebrew.replace("Zechariah", "זכריה");
    hebrew = hebrew.replace("Malachi", "מלאכי");
    return hebrew;
}

function trim(n) {
    if (n.startsWith('0')) return n.substring(1);
    return n;
}
