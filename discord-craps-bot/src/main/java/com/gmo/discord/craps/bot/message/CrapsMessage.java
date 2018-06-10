package com.gmo.discord.craps.bot.message;

import java.util.Optional;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

/**
 * @author tedelen
 */
public class CrapsMessage {
    private final String text;
    private final EmbedObject embedObject;
    private final boolean replacePrevious;

    private CrapsMessage(final Builder builder) {
        embedObject = builder.embedObject;
        text = embedObject == null ? builder.text : null;
        replacePrevious = builder.replacePrevious;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
    }

    public Optional<EmbedObject> getEmbedObject() {
        return Optional.ofNullable(embedObject);
    }

    public boolean isReplacePrevious() {
        return replacePrevious;
    }

    public static Builder newBuilder(final CrapsMessage copy) {
        return CrapsMessage.newBuilder()
                .withText(copy.text)
                .withReplacePrevious(copy.replacePrevious)
                .withEmbedObject(copy.embedObject);
    }

    public static final class Builder {
        private String text;
        private EmbedObject embedObject;
        private boolean replacePrevious;

        private Builder() {
            replacePrevious = false;
        }

        public Builder withText(final String text) {
            this.text = text;
            return this;
        }

        public Builder withEmbedObject(final EmbedObject embedObject) {
            this.embedObject = embedObject;
            return this;
        }

        public Builder withReplacePrevious(final boolean replacePrevious) {
            this.replacePrevious = replacePrevious;
            return this;
        }

        public CrapsMessage build() {
            return new CrapsMessage(this);
        }
    }
}
