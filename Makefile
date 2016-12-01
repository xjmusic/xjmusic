### DEFINITIONS

.setup:
	@cmd/setup

.build:
	@cmd/build

.clean:
	@cmd/clean

.distclean:
	@cmd/clean -d

.ideaclean:
	@cmd/clean -i

.install:
	@cmd/install

### COMMANDS

.PHONY: setup build clean distclean ideaclean install

setup: .setup

build: .build

clean: .clean

distclean: .distclean

ideaclean: .ideaclean

install: .install
