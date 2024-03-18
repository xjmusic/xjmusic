package io.xj.gui.nav;

import jakarta.annotation.Nullable;

import java.util.UUID;

public record NavState(Route route, @Nullable UUID id) {
}
