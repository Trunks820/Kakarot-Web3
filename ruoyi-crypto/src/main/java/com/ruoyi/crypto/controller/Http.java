package com.ruoyi.crypto.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
public class Http {

    /**
     *获取代币信息
     */
//    OkHttpClient client = new OkHttpClient();
//
//    Request request = new Request.Builder()
//            .url("https://api2.axiom.trade/pair-info?pairAddress=AQcBbrwGmgzwimvfwNdBTGTgn8mq2u74NerGta5mGB2o").get()
//            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .addHeader("Accept", "application/json, text/plain, */*")
//            .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .addHeader("sec-ch-ua-platform", "\"Windows\"")
//            .addHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .addHeader("sec-ch-ua-mobile", "?0")
//            .addHeader("origin", "https://axiom.trade")
//            .addHeader("sec-fetch-site", "same-site")
//            .addHeader("sec-fetch-mode", "cors")
//            .addHeader("sec-fetch-dest", "empty")
//            .addHeader("referer", "https://axiom.trade/")
//            .addHeader("accept-language", "zh-CN,zh;q=0.9")
//            .addHeader("priority", "u=1, i")
//            .addHeader("Cookie", "auth-refresh-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWZyZXNoVG9rZW5JZCI6IjRhODM5ZWJkLTRlMzUtNDUzMy05ZjdiLTBiMjQ5MDhkODlhNSIsImlhdCI6MTc0ODg4Nzc1OH0.-XRWiCkfcswkGH1-T-neknarXIkVR1zK7YopISLXH30; auth-access-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoZW50aWNhdGVkVXNlcklkIjoiOGQxNmY1OTktNjJlNC00Njk3LTljMTYtZDM0ZmE3M2Y4ZTc2IiwiaWF0IjoxNzQ4OTY0MDI0LCJleHAiOjE3NDg5NjQ5ODR9.hpiLxOeJtIdYkqi5aZFOwbY3qkAv5kLzeeBd9YOc48g")
//            .build();
//    Response response = client.newCall(request).execute();
//    String string = response.body().string();


    /**
     * 获取持有者信息
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://api.axiom.trade/token-info?pairAddress=AQcBbrwGmgzwimvfwNdBTGTgn8mq2u74NerGta5mGB2o"))
//            .GET()
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("origin", "https://axiom.trade")
//            .setHeader("sec-fetch-site", "same-site")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://axiom.trade/")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "auth-refresh-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWZyZXNoVG9rZW5JZCI6IjRhODM5ZWJkLTRlMzUtNDUzMy05ZjdiLTBiMjQ5MDhkODlhNSIsImlhdCI6MTc0ODg4Nzc1OH0.-XRWiCkfcswkGH1-T-neknarXIkVR1zK7YopISLXH30; auth-access-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoZW50aWNhdGVkVXNlcklkIjoiOGQxNmY1OTktNjJlNC00Njk3LTljMTYtZDM0ZmE3M2Y4ZTc2IiwiaWF0IjoxNzQ4OTY0MDI0LCJleHAiOjE3NDg5NjQ5ODR9.hpiLxOeJtIdYkqi5aZFOwbY3qkAv5kLzeeBd9YOc48g")
//            .build();
//
//    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


    /**
     * 关注钱包交易提醒
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://api8.axiom.trade/tracked-wallet-transactions"))
//            .GET()
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("origin", "https://axiom.trade")
//            .setHeader("sec-fetch-site", "same-site")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://axiom.trade/")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "auth-refresh-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWZyZXNoVG9rZW5JZCI6IjRhODM5ZWJkLTRlMzUtNDUzMy05ZjdiLTBiMjQ5MDhkODlhNSIsImlhdCI6MTc0ODg4Nzc1OH0.-XRWiCkfcswkGH1-T-neknarXIkVR1zK7YopISLXH30; auth-access-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoZW50aWNhdGVkVXNlcklkIjoiOGQxNmY1OTktNjJlNC00Njk3LTljMTYtZDM0ZmE3M2Y4ZTc2IiwiaWF0IjoxNzQ4OTY0MDI0LCJleHAiOjE3NDg5NjQ5ODR9.hpiLxOeJtIdYkqi5aZFOwbY3qkAv5kLzeeBd9YOc48g")
//            .build();

    /**
     * 关注钱包列表
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://api.axiom.trade/tracked-wallets"))
//            .GET()
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("origin", "https://axiom.trade")
//            .setHeader("sec-fetch-site", "same-site")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://axiom.trade/")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "auth-refresh-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWZyZXNoVG9rZW5JZCI6IjRhODM5ZWJkLTRlMzUtNDUzMy05ZjdiLTBiMjQ5MDhkODlhNSIsImlhdCI6MTc0ODg4Nzc1OH0.-XRWiCkfcswkGH1-T-neknarXIkVR1zK7YopISLXH30; auth-access-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoZW50aWNhdGVkVXNlcklkIjoiOGQxNmY1OTktNjJlNC00Njk3LTljMTYtZDM0ZmE3M2Y4ZTc2IiwiaWF0IjoxNzQ4OTY0MDI0LCJleHAiOjE3NDg5NjQ5ODR9.hpiLxOeJtIdYkqi5aZFOwbY3qkAv5kLzeeBd9YOc48g")
//            .build();


    /**
     * 获取代币信息gmgn
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://gmgn.ai/api/v1/mutil_window_token_info?device_id=5461cc48-2e83-4740-8e90-8409db8cf33d&client_id=gmgn_web_20250603-1799-730df90&from_app=gmgn&app_ver=20250603-1799-730df90&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=68fd1291b5f4f826d98e6378c0e3a663&os=web"))
//            .POST(HttpRequest.BodyPublishers.ofString("{\"chain\":\"sol\",\"addresses\":[\"3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump\"]}"))
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("Content-Type", "application/json")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("baggage", "sentry-environment=production,sentry-release=20250603-1799-730df90,sentry-public_key=93c25bab7246077dc3eb85b59d6e7d40,sentry-trace_id=47817276c5b44d07b2627d8c2f221f30,sentry-sample_rate=0.005,sentry-sampled=false")
//            .setHeader("sentry-trace", "47817276c5b44d07b2627d8c2f221f30-b05892ecd048eb90-0")
//            .setHeader("origin", "https://gmgn.ai")
//            .setHeader("sec-fetch-site", "same-origin")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://gmgn.ai/sol/token/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "_ga=GA1.1.704572436.1712248135; _ga_0XM0LYXGC8=deleted; b-user-id=f3b3bb30-0b5c-fc55-f1fa-5af0522175ce; GMGN_LOCALE=zh-CN; GMGN_THEME=dark; sid=gmgn%7C691295b013c0e5714ab5f79835145729; _ga_UGLVBMV4Z0=GS1.2.1748597965489087.a12f85a73d09c2e4fe36d46f9d41ade6.QaPK6yv2T4rQ6xjGBTlGnA%3D%3D.jX0Cij%2B%2BT0w5vaKwjl4PnA%3D%3D.lzf%2FKVwJ2Frr9r77jD4QUQ%3D%3D.H3EBykaM5jrX9MGPSyFtfg%3D%3D; GMGN_CHAIN=sol; __cf_bm=9HnOPa4neqo6CA1vYBePh.sI48rtejnAzm5xHZQordU-1748965722-1.0.1.1-atM7ddn7JsqfjPVOix7QEoTpAl4DAAQ69n277jcJJ9bk0vggGlP6dk4rubolKo8QeCDrav7LG2gX_s4N8ukUiGdq_bUvOuq0ySiZOUclslw; cf_clearance=2PgwveiglxlNmOCwrkQgXTEN.tEdWFmJ0fCisOogrIM-1748966032-1.2.1.1-nqhd1Mx2jE49qDWUWtWiBxLB9h0mzwt9OmLfUy48pGOwZuuGVYEElM1YPK.4tnPbyt7Yt6ov3DDrRHp2eagNwywx81TE9JV8AKNt9KUBskZ2wVjvzK4QFRdjzLDjFghz_ApXecOhczoMe8vUtpLV4Z9Jf5omck4Drnh5u4bV1JwlRpAd5CBhvVTI3T0FKZNHCuVVMLEFJiu7_qXx7pshWYIjC9iId3c6nkG64.r5Y91NSK577fRnxSNOPYBe7BMF9OqalS5_dBRERhZosGkSE2st6MiGQJpKdX7mD2Ef0BWmihC3oz9u.tMO5zL956jsekp2Xrc6LW4NayrO_zxUZWxHsN0Mlzv3VhW5txhILbs; _ga_0XM0LYXGC8=GS2.1.s1748966033$o32$g1$t1748966094$j60$l0$h0")
//            .build();

    /**
     * 代币持有人分析
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://gmgn.ai/api/v1/token_wallet_tags_stat/sol/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump?device_id=5461cc48-2e83-4740-8e90-8409db8cf33d&client_id=gmgn_web_20250603-1799-730df90&from_app=gmgn&app_ver=20250603-1799-730df90&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=68fd1291b5f4f826d98e6378c0e3a663&os=web"))
//            .GET()
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("sentry-trace", "47817276c5b44d07b2627d8c2f221f30-aabf49bbedb19b65-0")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("baggage", "sentry-environment=production,sentry-release=20250603-1799-730df90,sentry-public_key=93c25bab7246077dc3eb85b59d6e7d40,sentry-trace_id=47817276c5b44d07b2627d8c2f221f30,sentry-sample_rate=0.005,sentry-sampled=false")
//            .setHeader("sec-fetch-site", "same-origin")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://gmgn.ai/sol/token/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "_ga=GA1.1.704572436.1712248135; _ga_0XM0LYXGC8=deleted; b-user-id=f3b3bb30-0b5c-fc55-f1fa-5af0522175ce; GMGN_LOCALE=zh-CN; GMGN_THEME=dark; sid=gmgn%7C691295b013c0e5714ab5f79835145729; _ga_UGLVBMV4Z0=GS1.2.1748597965489087.a12f85a73d09c2e4fe36d46f9d41ade6.QaPK6yv2T4rQ6xjGBTlGnA%3D%3D.jX0Cij%2B%2BT0w5vaKwjl4PnA%3D%3D.lzf%2FKVwJ2Frr9r77jD4QUQ%3D%3D.H3EBykaM5jrX9MGPSyFtfg%3D%3D; GMGN_CHAIN=sol; __cf_bm=9HnOPa4neqo6CA1vYBePh.sI48rtejnAzm5xHZQordU-1748965722-1.0.1.1-atM7ddn7JsqfjPVOix7QEoTpAl4DAAQ69n277jcJJ9bk0vggGlP6dk4rubolKo8QeCDrav7LG2gX_s4N8ukUiGdq_bUvOuq0ySiZOUclslw; cf_clearance=2PgwveiglxlNmOCwrkQgXTEN.tEdWFmJ0fCisOogrIM-1748966032-1.2.1.1-nqhd1Mx2jE49qDWUWtWiBxLB9h0mzwt9OmLfUy48pGOwZuuGVYEElM1YPK.4tnPbyt7Yt6ov3DDrRHp2eagNwywx81TE9JV8AKNt9KUBskZ2wVjvzK4QFRdjzLDjFghz_ApXecOhczoMe8vUtpLV4Z9Jf5omck4Drnh5u4bV1JwlRpAd5CBhvVTI3T0FKZNHCuVVMLEFJiu7_qXx7pshWYIjC9iId3c6nkG64.r5Y91NSK577fRnxSNOPYBe7BMF9OqalS5_dBRERhZosGkSE2st6MiGQJpKdX7mD2Ef0BWmihC3oz9u.tMO5zL956jsekp2Xrc6LW4NayrO_zxUZWxHsN0Mlzv3VhW5txhILbs; _ga_0XM0LYXGC8=GS2.1.s1748966033$o32$g1$t1748966094$j60$l0$h0")
//            .build();

    /**
     * 前排钱包数据
     */
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://gmgn.ai/vas/api/v1/token_holders/sol/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump?device_id=5461cc48-2e83-4740-8e90-8409db8cf33d&client_id=gmgn_web_20250603-1799-730df90&from_app=gmgn&app_ver=20250603-1799-730df90&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=68fd1291b5f4f826d98e6378c0e3a663&os=web&limit=20&cost=20&orderby=amount_percentage&direction=desc"))
            .GET()
            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
            .setHeader("Accept", "application/json, text/plain, */*")
            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
            .setHeader("sentry-trace", "47817276c5b44d07b2627d8c2f221f30-aeb3f1408e5f05b5-0")
            .setHeader("sec-ch-ua-platform", "\"Windows\"")
            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
            .setHeader("sec-ch-ua-mobile", "?0")
            .setHeader("baggage", "sentry-environment=production,sentry-release=20250603-1799-730df90,sentry-public_key=93c25bab7246077dc3eb85b59d6e7d40,sentry-trace_id=47817276c5b44d07b2627d8c2f221f30,sentry-sample_rate=0.005,sentry-sampled=false")
            .setHeader("sec-fetch-site", "same-origin")
            .setHeader("sec-fetch-mode", "cors")
            .setHeader("sec-fetch-dest", "empty")
            .setHeader("referer", "https://gmgn.ai/sol/token/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump")
            .setHeader("accept-language", "zh-CN,zh;q=0.9")
            .setHeader("priority", "u=1, i")
            .setHeader("Cookie", "_ga=GA1.1.704572436.1712248135; _ga_0XM0LYXGC8=deleted; b-user-id=f3b3bb30-0b5c-fc55-f1fa-5af0522175ce; GMGN_LOCALE=zh-CN; GMGN_THEME=dark; sid=gmgn%7C691295b013c0e5714ab5f79835145729; _ga_UGLVBMV4Z0=GS1.2.1748597965489087.a12f85a73d09c2e4fe36d46f9d41ade6.QaPK6yv2T4rQ6xjGBTlGnA%3D%3D.jX0Cij%2B%2BT0w5vaKwjl4PnA%3D%3D.lzf%2FKVwJ2Frr9r77jD4QUQ%3D%3D.H3EBykaM5jrX9MGPSyFtfg%3D%3D; GMGN_CHAIN=sol; __cf_bm=9HnOPa4neqo6CA1vYBePh.sI48rtejnAzm5xHZQordU-1748965722-1.0.1.1-atM7ddn7JsqfjPVOix7QEoTpAl4DAAQ69n277jcJJ9bk0vggGlP6dk4rubolKo8QeCDrav7LG2gX_s4N8ukUiGdq_bUvOuq0ySiZOUclslw; cf_clearance=2PgwveiglxlNmOCwrkQgXTEN.tEdWFmJ0fCisOogrIM-1748966032-1.2.1.1-nqhd1Mx2jE49qDWUWtWiBxLB9h0mzwt9OmLfUy48pGOwZuuGVYEElM1YPK.4tnPbyt7Yt6ov3DDrRHp2eagNwywx81TE9JV8AKNt9KUBskZ2wVjvzK4QFRdjzLDjFghz_ApXecOhczoMe8vUtpLV4Z9Jf5omck4Drnh5u4bV1JwlRpAd5CBhvVTI3T0FKZNHCuVVMLEFJiu7_qXx7pshWYIjC9iId3c6nkG64.r5Y91NSK577fRnxSNOPYBe7BMF9OqalS5_dBRERhZosGkSE2st6MiGQJpKdX7mD2Ef0BWmihC3oz9u.tMO5zL956jsekp2Xrc6LW4NayrO_zxUZWxHsN0Mlzv3VhW5txhILbs; _ga_0XM0LYXGC8=GS2.1.s1748966033$o32$g1$t1748966094$j60$l0$h0")
            .build();


    /**
     * 代币安全信息
     */
//    HttpRequest request = HttpRequest.newBuilder()
//            .uri(URI.create("https://gmgn.ai/api/v1/mutil_window_token_security_launchpad/sol/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump?device_id=5461cc48-2e83-4740-8e90-8409db8cf33d&client_id=gmgn_web_20250603-1799-730df90&from_app=gmgn&app_ver=20250603-1799-730df90&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=68fd1291b5f4f826d98e6378c0e3a663&os=web"))
//            .GET()
//            .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
//            .setHeader("Accept", "application/json, text/plain, */*")
//            .setHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//            .setHeader("sentry-trace", "47817276c5b44d07b2627d8c2f221f30-b3e01f98091f7662-0")
//            .setHeader("sec-ch-ua-platform", "\"Windows\"")
//            .setHeader("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
//            .setHeader("sec-ch-ua-mobile", "?0")
//            .setHeader("baggage", "sentry-environment=production,sentry-release=20250603-1799-730df90,sentry-public_key=93c25bab7246077dc3eb85b59d6e7d40,sentry-trace_id=47817276c5b44d07b2627d8c2f221f30,sentry-sample_rate=0.005,sentry-sampled=false")
//            .setHeader("sec-fetch-site", "same-origin")
//            .setHeader("sec-fetch-mode", "cors")
//            .setHeader("sec-fetch-dest", "empty")
//            .setHeader("referer", "https://gmgn.ai/sol/token/3E4UJFK28KUK1q4nzddkPvS2iM53MuTjU8svMrEKpump")
//            .setHeader("accept-language", "zh-CN,zh;q=0.9")
//            .setHeader("priority", "u=1, i")
//            .setHeader("Cookie", "_ga=GA1.1.704572436.1712248135; _ga_0XM0LYXGC8=deleted; b-user-id=f3b3bb30-0b5c-fc55-f1fa-5af0522175ce; GMGN_LOCALE=zh-CN; GMGN_THEME=dark; sid=gmgn%7C691295b013c0e5714ab5f79835145729; _ga_UGLVBMV4Z0=GS1.2.1748597965489087.a12f85a73d09c2e4fe36d46f9d41ade6.QaPK6yv2T4rQ6xjGBTlGnA%3D%3D.jX0Cij%2B%2BT0w5vaKwjl4PnA%3D%3D.lzf%2FKVwJ2Frr9r77jD4QUQ%3D%3D.H3EBykaM5jrX9MGPSyFtfg%3D%3D; GMGN_CHAIN=sol; __cf_bm=9HnOPa4neqo6CA1vYBePh.sI48rtejnAzm5xHZQordU-1748965722-1.0.1.1-atM7ddn7JsqfjPVOix7QEoTpAl4DAAQ69n277jcJJ9bk0vggGlP6dk4rubolKo8QeCDrav7LG2gX_s4N8ukUiGdq_bUvOuq0ySiZOUclslw; cf_clearance=2PgwveiglxlNmOCwrkQgXTEN.tEdWFmJ0fCisOogrIM-1748966032-1.2.1.1-nqhd1Mx2jE49qDWUWtWiBxLB9h0mzwt9OmLfUy48pGOwZuuGVYEElM1YPK.4tnPbyt7Yt6ov3DDrRHp2eagNwywx81TE9JV8AKNt9KUBskZ2wVjvzK4QFRdjzLDjFghz_ApXecOhczoMe8vUtpLV4Z9Jf5omck4Drnh5u4bV1JwlRpAd5CBhvVTI3T0FKZNHCuVVMLEFJiu7_qXx7pshWYIjC9iId3c6nkG64.r5Y91NSK577fRnxSNOPYBe7BMF9OqalS5_dBRERhZosGkSE2st6MiGQJpKdX7mD2Ef0BWmihC3oz9u.tMO5zL956jsekp2Xrc6LW4NayrO_zxUZWxHsN0Mlzv3VhW5txhILbs; _ga_0XM0LYXGC8=GS2.1.s1748966033$o32$g1$t1748966094$j60$l0$h0")
//            .build();
}
