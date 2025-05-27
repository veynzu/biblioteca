package org.example.model;

import org.example.structures.doubleList.DoubleList;
import java.io.Serializable;

public class FriendSuggestionDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private User suggestedUser;
    private DoubleList<String> commonCategories;
    private int similarityScore; // El conteo de categorías comunes

    public FriendSuggestionDetail(User suggestedUser, DoubleList<String> commonCategories, int similarityScore) {
        this.suggestedUser = suggestedUser;
        this.commonCategories = commonCategories;
        this.similarityScore = similarityScore;
    }

    public User getSuggestedUser() {
        return suggestedUser;
    }

    public DoubleList<String> getCommonCategories() {
        return commonCategories;
    }

    public int getSimilarityScore() {
        return similarityScore;
    }

    @Override
    public String toString() {
        return "Suggestion{" +
                "user=" + (suggestedUser != null ? suggestedUser.getUsername() : "null") +
                ", score=" + similarityScore +
                ", categories=" + commonCategories +
                '}';
    }
} 