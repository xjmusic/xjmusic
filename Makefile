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

.PHONY: setup build clean distclean

setup: .setup

build: .build

clean: .clean

distclean: .distclean
