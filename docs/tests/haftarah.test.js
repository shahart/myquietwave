const test = require('node:test');
const assert = require('node:assert/strict');

const { createBrowserContext, loadScript } = require('./helpers/load-script');

test('builds a Kol Kore URL from a parasha name', () => {
    const context = createBrowserContext();
    loadScript(context, 'js/haftarah.js');

    const url = context.getKolKoreParashaUrl('פרשת אַחֲרֵי מוֹת־קְדֹשִׁים');

    assert.equal(
        decodeURIComponent(url),
        'https://kol-kore.org/פרשות/הפטרה-פרשת-אחרי-מות-קדשים/',
    );
});

test('extracts and normalizes the requested haftarah section', () => {
    const content = {
        cloneNode() {
            return {
                querySelectorAll: () => [],
                textContent: 'פתיח שלא יוצג\nנושאים בפרשה:\nפרט א\n\n\nפרט ב',
            };
        },
    };
    const heading = {
        closest: () => ({ querySelector: () => content }),
        textContent: '  על הקשר בין ההפטרה לפרשה  ',
    };
    class DOMParser {
        parseFromString() {
            return { querySelectorAll: () => [heading] };
        }
    }
    const context = createBrowserContext({ DOMParser });
    loadScript(context, 'js/haftarah.js');

    assert.equal(
        context.extractHaftarahConnection('<html></html>'),
        'נושאים בפרשה:\nפרט א\n\nפרט ב',
    );
});

test('rejects a page that does not contain the requested section', () => {
    class DOMParser {
        parseFromString() {
            return { querySelectorAll: () => [] };
        }
    }
    const context = createBrowserContext({ DOMParser });
    loadScript(context, 'js/haftarah.js');

    assert.throws(
        () => context.extractHaftarahConnection('<html></html>'),
        /requested section was not found/,
    );
});
