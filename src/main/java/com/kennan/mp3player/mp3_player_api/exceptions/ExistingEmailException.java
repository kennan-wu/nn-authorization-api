package com.kennan.mp3player.mp3_player_api.exceptions;

public class ExistingEmailException extends RuntimeException{
    public ExistingEmailException() {
        super("Email is already in use");
    }

    public ExistingEmailException(String message) {
        super(message);
    }
}
