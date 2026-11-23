package tw.gigradar.dto;

import tw.gigradar.model.Performer;

public record PerformerOut(Integer id, String name, String genre, String socialUrl) {

    public static PerformerOut from(Performer p) {
        return new PerformerOut(p.getId(), p.getName(), p.getGenre(), p.getSocialUrl());
    }
}
