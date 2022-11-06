package ru.practicum.shareit.item.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime created;
}
