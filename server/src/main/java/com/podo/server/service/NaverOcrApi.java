package com.podo.server.service;

import lombok.extern.slf4j.Slf4j;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class NaverOcrApi {
    @Value("${naver.service.url}")
    private String url;

    /**
     * 네이버 ocr api 호출한다
     * @param {string} type 호출 메서드 타입
     * @param {string} filePath 파일 경로
     * @param {string} naver_secretKey 네이버 시크릿키 값
     * @param {string} ext 확장자
     * @returns {List} 추출 text list
     */
    public  List<String> callApi(String type, String filePath, String ext) {
        String apiURL = "https://stytlwh0q0.apigw.ntruss.com/custom/v1/31116/6dab4bebc7f7a648a7ceba58619f6ffc750deea82e394b7363bea602ddc7eab9/general";
        String secretKey = "dElSVU1lUUxxTklXQm5wRElETU1obHBaSUJEWEpxWVE=";
        String imageFile = filePath;
        List<String> parseData = null;

        log.info("callApi Start!");

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod(type);
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary); // HTTP 요청 헤더를 설정합니다.
            con.setRequestProperty("X-OCR-SECRET", secretKey); // 시크릿 키를 설정합니다.

            JSONObject json = new JSONObject(); // JSON 객체를 생성합니다.
            json.put("version", "V2"); // JSON 객체에 버전 정보를 추가합니다.
            json.put("requestId", UUID.randomUUID().toString()); // JSON 객체에 요청 ID를 추가합니다.
            json.put("timestamp", System.currentTimeMillis()); // JSON 객체에 타임스탬프를 추가합니다.
            JSONObject image = new JSONObject(); // JSON 객체를 생성합니다.
            image.put("format", ext); // 이미지 형식 정보를 추가합니다.
            image.put("name", "demo"); // 이미지 이름을 추가합니다.
            JSONArray images = new JSONArray(); // JSON 배열을 생성합니다.
            images.add(image); // JSON 배열에 이미지 정보를 추가합니다.
            json.put("images", images); // JSON 객체에 이미지 배열을 추가합니다.
            String postParams = json.toString(); // JSON 객체를 문자열로 변환합니다.

            con.connect();
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            File file = new File(imageFile);
            writeMultiPart(wr, postParams, file, boundary);
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            parseData = jsonparse(response);


        } catch (Exception e) {
            System.out.println(e);
        }
        return parseData;
    }
    /**
     * writeMultiPart
     * @param {OutputStream} out 데이터를 출력
     * @param {string} jsonMessage 요청 params
     * @param {File} file 요청 파일
     * @param {String} boundary 경계
     */
    private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws
            IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString
                    .append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");
            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }
    /**
     * 데이터 가공
     * @param {StringBuffer} response 응답값
     * @returns {List} result text list
     */
    private static List<String> jsonparse(StringBuffer response) throws ParseException {
        //json 파싱
        JSONParser jp = new JSONParser();
        JSONObject jobj = (JSONObject) jp.parse(response.toString());
        //images 배열 obj 화
        JSONArray JSONArrayPerson = (JSONArray)jobj.get("images");
        JSONObject JSONObjImage = (JSONObject)JSONArrayPerson.get(0);
        JSONArray s = (JSONArray) JSONObjImage.get("fields");
        //
        List<Map<String, Object>> m = JsonUtill.getListMapFromJsonArray(s);
        List<String> result = new ArrayList<>();
        for (Map<String, Object> as : m) {
            result.add((String) as.get("inferText"));
        }

        return result;
    }
}
