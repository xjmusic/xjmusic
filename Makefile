PWD                := $(shell pwd)

TIMESTAMP          := $(shell date -u +%Y%m%d.%H%M%S)
GIT_BRANCH         := $(shell git rev-parse --abbrev-ref HEAD)
GIT_COMMIT         := $(shell git rev-parse --short HEAD)

LDFLAGS            := -X main.build='$(GIT_BRANCH)/$(GIT_COMMIT)/$(TIMESTAMP)'
TARGET_DIR         := $(PWD)/bin

# cool ascii bro █▓▒░
SECTION_IN         := "\\n██"
SECTION            := " ■"
SECTION_MINI       := " »"
SECTION_DONE_OK    := "░░ OK\\n"

#
# environment

.setup:
	@cmd/setup

.clean:
	@cmd/clean

.distclean:
	@cmd/clean -d

#
# commands

.PHONY: clean distclean core ui

setup: .setup
	@echo ""

core:
	@cmd/build_core
	@echo ""

ui:
	@cmd/build_ui
	@echo ""

clean: .clean
	@echo ""

distclean: .distclean
	@echo ""
