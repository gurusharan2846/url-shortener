import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 20 },
//    { duration: '3m', target: 100 },
//    { duration: '2m', target: 150 },
//    { duration: '2m', target: 0 },
  ],
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

const hotCode = 'b6N40v';
const coldUrls = ['def456', 'GbKuxY', 'NKsowR', '3ABIlk'];

export default function () {
  let code;

  if (Math.random() < 0.8) {
    code = hotCode;        // 80% traffic → hotspot
  } else {
    code = coldUrls[Math.floor(Math.random() * coldUrls.length)];
  }

  http.get(`${BASE}/${code}`, {
    redirects: 0,
    headers: {
      Host: 'redirect.urlshort.local',
    },
  });

  sleep(0.2);
}