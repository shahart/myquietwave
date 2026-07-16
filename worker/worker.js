export default {
    async fetch(request) {
        const url = new URL(request.url);
        const target = url.searchParams.get("url");

        if (!target) {
            return new Response(JSON.stringify({ error: "Missing ?url= parameter" }), {
                status: 400,
                headers: { "Content-Type": "application/json" },
            });
        }

        try {
            const resp = await fetch(target, {
                headers: request.headers,
                redirect: "follow",
            });

            const body = await resp.text();

            return new Response(body, {
                status: resp.status,
                headers: {
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Methods": "GET",
                    "Content-Type": resp.headers.get("Content-Type") || "application/octet-stream",
                },
            });
        } catch (e) {
            return new Response(JSON.stringify({ error: e.message }), {
                status: 502,
                headers: {
                    "Content-Type": "application/json",
                    "Access-Control-Allow-Origin": "*",
                },
            });
        }
    },
};