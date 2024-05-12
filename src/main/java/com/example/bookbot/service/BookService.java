package com.example.bookbot.service;

import com.example.bookbot.client.BookAPIClient;
import com.example.bookbot.client.TelegramAPIClient;
import com.example.bookbot.dto.bookDto.Book;
import com.example.bookbot.dto.bookDto.BookResponse;
import com.example.bookbot.dto.bookDto.PublishedWorks;
import com.example.bookbot.dto.request.RootRequestDto;
import com.example.bookbot.dto.request.TelegramSendDto;
import com.example.bookbot.dto.response.RootResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@Setter
@Getter
@RequiredArgsConstructor
public class BookService {
    private final TelegramAPIClient telegramClient;
    private final BookAPIClient bookClient;
    private final String host = "book-finder1.p.rapidapi.com";
    private final String key = "674678e122mshd00ec5b8f945302p1052bcjsn0ad69ed2af91";
    private Random random = new Random();
    private Long lastUpdateId = 0L;


    public RootRequestDto getUpdateService() {
        RootRequestDto updates = telegramClient.getUpdates(0L);
        if (!updates.getResult().isEmpty()) {
            Integer updateId = updates.getResult().get(updates.getResult().size() - 1).getUpdateId();
            log.info("Message got from - " + updates.getResult().get(0).getMessage().getFrom().getFirstName() + ", ID - "
                    + updates.getResult().get(0).getMessage().getChat().getId());
            return telegramClient.getUpdates(Long.valueOf(updateId));
        }
        return null;
    }

    public BookResponse getUpdates() {
        return bookClient.getData(host, key);
    }


    public RootResponseDto sendMessage(TelegramSendDto dto) {
        return telegramClient.sendMessage(dto);
    }

    public String formatBookInfo(Book book) {
        String authors = book.getAuthors().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "))
                .replace("[", "")
                .replace("]", "");

        String coverArtUrl = null;
        for (PublishedWorks publishedWork : book.getPublishedWorks()) {
            if (publishedWork.getCoverArtUrl() != null) {
                coverArtUrl = publishedWork.getCoverArtUrl();
                break;
            }
        }

        coverArtUrl = coverArtUrl != null ? coverArtUrl : "Cover art not available";

        return String.format("📚Book information:\n\n\n"
                        + " - Book Title: %s\n\n"
                        + " - Book Author: %s\n\n"
                        + " - Book's Summary: %s\n\n"
                        + " - Book's Cover Art URL: %s\n",
                book.getTitle(),
                authors,
                book.getSummary(),
                coverArtUrl);
    }

    public RootResponseDto sendInfo() {
        RootRequestDto updateService = getUpdateService();
        if (updateService != null) {
            String text = updateService.getResult().get(0).getMessage().getText();
            Long id = updateService.getResult().get(0).getMessage().getChat().getId();
            TelegramSendDto dto = new TelegramSendDto();
            dto.setChatId(String.valueOf(id));
            BookResponse updates = getUpdates();

            if (text.equals("/start")) {
                String msg = "Hi " + updateService.getResult().get(0).getMessage().getFrom().getFirstName() +
                        ", welcome to book recommender!";
                dto.setText(msg);
                return sendMessage(dto);
            }

            boolean containsBookWord = text.toLowerCase().contains("book");

            if (updates != null && updates.getResults() != null && !updates.getResults().isEmpty()) {
                boolean foundBook = false;
                if (containsBookWord) {
                    Book randomBook = updates.getResults().get(random.nextInt(updates.getResults().size()));
                    dto.setText(formatBookInfo(randomBook));
                    sendMessage(dto);
                    foundBook = true;
                } else {
                    for (Book book : updates.getResults()) {
                        if (book.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                                book.getAuthors().stream().anyMatch(author -> author.toLowerCase().contains(text.toLowerCase()))) {
                            foundBook = true;
                            dto.setText(formatBookInfo(book));
                            sendMessage(dto);
                            break;
                        }
                    }
                }
                if (!foundBook) {
                    log.info("No matching book found. Msg got from - " +
                            updateService.getResult().get(0).getMessage().getFrom().getFirstName());
                    dto.setText("Sorry, I couldn't find any book information for the title or author '" + text + "'.");
                    sendMessage(dto);
                }
            } else {
                dto.setText("Sorry, no book recommendations available at the moment.");
                sendMessage(dto);
            }
        }
        return null;
    }

    @Scheduled(fixedDelay = 1000)
    public void refresh() {
        RootRequestDto updateService = getUpdateService();
        if (updateService != null && !updateService.getResult().isEmpty()) {
            Integer latestUpdateId = updateService.getResult().get(updateService.getResult().size() - 1).getUpdateId();
            if (latestUpdateId > lastUpdateId) {
                lastUpdateId = Long.valueOf(latestUpdateId);
                sendInfo();
            }
        }
    }
}
