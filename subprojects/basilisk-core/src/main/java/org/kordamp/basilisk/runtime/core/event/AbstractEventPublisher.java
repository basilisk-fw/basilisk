/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.basilisk.runtime.core.event;

import basilisk.core.CallableWithArgs;
import basilisk.core.RunnableWithArgs;
import basilisk.core.event.Event;
import basilisk.core.event.EventPublisher;
import basilisk.core.event.EventRouter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public abstract class AbstractEventPublisher implements EventPublisher {
    private static final String ERROR_EVENT_NAME_BLANK = "Argument 'eventName' must not be blank";
    private static final String ERROR_LISTENER_NULL = "Argument 'listener' must not be null";
    private static final String ERROR_EVENT_CLASS_NULL = "Argument 'eventClass' must not be null";
    private static final String ERROR_EVENT_NULL = "Argument 'event' must not be null";

    private EventRouter eventRouter;

    @Inject
    public void setEventRouter(@Nonnull EventRouter eventRouter) {
        this.eventRouter = requireNonNull(eventRouter, "Argument 'eventRouter' must not be null");
    }

    public boolean isEventPublishingEnabled() {
        return eventRouter.isEventPublishingEnabled();
    }

    public void setEventPublishingEnabled(boolean enabled) {
        eventRouter.setEventPublishingEnabled(enabled);
    }

    public <E extends Event> void removeEventListener(@Nonnull Class<E> eventClass, @Nonnull CallableWithArgs<?> listener) {
        requireNonNull(eventClass, ERROR_EVENT_CLASS_NULL);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(eventClass, listener);
    }

    public <E extends Event> void removeEventListener(@Nonnull Class<E> eventClass, @Nonnull RunnableWithArgs listener) {
        requireNonNull(eventClass, ERROR_EVENT_CLASS_NULL);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(eventClass, listener);
    }

    public void addEventListener(@Nonnull Object listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(listener);
    }

    public void addEventListener(@Nonnull String eventName, @Nonnull CallableWithArgs<?> listener) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(eventName, listener);
    }

    public void addEventListener(@Nonnull String eventName, @Nonnull RunnableWithArgs listener) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(eventName, listener);
    }

    public void publishEvent(@Nonnull String eventName) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEvent(eventName);
    }

    public void publishEventOutsideUI(@Nonnull Event event) {
        requireNonNull(event, ERROR_EVENT_NULL);
        eventRouter.publishEventOutsideUI(event);
    }

    public void removeEventListener(@Nonnull Map<String, Object> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(listener);
    }

    public <E extends Event> void addEventListener(@Nonnull Class<E> eventClass, @Nonnull CallableWithArgs<?> listener) {
        requireNonNull(eventClass, ERROR_EVENT_CLASS_NULL);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(eventClass, listener);
    }

    public <E extends Event> void addEventListener(@Nonnull Class<E> eventClass, @Nonnull RunnableWithArgs listener) {
        requireNonNull(eventClass, ERROR_EVENT_CLASS_NULL);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(eventClass, listener);
    }

    public void publishEventOutsideUI(@Nonnull String eventName) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEventOutsideUI(eventName);
    }

    public void publishEventOutsideUI(@Nonnull String eventName, @Nullable List<?> params) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEventOutsideUI(eventName, params);
    }

    public void publishEvent(@Nonnull String eventName, @Nullable List<?> params) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEvent(eventName, params);
    }

    public void publishEventAsync(@Nonnull String eventName, @Nullable List<?> params) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEventAsync(eventName, params);
    }

    public void removeEventListener(@Nonnull String eventName, @Nonnull CallableWithArgs<?> listener) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(eventName, listener);
    }

    public void removeEventListener(@Nonnull String eventName, @Nonnull RunnableWithArgs listener) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(eventName, listener);
    }

    public void removeEventListener(@Nonnull Object listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.removeEventListener(listener);
    }

    public void addEventListener(@Nonnull Map<String, Object> listener) {
        requireNonNull(listener, ERROR_LISTENER_NULL);
        eventRouter.addEventListener(listener);
    }

    public void publishEvent(@Nonnull Event event) {
        requireNonNull(event, ERROR_EVENT_NULL);
        eventRouter.publishEvent(event);
    }

    public void publishEventAsync(@Nonnull String eventName) {
        requireNonBlank(eventName, ERROR_EVENT_NAME_BLANK);
        eventRouter.publishEventAsync(eventName);
    }

    public void publishEventAsync(@Nonnull Event event) {
        requireNonNull(event, ERROR_EVENT_NULL);
        eventRouter.publishEventAsync(event);
    }
}
