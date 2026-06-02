const http = require('http');

function fetchFAQ() {
    return new Promise((resolve, reject) => {
        const req = http.get('http://localhost:3000/api/faq', (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(data);
                    resolve(parsed);
                } catch (e) {
                    reject(new Error('Invalid JSON: ' + data.substring(0, 200)));
                }
            });
        });
        req.on('error', reject);
        req.setTimeout(5000, () => {
            req.destroy();
            reject(new Error('Request timeout'));
        });
    });
}

async function main() {
    try {
        const faqs = await fetchFAQ();
        console.log('FAQ count:', faqs.length);
        console.log('First FAQ:', JSON.stringify(faqs[0]).substring(0, 300));
    } catch (e) {
        console.error('Error:', e.message);
    }
}

main();
