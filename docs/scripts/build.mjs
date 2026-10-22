import { buildSync } from 'esbuild';
import { mkdirSync } from 'node:fs';

const entries = [
    'utils',
    'haftarah',
    'zmanim',
    'date-time',
    'radio',
    'app',
];

mkdirSync('dist', { recursive: true });

for (const entry of entries) {
    buildSync({
        entryPoints: [`ts/${entry}.ts`],
        bundle: false,
        // Keep these as classic scripts: the pages intentionally share a small
        // global API between files and load them in dependency order.
        format: 'esm',
        target: 'es2020',
        outfile: `dist/${entry}.js`,
        legalComments: 'none',
    });
}

buildSync({
    entryPoints: ['ts/cloudFlare-cors-worker.ts'],
    bundle: false,
    format: 'esm',
    target: 'es2020',
    outfile: 'cloudflare/cloudFlare-cors-worker.js',
    legalComments: 'none',
});
