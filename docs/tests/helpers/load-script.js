const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function createBrowserContext(overrides = {}) {
    const context = vm.createContext({
        AbortSignal,
        Date,
        Intl,
        Map,
        URL,
        clearInterval,
        clearTimeout,
        console,
        decodeURIComponent,
        encodeURIComponent,
        setInterval,
        setTimeout,
        ...overrides,
    });

    context.window = context;
    return context;
}

function loadScript(context, relativePath) {
    const filename = path.resolve(__dirname, '..', '..', relativePath);
    const source = fs.readFileSync(filename, 'utf8');
    vm.runInContext(source, context, { filename });
}

module.exports = { createBrowserContext, loadScript };
