### DEFINITIONS

.setup:
	@cmd/setup

.build:
	@cmd/build

.clean:
	@cmd/clean

.distclean:
	@cmd/clean -d

### COMMANDS

.PHONY: clean distclean

all: .setup .build

setup: .setup

build: .build

clean: .clean

distclean: .distclean
