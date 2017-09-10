package integration

import basilisk.inject.Contextual
import groovy.transform.Canonical

import javax.annotation.Nonnull

@Canonical
class ContextualBean {
    @Contextual
    @Nonnull
    String someKey
}
