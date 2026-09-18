document.addEventListener('DOMContentLoaded', (event) => {
    const dropdown = document.getElementById('locationSelect');
    const otherInput = document.getElementById('otherLocation');

    document.getElementById('radioButton').addEventListener('click', listenToNextNews);
    document.getElementById('listenNowButton').addEventListener('click', listenNow);
    document.getElementById('whatsNext').addEventListener('click', whatsNext);
    document.getElementById('useCurrentLocationButton').addEventListener('click', getLoc);
    document.getElementById('refreshButton').addEventListener('click', calc);
    document.getElementById('haftarahConnectionButton').addEventListener('click', showHaftarahConnection);
    document.getElementById('closeHaftarahConnectionButton').addEventListener('click', closeHaftarahConnection);

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
    });

    const haftarahDialog = document.getElementById('haftarahConnectionDialog');
    haftarahDialog.addEventListener('click', function(event) {
        if (event.target === haftarahDialog) closeHaftarahConnection();
    });
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
