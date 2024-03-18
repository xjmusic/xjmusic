package io.xj.gui.nav;

import io.xj.gui.nav.Route;

import java.util.UUID;

public record NavState(UUID id, Route route) {
}
