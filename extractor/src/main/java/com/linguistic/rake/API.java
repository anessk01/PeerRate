package com.linguistic.rake;

import java.util.LinkedHashMap;

public class API {
    public LinkedHashMap<String, Double> extract(String inputStr){
        String languageCode = RakeLanguages.ENGLISH;
        Rake rake = new Rake(languageCode);
        LinkedHashMap<String, Double> results = rake.getKeywordsFromText(inputStr);
        System.out.println(results);
        return results;
    }
}