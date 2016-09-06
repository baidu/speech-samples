/***************************************************************************
 * 
 * Copyright (c) 2014 Baidu.com, Inc. All Rights Reserved
 * 
 **************************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include "curl/include/curl/curl.h"
#include "curl/include/curl/easy.h"
#include "json-cpp/include/json.h"
#include "base64.h"

#define MAX_BUFFER_SIZE 512
#define MAX_BODY_SIZE 1000000


static size_t writefunc(void *ptr, size_t size, size_t nmemb, char **result)
{
    size_t result_len = size * nmemb;
    *result = (char *)realloc(*result, result_len + 1);
    if (*result == NULL)
    {
        printf("realloc failure!\n");
        return 1;
    }
    memcpy(*result, ptr, result_len);
    (*result)[result_len] = '\0';
    printf("%s\n", *result);
    return result_len;
}


int main (int argc,char* argv[])
{
    if (argc != 2)
    {
        printf("Usage: %s audiofile\n", argv[0]);
        return -1;
    }

    FILE *fp = NULL;
    fp = fopen(argv[1], "r");
    if (NULL == fp)
    {
        return -1;
    }
    fseek(fp, 0, SEEK_END);
    int content_len = ftell(fp);
    fseek(fp, 0, SEEK_SET);
    char *audiodata = (char *)malloc(content_len);
    fread(audiodata, content_len, sizeof(char), fp);

    //put your own params here
    char *cuid = "";
    char *apiKey = "";
    char *secretKey = "";

    std::string token;
    char host[MAX_BUFFER_SIZE];
    snprintf(host, sizeof(host), 
            "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s", 
            apiKey, secretKey);
    FILE* fpp = NULL;
    char cmd[MAX_BUFFER_SIZE];
    char* result = (char*)malloc(MAX_BUFFER_SIZE);
    char* curl_cmd = "curl -s ";
    char* yinhao = "\"";
    strcpy(cmd, curl_cmd);
    strcat(cmd, yinhao);
    strcat(cmd, host);
    strcat(cmd, yinhao);
    fpp = popen(cmd, "r");
    fgets(result, MAX_BUFFER_SIZE, fpp);
    pclose(fpp);

    if (result != NULL) 
    {
        Json::Reader reader;
        Json::Value root;
        if (reader.parse(result, root, false)) 
        {
            token = root.get("access_token","").asString();
        }
    }

    memset(host, 0, sizeof(host));
    snprintf(host, sizeof(host), "%s", "http://vop.baidu.com/server_api");

#ifdef _METHOD_1_
    //method 1
    char tmp[MAX_BUFFER_SIZE];
    memset(tmp, 0, sizeof(tmp));
    char body[MAX_BODY_SIZE];
    memset(body, 0, sizeof(body));
    std::string decode_data = base64_encode((const unsigned char *)audiodata, content_len);
    if (0 == decode_data.length())
    {
        printf("base64 encoded data is empty.\n");
        return 1;
    }
    
    Json::Value buffer;
    Json::FastWriter trans;
    buffer["format"]  = "pcm";
    buffer["rate"]    = 8000;
    buffer["channel"] = 1;
    buffer["token"]   = token.c_str();
    buffer["cuid"]    = cuid;
    buffer["speech"]  = decode_data;
    buffer["len"]     = content_len;
//    buffer["url"]  = url;
//    buffer["callback"]     = callback;

    content_len = trans.write(buffer).length();
    memcpy(body, trans.write(buffer).c_str(), content_len);

    CURL *curl;
    CURLcode res;
    char *resultBuf = NULL;
    struct curl_slist *headerlist = NULL;
    snprintf(tmp, sizeof(tmp), "%s", "Content-Type: application/json; charset=utf-8");
    headerlist = curl_slist_append(headerlist, tmp);
    snprintf(tmp, sizeof(tmp), "Content-Length: %d", content_len);
    headerlist = curl_slist_append(headerlist, tmp);

    curl = curl_easy_init();
    curl_easy_setopt(curl, CURLOPT_URL, host);
    curl_easy_setopt(curl, CURLOPT_POST, 1);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headerlist);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, content_len);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writefunc);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &resultBuf);
    res = curl_easy_perform(curl);
    if (res != CURLE_OK)
    {
        printf("perform curl error:%d.\n", res);
        return 1;
    }
    curl_slist_free_all(headerlist);
    curl_easy_cleanup(curl);

#endif

#ifdef _METHOD_2_
    //second way, post raw data
    char tmp[MAX_BUFFER_SIZE];
    memset(tmp, 0, sizeof(tmp));
    snprintf(tmp, sizeof(tmp), "?cuid=%s&token=%s", cuid, token.c_str());
    strcat(host, tmp);

    CURL *curl;
    CURLcode res;
    char *resultBuf = NULL;
    struct curl_slist *headerlist = NULL;
    snprintf(tmp, sizeof(tmp), "%s","Content-Type: audio/pcm; rate=8000");
    headerlist = curl_slist_append(headerlist, tmp);
    snprintf(tmp, sizeof(tmp), "Content-Length: %d", content_len);
    headerlist = curl_slist_append(headerlist, tmp);

    curl = curl_easy_init();
    curl_easy_setopt(curl, CURLOPT_URL, host);
    curl_easy_setopt(curl, CURLOPT_POST, 1);
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30); 
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headerlist);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, audiodata);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, content_len);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, writefunc);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &resultBuf);
    res = curl_easy_perform(curl);
    if (res != CURLE_OK)
    {
        printf("perform curl error:%d.\n", res);
        return 1;
    }
    curl_slist_free_all(headerlist);
    curl_easy_cleanup(curl);

#endif
    fclose(fp);
    free(audiodata);
    return 0;
}

