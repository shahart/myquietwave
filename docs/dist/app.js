document.addEventListener("DOMContentLoaded", (event) => {
  const dropdown = document.getElementById("locationSelect");
  const otherInput = document.getElementById("otherLocation");
  document.getElementById("radioButton").addEventListener("click", listenToNextNews);
  document.getElementById("listenNowButton").addEventListener("click", listenNow);
  document.getElementById("whatsNext").addEventListener("click", whatsNext);
  document.getElementById("useCurrentLocationButton").addEventListener("click", getLoc);
  document.getElementById("refreshButton").addEventListener("click", calc);
  document.getElementById("haftarahConnectionButton").addEventListener("click", showHaftarahConnection);
  document.getElementById("closeHaftarahConnectionButton").addEventListener("click", closeHaftarahConnection);
  dropdown.addEventListener("change", function() {
    if (this.value === "other") {
      alert("\u05D4\u05D5\u05E1\u05E3 ue, \u05E2\u05D1\u05D5\u05E8 do not use elevation \u05D1\u05D7\u05D9\u05E9\u05D5\u05D1\u05D9\u05DD - \u05DB\u05DC\u05D5\u05DE\u05E8 \u05E9\u05E7\u05D9\u05E2\u05D4 \u05D4\u05DE\u05D9\u05E9\u05D5\u05E8\u05D9\u05EA. \u05D0\u05D7\u05E8\u05EA, \u05EA\u05D5\u05E6\u05D2 \u05D4\u05E9\u05E7\u05D9\u05E2\u05D4 \u05D4\u05E0\u05E8\u05D0\u05D9\u05EA");
      otherInput.style.display = "block";
      otherInput.focus();
    } else {
      otherInput.style.display = "none";
      otherInput.value = "";
    }
  });
  document.getElementById("todo").addEventListener("focusout", function() {
    saveInput("todo", document.getElementById("todo").value);
  });
  const haftarahDialog = document.getElementById("haftarahConnectionDialog");
  haftarahDialog.addEventListener("click", function(event2) {
    if (event2.target === haftarahDialog) closeHaftarahConnection();
  });
});
let cookieInput = loadInput("zmanim-location");
if (cookieInput !== "") {
  document.getElementById("locationSelect").value = cookieInput;
  if (cookieInput === "other") {
    document.getElementById("otherLocation").style.display = "block";
    document.getElementById("otherLocation").value = loadInput("zmanim-location-other");
  } else {
    document.getElementById("otherLocation").style.display = "none";
    document.getElementById("otherLocation").value = "";
  }
}
let todocookieInput = loadInput("todo");
if (todocookieInput !== "") {
  document.getElementById("todo").value = todocookieInput;
}
calc();
