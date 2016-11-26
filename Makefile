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
	@echo ""

.build:
	@cmd/build
	@echo ""

.clean:
	@cmd/clean
	@echo ""

.distclean:
	@cmd/clean -d
	@echo ""

#
# commands

.PHONY: clean distclean

all: .setup .build

setup: .setup

build: .build

clean: .clean

distclean: .distclean
