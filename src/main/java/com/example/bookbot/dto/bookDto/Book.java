package com.example.bookbot.dto.bookDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private List<String> authors;
    private String summary;
    @NotNull
    @JsonProperty("cover_art_url")
    private String coverArtUrl;
    @JsonProperty("published_works")
    private List<PublishedWorks> publishedWorks;
}
