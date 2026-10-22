function saveInput(cname, cvalue) {
  if (typeof Storage !== "undefined") {
    localStorage.setItem(cname, cvalue);
  } else {
    const d = /* @__PURE__ */ new Date();
    let expireInDays = 399;
    d.setTime(d.getTime() + expireInDays * 24 * 60 * 60 * 1e3);
    let expires = "expires=" + d.toUTCString();
    var myCookieValue = cvalue;
    document.cookie = cname + "=" + myCookieValue + ";" + expires + ";path=/";
  }
}
function loadInput(cname) {
  if (typeof Storage !== "undefined") {
    let res = localStorage.getItem(cname);
    return res || "";
  } else {
    let name = cname + "=";
    let decodedCookie = document.cookie;
    let ca = decodedCookie.split(";");
    for (let c of ca) {
      while (c.charAt(0) === " ") {
        c = c.substring(1);
      }
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length).split("\\").join("\n");
      }
    }
    return "";
  }
}
function no2gim(input) {
  const letters = ['\u05DC"', '\u05DB"', '\u05D9"', "\u05D8", "\u05D7", "\u05D6", "\u05D5", "\u05D4", "\u05D3", "\u05D2", "\u05D1", "\u05D0"];
  const values = [30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1];
  let output = "";
  while (input > 0) {
    for (let i = 0; i < letters.length; ++i) {
      if (input == 16) {
        return output + '\u05D8"\u05D6';
      }
      if (input == 15) {
        return output + '\u05D8"\u05D5';
      }
      if (input >= values[i]) {
        input -= values[i];
        output += letters[i];
        break;
      }
    }
  }
  if (output.endsWith('"'))
    output = output.slice(0, -1);
  return output;
}
function getYY(no) {
  let input = no;
  const letters = ["\u05D4'", "\u05D3'", "\u05D2'", "\u05D1'", "\u05D0'", "\u05EA", "\u05E9", "\u05E8", "\u05E7", "\u05E6", "\u05E4", "\u05E2", "\u05E1", "\u05E0", "\u05DE", "\u05DC", "\u05DB", "\u05D9", "\u05D8", "\u05D7", "\u05D6", "\u05D5", "\u05D4", "\u05D3", "\u05D2", "\u05D1", "\u05D0"];
  const values = [5e3, 4e3, 3e3, 2e3, 1e3, 400, 300, 200, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1];
  let output = "";
  while (input > 0) {
    for (let i = 0; i < letters.length; i++) {
      if (input == 16) {
        return output + "\u05D8\u05D6";
      }
      if (input == 15) {
        return output + "\u05D8\u05D5";
      }
      if (input >= values[i]) {
        input -= values[i];
        output += letters[i];
        break;
      }
    }
  }
  return output;
}
function convertEng(hebrew) {
  hebrew = hebrew.replace("Genesis", "\u05D1\u05E8\u05D0\u05E9\u05D9\u05EA");
  hebrew = hebrew.replace("Exodus", "\u05E9\u05DE\u05D5\u05EA");
  hebrew = hebrew.replace("Leviticus", "\u05D5\u05D9\u05E7\u05E8\u05D0");
  hebrew = hebrew.replace("Numbers", "\u05D1\u05DE\u05D3\u05D1\u05E8");
  hebrew = hebrew.replace("Deuteronomy", "\u05D3\u05D1\u05E8\u05D9\u05DD");
  hebrew = hebrew.replace("Joshua", "\u05D9\u05D4\u05D5\u05E9\u05E2");
  hebrew = hebrew.replace("Judges", "\u05E9\u05D5\u05E4\u05D8\u05D9\u05DD");
  hebrew = hebrew.replace("II Samuel", "\u05E9\u05DE\u05D5\u05D0\u05DC \u05D1");
  hebrew = hebrew.replace("I Samuel", "\u05E9\u05DE\u05D5\u05D0\u05DC \u05D0");
  hebrew = hebrew.replace("II Kings", "\u05DE\u05DC\u05DB\u05D9\u05DD \u05D1");
  hebrew = hebrew.replace("I Kings", "\u05DE\u05DC\u05DB\u05D9\u05DD \u05D0");
  hebrew = hebrew.replace("Isaiah", "\u05D9\u05E9\u05E2\u05D9\u05D4\u05D5");
  hebrew = hebrew.replace("Jeremiah", "\u05D9\u05E8\u05DE\u05D9\u05D4\u05D5");
  hebrew = hebrew.replace("Ezekiel", "\u05D9\u05D7\u05D6\u05E7\u05D0\u05DC");
  hebrew = hebrew.replace("Hosea", "\u05D4\u05D5\u05E9\u05E2");
  hebrew = hebrew.replace("Joel", "\u05D9\u05D5\u05D0\u05DC");
  hebrew = hebrew.replace("Amos", "\u05E2\u05DE\u05D5\u05E1");
  hebrew = hebrew.replace("Obadiah", "\u05E2\u05D5\u05D1\u05D3\u05D9\u05D4");
  hebrew = hebrew.replace("Jonah", "\u05D9\u05D5\u05E0\u05D4");
  hebrew = hebrew.replace("Micah", "\u05DE\u05D9\u05DB\u05D4");
  hebrew = hebrew.replace("Nachum", "\u05E0\u05D7\u05D5\u05DD");
  hebrew = hebrew.replace("Habakkuk", "\u05D7\u05D1\u05E7\u05D5\u05E7");
  hebrew = hebrew.replace("Zephaniah", "\u05E6\u05E4\u05E0\u05D9\u05D4");
  hebrew = hebrew.replace("Haggai", "\u05D7\u05D2\u05D9");
  hebrew = hebrew.replace("Zechariah", "\u05D6\u05DB\u05E8\u05D9\u05D4");
  hebrew = hebrew.replace("Malachi", "\u05DE\u05DC\u05D0\u05DB\u05D9");
  return hebrew;
}
function trim(n) {
  if (n.startsWith("0")) return n.substring(1);
  return n;
}
