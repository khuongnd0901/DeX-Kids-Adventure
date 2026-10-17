package com.khuongnd.dexkids.story;

/** Exactly-once progression shared by normal TTS callbacks and caption/watchdog paths. */
public final class QuestionTurn {
    public static final long MIN_RESPONSE_WAIT_MS = 3_000L;
    public static final long MAX_RESPONSE_WAIT_MS = 5_000L;
    public static final long CAPTION_READ_MS = 9_000L;
    public static final long VOICE_WATCHDOG_MS = 30_000L;
    private final long responseWaitMs;
    public QuestionTurn() { this(3_000L + new java.util.Random().nextInt(3)*1_000L); }
    public QuestionTurn(long responseWaitMs) {
        if(responseWaitMs<MIN_RESPONSE_WAIT_MS || responseWaitMs>MAX_RESPONSE_WAIT_MS)
            throw new IllegalArgumentException("response wait");
        this.responseWaitMs=responseWaitMs;
    }
    public long responseWaitMs() { return responseWaitMs; }
    private int stage; // prompt, waiting, answer, finished
    private long answerAt;
    public boolean isPromptPending() { return stage == 0; }
    public boolean promptFinished(long now) {
        if (stage != 0) return false;
        stage = 1; answerAt = now + responseWaitMs; return true;
    }
    public boolean beginAnswer(long now) {
        if (stage != 1 || now < answerAt) return false;
        stage = 2; return true;
    }
    public boolean answerFinished() {
        if (stage != 2) return false;
        stage = 3; return true;
    }
}
