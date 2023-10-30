package com.teamproject.subcom.search;
import java.util.*;
public class BM25Similarity {

    // BM25 매개 변수
    private final double k1 = 2;
    private final double b = 0.25;

    public BM25Similarity(){

    }
    // BM25 유사도 계산 함수
    public double calculateBM25Similarity(String query, String documentTitle) {
        String[] queryTerms = query.toLowerCase().split(" ");
        String[] documentTerms = documentTitle.toLowerCase().split(" ");

        double score = 0.0;
        for (String term : queryTerms) {
            int termFrequency = calculateTermFrequency(term, documentTerms);
            double idf = calculateIDF(term);
            double numerator = termFrequency * (k1 + 1);
            double denominator = termFrequency + k1 * (1 - b + (b * documentTerms.length / averageDocumentLength()));
            score += idf * (numerator / denominator);
        }
        return score;
    }

    // 쿼리 용어의 IDF 계산 (실제 데이터로 대체해야 함)
    public double calculateIDF(String term) {
        // 간단한 예시로 IDF를 1.0으로 설정
        return 1.0;
    }

    // 용어의 문서 내 TF 계산
    public int calculateTermFrequency(String term, String[] documentTerms) {
        int frequency = 0;
        for (String docTerm : documentTerms) {
            if (docTerm.equals(term)) {
                frequency++;
            }
        }
        return frequency;
    }

    // 평균 문서 길이 (실제 데이터로 대체해야 함)
    public double averageDocumentLength() {
        // 간단한 예시로 평균 문서 길이를 10으로 설정
        return 5.0;
    }
}
