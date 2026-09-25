const test = require('node:test');
const assert = require('node:assert/strict');

const { createBrowserContext, loadScript } = require('./helpers/load-script');
const { createDocument } = require('./helpers/fake-dom');

test('calculates local calendar dates without UTC day rollover', () => {
    const context = createBrowserContext();
    loadScript(context, 'js/zmanim.js');

    assert.equal(context.getLocalDateString(new Date(2026, 8, 22, 0, 30)), '2026-09-22');
    assert.equal(context.getNextSaturdayDate(new Date(2026, 8, 22, 12)), '2026-09-26');
    assert.equal(context.getNextSaturdayDate(new Date(2026, 8, 26, 12)), '2026-09-26');
});

test('fetches and renders the current calendar data', async () => {
    const document = createDocument({ locationSelect: 'IL-Jerusalem' });
    const requestedUrls = [];
    const zmanim = {
        location: { title: 'Jerusalem, Israel' },
        times: {
            sunrise: '2026-09-18T06:25:00+03:00',
            sunset: '2026-09-18T18:39:00+03:00',
        },
    };
    const shabbat = {
        items: [
            {
                category: 'parashat',
                hebrew: 'פרשת בראשית',
                leyning: {
                    haftarah: 'Isaiah 42:5-43:10',
                    haftarah_sephardic: 'Joshua 1:1-9',
                },
            },
            { category: 'candles', date: '2026-09-18T18:20:00+03:00', hebrew: 'הדלקת נרות' },
            { category: 'havdalah', date: '2000-09-16T18:00:00+03:00', hebrew: 'הבדלה' },
            { category: 'havdalah', date: '2099-09-19T19:25:00+03:00', hebrew: 'הבדלה' },
        ],
    };
    const dailyLearning = {
        items: [
            { category: 'mishnayomi', hebrew: 'משנה א' },
            { category: 'dafyomi', hebrew: 'זבחים ל״א' },
        ],
    };
    const responses = [zmanim, shabbat, dailyLearning];
    const context = createBrowserContext({
        alert: () => {},
        document,
        fetch: async url => {
            requestedUrls.push(url);
            return { json: async () => responses[requestedUrls.length - 1] };
        },
        localStorage: { setItem: () => {} },
        Storage: function Storage() {},
    });

    loadScript(context, 'js/utils.js');
    loadScript(context, 'js/haftarah.js');
    loadScript(context, 'js/zmanim.js');
    await context.calc();

    assert.equal(requestedUrls.length, 3);
    assert.match(requestedUrls[0], /hebcal\.com\/zmanim\?cfg=json&city=IL-Jerusalem/);
    assert.match(requestedUrls[1], /hebcal\.com\/shabbat\?cfg=json&city=IL-Jerusalem/);
    assert.equal(document.getElementById('sunrise').innerHTML, '6:25');
    assert.equal(document.getElementById('sunset').innerHTML, '18:39');
    assert.equal(document.getElementById('foundLoc').innerHTML, 'Jerusalem, Israel');
    assert.equal(document.getElementById('parasha').innerHTML, 'פרשת בראשית');
    assert.match(document.getElementById('haftarah').innerHTML, /ישעיהו/);
    assert.match(document.getElementById('lighting').innerHTML, /18:20/);
    assert.match(document.getElementById('havdala').innerHTML, /19:25/);
    assert.doesNotMatch(document.getElementById('havdala').innerHTML, /18:00/);
    assert.equal(document.getElementById('dafYomi').innerHTML, 'זבחים ל״א');
    assert.equal(document.getElementById('haftarahConnectionButton').disabled, false);
});

test('renders major holiday readings that fall on the next Saturday', async () => {
    const document = createDocument({ locationSelect: 'IL-Jerusalem' });
    const RealDate = Date;
    class TestDate extends RealDate {
        constructor(...args) {
            super(...(args.length ? args : ['2026-09-22T12:00:00+03:00']));
        }

        static now() {
            return new RealDate('2026-09-22T12:00:00+03:00').getTime();
        }
    }
    const zmanim = {
        location: { title: 'Jerusalem, Israel' },
        times: {
            sunrise: '2026-09-22T06:25:00+03:00',
            sunset: '2026-09-22T18:39:00+03:00',
        },
    };
    const shabbat = {
        items: [
            { category: 'holiday', subcat: 'major', title: 'Yom Kippur', date: '2026-09-21', hebrew: 'יום כיפור', yomtov: true },
            { category: 'roshchodesh', date: '2026-09-21', hebrew: 'ראש חודש תשרי', memo: 'Past Rosh Chodesh' },
            { title: 'Fast begins', date: '2026-09-21T05:00:00+03:00', hebrew: 'תחילת צום' },
            { title: 'Fast ends', date: '2026-09-21T19:00:00+03:00', hebrew: 'סיום צום' },
            {
                category: 'holiday',
                subcat: 'major',
                title: 'Sukkot I',
                date: '2026-09-26',
                hebrew: 'סוכות יום א׳',
                yomtov: true,
                leyning: {
                    1: 'Leviticus 22:26-23:44',
                    haftarah: 'Zechariah 14:1-21',
                },
            },
            { category: 'holiday', subcat: 'major', title: 'Sukkot II', date: '2026-09-27', hebrew: 'סוכות יום ב׳', yomtov: true },
        ],
    };
    const dailyLearning = { items: [] };
    let responseIndex = 0;
    const context = createBrowserContext({
        Date: TestDate,
        alert: () => {},
        document,
        fetch: async () => ({ json: async () => [zmanim, shabbat, dailyLearning][responseIndex++] }),
        localStorage: { setItem: () => {} },
        Storage: function Storage() {},
    });

    loadScript(context, 'js/utils.js');
    loadScript(context, 'js/haftarah.js');
    loadScript(context, 'js/zmanim.js');
    await context.calc();

    assert.equal(
        document.getElementById('parasha').innerHTML,
        'יום טוב סוכות יום א׳<br>ויקרא 22:26-23:44',
    );
    assert.equal(document.getElementById('parasha2').innerHTML, '');
    assert.equal(document.getElementById('haftarah').innerHTML, 'זכריה 14:1-21');
    assert.equal(
        document.getElementById('haftarahUrl').href,
        'https://shahart.github.io/heb-bible/index.html?b=Zechariah 14',
    );
    assert.match(document.getElementById('special').innerHTML, /סוכות יום א׳/);
    assert.doesNotMatch(document.getElementById('special').innerHTML, /יום כיפור/);
    assert.equal(document.getElementById('roshchodesh').innerHTML, '');
    assert.equal(document.getElementById('fast').innerHTML, '');
    assert.match(document.getElementById('haftarahConnectionDisabledReason').textContent, /סוכות יום א׳/);
});
