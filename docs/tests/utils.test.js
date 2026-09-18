const test = require('node:test');
const assert = require('node:assert/strict');

const { createBrowserContext, loadScript } = require('./helpers/load-script');

function loadUtils() {
    const values = new Map();
    const context = createBrowserContext({
        Storage: function Storage() {},
        document: { cookie: '' },
        localStorage: {
            getItem: key => values.get(key) ?? null,
            setItem: (key, value) => values.set(key, value),
        },
    });
    loadScript(context, 'js/utils.js');
    return context;
}

test('converts Hebrew day numbers, including the 15 and 16 exceptions', () => {
    const context = loadUtils();

    assert.equal(context.no2gim(1), 'א');
    assert.equal(context.no2gim(15), 'ט"ו');
    assert.equal(context.no2gim(16), 'ט"ז');
    assert.equal(context.no2gim(31), 'ל"א');
});

test('converts Hebrew years', () => {
    const context = loadUtils();

    assert.equal(context.getYY(5786), "ה'תשפו");
});

test('translates book names and trims leading zeroes from times', () => {
    const context = loadUtils();

    assert.equal(context.convertEng('Isaiah 1:1'), 'ישעיהו 1:1');
    assert.equal(context.convertEng('II Samuel 7:1'), 'שמואל ב 7:1');
    assert.equal(context.convertEng('II Kings 4:1'), 'מלכים ב 4:1');
    assert.equal(context.trim('06:15'), '6:15');
    assert.equal(context.trim('16:15'), '16:15');
});

test('persists and loads user input through local storage', () => {
    const context = loadUtils();

    context.saveInput('location', 'IL-Jerusalem');
    assert.equal(context.loadInput('location'), 'IL-Jerusalem');
    assert.equal(context.loadInput('missing'), '');
});
