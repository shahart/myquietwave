const test = require('node:test');
const assert = require('node:assert/strict');

const { createBrowserContext, loadScript } = require('./helpers/load-script');
const { createDocument } = require('./helpers/fake-dom');

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
            { category: 'havdalah', date: '2026-09-19T19:25:00+03:00', hebrew: 'הבדלה' },
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
    assert.equal(document.getElementById('dafYomi').innerHTML, 'זבחים ל״א');
    assert.equal(document.getElementById('haftarahConnectionButton').disabled, false);
});
